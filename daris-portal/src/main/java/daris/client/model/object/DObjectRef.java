package daris.client.model.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import arc.mf.object.lock.LockToken;
import daris.client.model.IDUtil;
import daris.client.model.collection.DObjectCollectionRef;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.messages.DObjectLockToken;
import daris.client.model.project.Project;
import daris.client.model.query.HasXPathValues;
import daris.client.model.query.XPathValue;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public class DObjectRef extends ObjectRef<DObject> implements Comparable<DObjectRef>, HasXPathValues {

    private String _id;
    private String _proute;
    private boolean _foredit;
    private boolean _testLeaf;
    private int _nbChildren;
    private boolean _resolved = false;

    private DObjectCollectionRef _childrenRef;

    private Map<String, XPathValue> _xpathValues;
    
    private String _name;

    public DObjectRef(String id) {
        this(id, null, false, true, -1);
    }

    public DObjectRef(String id, int nbChildren) {

        this(id, null, false, true, nbChildren);
    }

    public DObjectRef(String id, String proute, boolean foredit, boolean testLeaf, int nbChildren) {

        _id = id;
        _name = null;
        _proute = proute;
        _foredit = foredit;
        _testLeaf = testLeaf;
        _nbChildren = nbChildren;
        _childrenRef = new DObjectCollectionRef(this);
    }

    public DObjectRef(DObject o, boolean foredit, boolean testLeaf) {

        super(o);
        _id = o.id();
        _name = o.name();
        _proute = o.proute();
        _foredit = foredit;
        _testLeaf = testLeaf;
        _childrenRef = new DObjectCollectionRef(this);
        _nbChildren = o.nbChildren();
    }

    public DObjectCollectionRef childrenRef() {

        return _childrenRef;
    }

    public void addXpathValue(XPathValue value) {
        if (_xpathValues == null) {
            _xpathValues = new TreeMap<String, XPathValue>();
        }
        _xpathValues.put(value.xpath(), value);
    }

    public XPathValue xpathValue(String xpath) {
        if (_xpathValues != null) {
            return _xpathValues.get(xpath);
        }
        return null;
    }

    public List<XPathValue> xpathValues() {
        if (_xpathValues != null && !_xpathValues.isEmpty()) {
            return new ArrayList<XPathValue>(_xpathValues.values());
        }
        return null;
    }

    public void clearXpathValues() {
        if (_xpathValues != null) {
            _xpathValues.clear();
        }
    }

    public void setForEdit(boolean foredit) {

        if (_foredit != foredit) {
            reset();
        }
        _foredit = foredit;
    }

    public boolean forEdit() {
        return _foredit;
    }

    public String id() {

        return _id;
    }

    public String proute() {

        return _proute;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {

        resolveServiceArgs(w, false);
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w, boolean lock) {

        if (_proute != null) {
            w.add("id", new String[] { "proute", _proute }, _id);
        } else {
            w.add("id", _id);
        }
        w.add("foredit", _foredit);
        w.add("isleaf", _testLeaf);

        if (lock) {
            // Can only lock the latest version.
            w.add("lock", new String[] { "type", "transient", "timeout", "60", "descend", "false" }, true);
        }
    }

    @Override
    public boolean supportLocking() {

        return true;
    }

    @Override
    protected String resolveServiceName() {

        return "om.pssd.object.describe";
    }

    // The structure of the XmlElement is that from om.pssd.object.describe
    @Override
    protected DObject instantiate(XmlElement xe) throws Throwable {

        DObject o = null;
        if (xe != null) {
            XmlElement oe = xe.element("object");
            if (oe != null) {
                _proute = oe.value("id/@proute");
                o = DObject.create(oe);
                _resolved = true;
            }
        }
        if (o != null) {
            _nbChildren = o.nbChildren();
        }
        _name = o.name();
        return o;
    }
    
    public String name(){
        return _name;
    }

    @Override
    protected LockToken instantiateLockToken(XmlElement xe) throws Throwable {

        // object lock does not have an id -- use the object citeable id.
        String id = xe.value("object/id");

        XmlElement le = xe.element("object/lock");
        if (le == null) {
            return null;
        }

        return new DObjectLockToken(id);
    }

    @Override
    public boolean resolved() {

        if (_resolved == false) {
            return false;
        }
        return super.resolved();
    }

    public DObject.Type referentType() {
        return referent() != null ? referent().type() : IDUtil.typeFromId(_id);
    }

    @Override
    public String referentTypeName() {

        DObject.Type type = referentType();
        return type != null ? type.toString() : "pssd-object";
    }

    @Override
    public String idToString() {

        return _id;
    }

    @Override
    public boolean equals(Object o) {

        if (o != null) {
            if (o instanceof DObjectRef) {
                DObjectRef r = (DObjectRef) o;
                return ObjectUtil.equals(_id, r.id()) && ObjectUtil.equals(_proute, r.proute());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {

        return (_id + _proute).hashCode();
    }

    @Override
    public int compareTo(DObjectRef o) {

        if (o == null) {
            return 1;
        }
        if (_id != null) {
            if (o.id() != null) {
                return IDUtil.compare(_id, o.id());
            } else {
                return -1;
            }
        } else {
            if (o.id() != null) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {

        return _id;
    }

    public DObject createEmptyChildObject() {

        /**
         * only support creating project, subject and study objects.
         */
        if (referent() != null) {
            if (referent() instanceof Repository) {
                return new Project();
            } else if (referent() instanceof Project) {
                return new Subject();
            } else if (referent() instanceof ExMethod) {
                return new Study(id());
            }
        } else {
            if (_id == null) {
                return new Project();
            } else {
                DObject.Type type = IDUtil.childTypeFromId(_id);
                if (type == DObject.Type.subject) {
                    return new Subject();
                } else if (type == DObject.Type.study) {
                    return new Study(id());
                }
            }
        }
        return null;
    }

    public int nbChildren() {
        if (this.referent() != null) {
            return referent().nbChildren();
        }
        return _nbChildren;
    }

    public boolean isProject() {
        return IDUtil.isProjectId(_id);
    }

    public boolean isSubject() {
        return IDUtil.isSubjectId(_id);
    }

    public boolean isExMethod() {
        return IDUtil.isExMethodId(_id);
    }

    public boolean isStudy() {
        return IDUtil.isStudyId(_id);
    }

    public boolean isDataSet() {
        return IDUtil.isDataSetId(_id);
    }

    public boolean isRepository() {
        return _id == null;
    }

    public String projectId() {
        if (isRepository()) {
            return null;
        } else if (isProject()) {
            return _id;
        } else {
            return IDUtil.getProjectId(_id);
        }
    }
}
