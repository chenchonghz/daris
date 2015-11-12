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

        public static Type fromString(String value) throws Throwable {
            if (value != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.typeName().equals(value)) {
                        return v;
                    }
                }
            }
            throw new Exception(
                    "Failed to instantiate DObject.Type from string: " + value);
        }
    }

    private String _route;
    private String _aid;
    private String _cid;
    private String _name;
    private String _description;
    private int _numberOfChildren;

    protected DObject(XmlDoc.Element oe) throws Throwable {
        _aid = oe.value("id/asset");
        _route = oe.value("id/proute");
        _cid = oe.value("id");
        _name = oe.value("name");
        _description = oe.value("description");
        _numberOfChildren = oe.intValue("number-of-children", -1);
    }

    public abstract Type type();

    public String assetId() {
        return _aid;
    }

    public String citeableId() {
        return _cid;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public static DObject create(XmlDoc.Element oe) throws Throwable {
        if (oe != null) {
            DObject.Type type = DObject.Type.fromString(oe.value("@type"));
            if (type == null) {
                throw new Exception(
                        "No type attribute found in XML element: " + oe);
            }
            switch (type) {
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

    public boolean hasMetadataForView() {
        // TODO:
        return false;
    }

    public boolean hasMetadataForEdit() {
        // TODO:
        return false;
    }

    public int numberOfChildren() {
        return _numberOfChildren;
    }

}
