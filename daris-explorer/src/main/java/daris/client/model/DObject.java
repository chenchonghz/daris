package daris.client.model;

import arc.mf.client.ServerRoute;
import arc.xml.XmlDoc;

public abstract class DObject {

    public static enum Type {
        REPOSITORY("repository"), PROJECT("project"), SUBJECT(
                "subject"), EX_METHOD("ex-method"), STUDY("study"), DATASET(
                        "dataset"), METHOD("method");
        private String _typeName;

        Type(String typeName) {
            _typeName = typeName;
        }

        public String typeName() {
            return _typeName;
        }

        @Override
        public String toString() {
            return _typeName;
        }

        public static Type[] modelTypes() {
            return new Type[] { Type.PROJECT, Type.SUBJECT, Type.EX_METHOD,
                    Type.STUDY, Type.DATASET };
        }

        public static Type parse(XmlDoc.Element e, Type defaultValue)
                throws Throwable {
            if (e.nameEquals(Type.REPOSITORY.typeName())) {
                return Type.REPOSITORY;
            }
            if (e.nameEquals(Type.METHOD.typeName())) {
                return Type.METHOD;
            }
            if (e.nameEquals("object")) {
                String value = e.value("@type");
                if (value != null) {
                    Type[] vs = values();
                    for (Type v : vs) {
                        if (v.typeName().equals(value)) {
                            return v;
                        }
                    }
                }
            }
            return defaultValue;
        }
    }

    private String _route;
    private String _aid;
    private String _cid;
    private String _name;
    private String _description;
    private int _numberOfChildren;
    private String _namespace;
    private XmlDoc.Element _metadataForView;
    private XmlDoc.Element _metadataForEdit;

    protected DObject(XmlDoc.Element oe) throws Throwable {
        _aid = oe.value("id/asset");
        _route = oe.value("id/proute");
        _cid = oe.value("id");
        _name = oe.value("name");
        _description = oe.value("description");
        _numberOfChildren = oe.intValue("number-of-children", -1);
        _namespace = oe.value("namespace");
        if (oe.elementExists("meta/metadata")) {
            _metadataForEdit = oe.element("meta");
        } else {
            _metadataForView = oe.element("meta");
        }
    }

    public abstract Type type();

    public String assetId() {
        return _aid;
    }

    public String citeableId() {
        return _cid;
    }

    public String namespace() {
        return _namespace;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public static DObject create(XmlDoc.Element oe) throws Throwable {
        if (oe != null) {
            DObject.Type type = DObject.Type.parse(oe, null);
            if (type == null) {
                throw new Exception(
                        "No type attribute found in XML element: " + oe);
            }
            switch (type) {
            case REPOSITORY:
                return new Repository(oe);
            case PROJECT:
                return new Project(oe);
            case SUBJECT:
                return new Subject(oe);
            case EX_METHOD:
                return new ExMethod(oe);
            case STUDY:
                return new Study(oe);
            case DATASET:
                return new DataSet(oe);
            default:
                break;
            }
        }
        throw new Exception(
                "Failed to instantiate DaRIS object from XML element: " + oe);
    }

    public ServerRoute serverRoute() {
        return _route == null ? null : new ServerRoute(_route);
    }

    public String route() {
        return _route;
    }

    public XmlDoc.Element metadataForView() {
        return _metadataForView;
    }

    public boolean hasMetadataForView() {
        return _metadataForView != null;
    }

    public XmlDoc.Element metadataForEdit() {
        return _metadataForEdit;
    }

    public boolean hasMetadataForEdit() {
        return _metadataForEdit != null;
    }

    public int numberOfChildren() {
        return _numberOfChildren;
    }

}
