package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public class ScpSink extends Sink {

    public static final int DEFAULT_SSH_PORT = 22;

    public static enum Param {
        HOST("host"), PORT("port"), HOST_KEY("host-key"), USER("user"), PASSWORD("password"), PRIVATE_KEY("private-key"), DIRECTORY(
                "directory"), DECOMPRESS("decompress"), FILE_MODE("file-mode");
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

        public static int parseInt(XmlElement se, Param param, int defaultValue) throws Throwable {
            return se.intValue("destination/arg[@name='" + param.paramName() + "']", defaultValue);
        }

        public static boolean parseBoolean(XmlElement se, Param param, boolean defaultValue) throws Throwable {
            return se.booleanValue("destination/arg[@name='" + param.paramName() + "']", defaultValue);
        }
    }

    private String _host;
    private int _port;
    private String _hostKey;
    private String _user;
    private String _password;
    private String _privateKey;
    private String _directory;
    private boolean _decompress;
    private String _fileMode;

    ScpSink(XmlElement se) throws Throwable {
        super(se);
        _host = Param.parseString(se, Param.HOST);
        _port = Param.parseInt(se, Param.PORT, -1);
        _hostKey = Param.parseString(se, Param.HOST_KEY);
        _user = Param.parseString(se, Param.USER);
        _password = Param.parseString(se, Param.PASSWORD);
        _privateKey = Param.parseString(se, Param.PRIVATE_KEY);
        _directory = Param.parseString(se, Param.DIRECTORY);
        _decompress = Param.parseBoolean(se, Param.DECOMPRESS, false);
        _fileMode = Param.parseString(se, Param.FILE_MODE);
    }

    public String serverHost() {
        return _host;
    }

    public int serverPort() {
        return _port;
    }

    public String serverHostKey() {
        return _hostKey;
    }

    public String user() {
        return _user;
    }

    public String userPassword() {
        return _password;
    }

    public String userPrivateKey() {
        return _privateKey;
    }

    public String directory() {
        return _directory;
    }

    public boolean decompress() {
        return _decompress;
    }

    public String fileMode() {
        return _fileMode;
    }

}
