package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public abstract class Sink {

    public static String URL_PREFIX = "sink:";

    public static enum Type {

        file_system("file-system"), scp("scp"), webdav("webdav"), owncloud("owncloud");

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

        public static Type fromString(String type) {
            if (type != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.typeName().equals(type)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private Type _type;
    private String _name;
    private String _description;

    Sink(XmlElement se) throws Throwable {
        _name = se.value("@name");
        _type = Type.fromString(se.value("destination/type"));
        _description = se.value("description");
    }

    public Type type() {
        return _type;
    }

    public String name() {
        return _name;
    }

    public String url() {
        return Sink.URL_PREFIX + _name;
    }

    public String description() {
        return _description;
    }

    public static String nameFromUrl(String url) {
        return url.substring(URL_PREFIX.length());
    }

    public static Sink instantiate(XmlElement se) throws Throwable {
        Type type = Type.fromString(se.value("destination/type"));
        switch (type) {
        case file_system:
            return new FileSystemSink(se);
        case scp:
            return new ScpSink(se);
        case webdav:
            return new WebDavSink(se);
        case owncloud:
            return new OwnCloudSink(se);
        default:
            return null;
        }
    }

}
