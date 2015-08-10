package daris.client.model.object;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.method.Method;
import daris.client.model.object.messages.CanModify;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.project.Project;
import daris.client.model.rsubject.RSubject;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public abstract class DObject {

    public static enum Type {
        project, subject, ex_method, study, dataset, data_object, method, r_subject, repository;

        @Override
        public final String toString() {
            return super.toString().replace('_', '-');
        }

        public String model() {
            switch (this) {
            case project:
            case subject:
            case ex_method:
            case study:
            case dataset:
                return "om.pssd." + toString();
            default:
                return null;
            }
        }

        public static Type parse(String s) {
            if (s == null) {
                return null;
            }
            return valueOf(s.toLowerCase().replace('-', '_'));
        }

        public static Type[] childTypesFor(Type parent) {
            if (parent == null) {
                return values();
            }
            switch (parent) {
            case repository:
                return new Type[] { project, subject, ex_method, study, dataset };
            case project:
                return new Type[] { subject, ex_method, study, dataset };
            case subject:
                return new Type[] { ex_method, study, dataset };
            case ex_method:
                return new Type[] { study, dataset };
            case study:
                return new Type[] { dataset };
            default:
                return new Type[0];
            }
        }

        public static boolean contains(Type parent, Type child) {
            Type[] ctypes = childTypesFor(parent);
            for (Type ctype : ctypes) {
                if (ctype == child) {
                    return true;
                }
            }
            return false;
        }
    }

    private static int VERSION_LATEST = 0;
    private String _proute;
    private String _id;
    private String _vid;
    private String _assetId;
    private String _name;
    private String _description;
    private String _namespace;
    private boolean _editable;
    private int _version;
    private boolean _isleaf;
    private int _nbChildren = -1;
    private String _fileName;

    private XmlElement _meta = null;
    private XmlElement _metaForEdit = null;

    private List<Tag> _tags;

    private boolean _allowIncompleteMeta = false;

    /**
     * Constructor.
     * 
     * @param xe
     *            XML Element object contains the object detail.
     * @throws Throwable
     */
    protected DObject(XmlElement xe) throws Throwable {

        if (xe.name().equals("object")) {
            // xe is the result of om.pssd.object.describe
            _id = xe.value("id");
            if (_id == null) {
                _id = xe.value("@id");
            }
            _assetId = xe.value("id/@asset");
            _proute = xe.value("id/@proute");
            _vid = xe.value("@vid");
            try {
                _editable = xe.booleanValue("@editable", false);
            } catch (Throwable e) {
                _editable = false;
            }
            try {
                _version = xe.intValue("@version", VERSION_LATEST);
            } catch (Throwable e) {
                _version = VERSION_LATEST;
            }
            _name = xe.value("name");
            _description = xe.value("description");
            _namespace = xe.value("namespace");
            _fileName = xe.value("filename");
            try {
                _isleaf = xe.booleanValue("isleaf", false);
            } catch (Throwable e) {
                _isleaf = false;
            }
            try {
                _nbChildren = xe.intValue("number-of-children", -1);
            } catch (Throwable e) {
                _nbChildren = -1;
            }
            XmlElement me = xe.element("meta");
            if (me != null) {
                if (me.element("metadata") != null) {
                    _metaForEdit = me;
                } else {
                    _meta = me;
                }
            }
            List<XmlElement> tes = xe.elements("tag");
            if (tes != null && !tes.isEmpty()) {
                _tags = new ArrayList<Tag>(tes.size());
                for (XmlElement te : tes) {
                    _tags.add(new Tag(_id, te.intValue("@id"), te.value()));
                }
            }
        } else if (xe.name().equals("method")) {
            _id = xe.value("id");
            if (_id == null) {
                _id = xe.value("@id");
            }
            _proute = xe.value("@proute");
            if (_proute == null) {
                _proute = xe.value("id/@proute");
            }
            _editable = false;
            try {
                _version = xe.intValue("@version", VERSION_LATEST);
            } catch (Throwable e) {
                _version = VERSION_LATEST;
            }
            _name = xe.value("name");
            _description = xe.value("description");
            _namespace = xe.value("namespace");
            _isleaf = true;
            _nbChildren = 0;
            // _filename is null - methods have no content
        } else if (xe.name().equals("repository")) {
            // xe is the result of om.pssd.repository.describe
            _id = xe.value("id");
            _name = xe.value("name");
            _description = xe.value("description");
            _isleaf = false;
            try {
                _nbChildren = xe.intValue("number-of-projects", -1);
            } catch (Throwable e) {
                _nbChildren = -1;
            }
        }
    }

    /**
     * Constructor.
     * 
     * @param proute
     * @param id
     * @param name
     * @param description
     * @param editable
     * @param version
     * @param isleaf
     */
    protected DObject(String id, String proute, String name,
            String description, boolean editable, int version, boolean isleaf) {

        _id = id;
        _proute = proute;
        _name = name;
        _description = description;
        _editable = editable;
        _version = version;
        _isleaf = isleaf;
        _nbChildren = -1;

    }

    public DObject(String id, String proute, String name, String description,
            boolean editable, int version, int nbChildren) {
        _id = id;
        _proute = proute;
        _name = name;
        _description = description;
        _editable = editable;
        _version = version;
        _nbChildren = nbChildren;
        _isleaf = _nbChildren == 0;
    }

    /**
     * The asset/object version.
     * 
     * @return
     */
    public int version() {

        return _version;

    }

    public void setVersion(int version) {
        _version = version;
    }

    /**
     * The number of direct children.
     * 
     * @return
     */
    public int nbChildren() {
        return _nbChildren;
    }

    /**
     * True if the object has no children.
     * 
     * @return
     */
    public boolean isleaf() {

        return _isleaf;

    }

    /**
     * The parent ID, if any.
     * 
     * @return
     */
    public String pid() {

        return IDUtil.getParentId(_id);

    }

    public String vid() {
        return _vid;
    }

    /**
     * Equality.
     */
    public boolean equals(Object o) {

        if (o instanceof DObject) {
            if (ObjectUtil.equals(_proute, ((DObject) o).proute())) {
                if (ObjectUtil.equals(_id, ((DObject) o).id())) {
                    return true;
                }
            }
        }
        return false;

    }

    public int hashCode() {

        return (_id + _proute).hashCode();
    }

    /**
     * Is the object in some other repository other than the one we are
     * connected to?
     * 
     * @return
     */
    public boolean isRemote() {

        if (proute() == null) {
            return false;
        }
        return true;

    }

    /**
     * Identity of the remote server.
     * 
     * @return
     */
    public String remoteServerID() {

        return IDUtil.getLastSection(_proute);

    }

    /**
     * The path route to the repository where this object is located.
     * 
     * @return
     */
    public String proute() {

        return _proute;

    }

    /**
     * The identify of this object.
     * 
     * @return
     */
    public String id() {

        return _id;

    }

    public String name() {

        return _name;
    }

    public void setName(String name) {

        _name = name;
    }

    public String description() {

        return _description;

    }

    public String namespace() {
        return _namespace;
    }

    protected void setNamespace(String namespace) {
        _namespace = namespace;
    }

    public void setDescription(String description) {

        _description = description;
    }

    public String fileName() {
        return _fileName;
    }

    public void setFileName(String fileName) {
        _fileName = fileName;
    }

    public boolean editable() {

        return _editable;
    }

    public String assetId() {

        return _assetId;
    }

    public void setAssetId(String assetId) {
        _assetId = assetId;
    }

    public XmlElement meta() {

        return _meta;
    }

    public void setMeta(XmlStringWriter w) {

        try {
            _meta = XmlDoc.parse(w.document());
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
    }

    public XmlElement metaForEdit() {

        return _metaForEdit;
    }

    public void setMetaForEdit(XmlElement metaForEdit) {

        _metaForEdit = metaForEdit;
    }

    public boolean hasMeta() {

        if (_meta != null) {
            if (_meta.hasElements()) {
                return true;
            }
        }
        return false;
    }

    public static DObject create(XmlElement oe) throws Throwable {
        DObject.Type type = DObject.Type.parse(oe.value("@type"));
        if (type == null) {
            throw new IllegalArgumentException(
                    "Failed to parse object type from: " + oe);
        }
        switch (type) {
        case project:
            return new Project(oe);
        case subject:
            return new Subject(oe);
        case ex_method:
            return new ExMethod(oe);
        case study:
            return new Study(oe);
        case dataset:
            return DataSet.create(oe);
        case data_object:
            return new DataObject(oe);
        case r_subject:
            return new RSubject(oe);
        case method:
            return new Method(oe);
        default:
            break;
        }
        throw new IllegalArgumentException("Failed to instantiate " + type
                + " from " + oe);
    }

    public abstract Type type();

    protected abstract DObjectCreate objectCreateMessage(DObjectRef po);

    public void create(DObjectRef po, ObjectMessageResponse<DObjectRef> rh) {

        DObjectCreate msg = objectCreateMessage(po);
        if (msg != null) {
            msg.send(rh);
        }
    }

    public void createServiceArgs(XmlWriter w) {

        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        if (_fileName != null) {
            w.add("filename", _fileName);
        }
        if (_allowIncompleteMeta) {
            w.add("allow-incomplete-meta", _allowIncompleteMeta);
        }
        if (_meta != null) {
            w.add(_meta, true);
        }

    }

    protected abstract DObjectUpdate objectUpdateMessage();

    public void update(ObjectMessageResponse<Boolean> rh) {

        DObjectUpdate msg = objectUpdateMessage();
        if (msg != null) {
            msg.send(rh);
        }
    }

    public void updateServiceArgs(XmlWriter w) {

        w.add("id", _id);
        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        if (_fileName != null) {
            w.add("filename", _fileName);
        }
        if (_meta != null) {
            w.add(_meta, true);
        }
        if (_allowIncompleteMeta) {
            w.add("allow-incomplete-meta", _allowIncompleteMeta);
        }

    }

    public void editable(ObjectMessageResponse<Boolean> rh) {

        new CanModify(this).send(rh);
    }

    public List<Tag> tags() {
        return _tags;
    }

    public boolean hasTags() {

        return _tags != null && !_tags.isEmpty();
    }

    public void setAllowIncompleteMeta(boolean allow) {
        _allowIncompleteMeta = allow;
    }

    public boolean allowIncompleteMeta() {
        return _allowIncompleteMeta;
    }

}
