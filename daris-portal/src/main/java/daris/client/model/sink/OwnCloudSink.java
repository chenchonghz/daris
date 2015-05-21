package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public class OwnCloudSink extends WebDavSink {

    public static enum Param {
        URL("url"), DIRECTORY("directory"), DECOMPRESS("decompress"), USER("user"), PASSWORD("password"), CHUNKED(
                "chunked");
        private String _paramName;

        Param(String paramName) {
            _paramName = paramName;
        }

        public final String paramName() {
            return _paramName;
        }

        public final String toString() {
            return _paramName;
        }

        public static String parseString(XmlElement se, Param param) {
            return se.value("destination/arg[@name='" + param.paramName() + "']");
        }

        public static boolean parseBoolean(XmlElement se, Param param, boolean defaultValue) throws Throwable {
            return se.booleanValue("destination/arg[@name='" + param.paramName() + "']", defaultValue);
        }
    }

    private boolean _chunked;

    OwnCloudSink(XmlElement se) throws Throwable {
        super(se);
        _chunked = Param.parseBoolean(se, Param.CHUNKED, false);
    }

    public boolean chunkedUpload() {
        return _chunked;
    }

}
