package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public class WebDavSink extends Sink {

    public static enum Param {
        URL("url"), DIRECTORY("directory"), DECOMPRESS("decompress"), USER("user"), PASSWORD("password");
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

    private String _url;
    private String _directory;
    private boolean _decompress;
    private String _user;
    private String _password;

    WebDavSink(XmlElement se) throws Throwable {
        super(se);
        _url = Param.parseString(se, Param.URL);
        _directory = Param.parseString(se, Param.DIRECTORY);
        _decompress = Param.parseBoolean(se, Param.DECOMPRESS, false);
        _user = Param.parseString(se, Param.USER);
        _password = Param.parseString(se, Param.PASSWORD);
    }

    public String serverUrl() {
        return _url;
    }

    public String directory() {
        return _directory;
    }

    public boolean decompress() {
        return _decompress;
    }

    public String user() {
        return _user;
    }

    public String userPassword() {
        return _password;
    }

}
