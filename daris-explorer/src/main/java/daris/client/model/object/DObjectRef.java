package daris.client.model.object;

import arc.mf.client.ServerRoute;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectRef;
import arc.xml.XmlDoc.Element;
import daris.client.model.CiteableIdUtils;
import daris.client.model.repository.RepositoryRef;

public class DObjectRef extends ObjectRef<DObject>
        implements Comparable<DObjectRef> {

    private String _route;
    private String _cid;
    private String _name;
    private String _description;
    private int _numberOfChildren;
    private boolean _forEdit;
    private boolean _resolved = false;

    public DObjectRef(String route, String cid, String name, String description,
            int numberOfChildren, boolean forEdit) {
        _route = route;
        _cid = cid;
        _name = name;
        _description = description;
        _numberOfChildren = numberOfChildren;
        _forEdit = forEdit;
    }

    public DObjectRef(DObject obj) {
        super(obj);
        _route = obj.route();
        _cid = obj.citeableId();
        _name = obj.name();
        _description = obj.description();
        _numberOfChildren = obj.numberOfChildren();
        _forEdit = obj.hasMetadataForEdit();
    }

    public DObjectRef(String cid, int numberOfChildren) {
        this(null, cid, null, null, numberOfChildren, false);
    }

    public boolean resolved() {
        return _resolved && super.resolved();
    }
    
    public void reset() {
        super.reset();
        _resolved = false;
    }

    public void setForEdit(boolean forEdit) {
        if (_forEdit != forEdit) {
            reset();
            _forEdit = forEdit;
        }
    }

    public String route() {
        return _route;
    }

    @Override
    protected ServerRoute serverRoute() {
        return _route == null ? null : new ServerRoute(_route);
    }

    @Override
    public String idToString() {
        return _cid;
    }

    @Override
    protected DObject instantiate(Element xe) throws Throwable {
        DObject o = DObject.create(xe.element("object"));
        if (o != null) {
            _name = o.name();
            _description = o.description();
            if (o.numberOfChildren() > -1) {
                _numberOfChildren = o.numberOfChildren();
            }
        }
        if (!_resolved) {
            _resolved = true;
        }
        return o;
    }

    @Override
    public String referentTypeName() {
        if (referent() != null) {
            return referent().type().typeName();
        } else {
            DObject.Type type = CiteableIdUtils.getTypeFromCID(_cid);
            if (type == null) {
                return null;
            }
            return type.typeName();
        }
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w) {
        if (_route != null) {
            w.add("id", new String[] { "proute", _route }, _cid);
        } else {
            w.add("id", _cid);
        }
        w.add("isleaf", true);
        w.add("foredit", _forEdit);
    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.object.describe";
    }

    public String citeableId() {
        return _cid;
    }

    public String name() {
        return _name;
    }

    protected void setName(String name) {
        _name = name;
    }

    public String description() {
        return _description;
    }

    protected void setDescription(String description) {
        _description = description;
    }

    public int numberOfChildren() {
        return _numberOfChildren;
    }

    protected void setNumberOfChildren(int numberOfChildren) {
        _numberOfChildren = numberOfChildren;
    }

    public Fuzzy hasChildren() {
        if (numberOfChildren() < 0) {
            return Fuzzy.MAYBE;
        } else if (numberOfChildren() == 0) {
            return Fuzzy.NO;
        } else {
            return Fuzzy.YES;
        }
    }

    @Override
    public int compareTo(DObjectRef o) {
        String cid1 = citeableId();
        String cid2 = o == null ? null : o.citeableId();
        if (cid1 == null && cid2 == null) {
            return 0;
        }
        if (cid1 == null && cid2 != null) {
            if (this instanceof RepositoryRef) {
                return 1;
            } else {
                return -1;
            }
        }
        if (cid1 != null && cid2 == null) {
            if (o instanceof RepositoryRef) {
                return -1;
            } else {
                return 1;
            }
        }
        return CiteableIdUtils.compare(cid1, cid2);
    }

    public boolean isRepository() {
        return (this instanceof RepositoryRef) || _cid == null;
    }

    public boolean isProject() {
        return CiteableIdUtils.isProjectCID(_cid);
    }

    public boolean isSubject() {
        return CiteableIdUtils.isSubjectCID(_cid);
    }

    public boolean isExMethod() {
        return CiteableIdUtils.isExMethodCID(_cid);
    }

    public boolean isStudy() {
        return CiteableIdUtils.isStudyCID(_cid);
    }

    public boolean isDataSet() {
        return CiteableIdUtils.isDataSetCID(_cid);
    }

}
