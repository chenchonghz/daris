package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public class FileSystemSink extends Sink {

    public static enum Save {
        metadata, content;
        public static Save fromString(String save) {
            Save[] vs = values();
            for (Save v : vs) {
                if (v.name().equals(save)) {
                    return v;
                }
            }
            return null;
        }
    }

    public static enum Param {
        DIRECTORY("directory"), PATH("path"), SAVE("save"), DECOMPRESS("decompress");
        private String _paramName;

        Param(String paramName) {
            _paramName = paramName;
        }

        public final String paramName() {
            return _paramName;
        }

        @Override
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

        public static Save parseSave(XmlElement se) {
            return Save.fromString(parseString(se, Param.SAVE));
        }
    }

    private String _directory;
    private String _path;
    private Save _save;
    private boolean _decompress;

    FileSystemSink(XmlElement se) throws Throwable {
        super(se);
        _directory = Param.parseString(se, Param.DIRECTORY);
        _path = Param.parseString(se, Param.PATH);
        _save = Param.parseSave(se);
        _decompress = Param.parseInt(se, Param.DECOMPRESS, 0) > 0;
    }

    public String directory() {
        return _directory;
    }

    public String path() {
        return _path;
    }

    public Save save() {
        return _save;
    }

    public boolean decompress() {
        return _decompress;
    }

}
