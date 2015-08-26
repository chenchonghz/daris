package daris.client.model.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.RemoteServer;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObject;
import daris.client.model.object.DataContent;
import daris.client.model.object.MimeTypes;

public abstract class DataSet extends DObject {

    public static class Transform {

        public static class Software {

            public static class Command {

                private String _name;
                private Map<String, String> _args;

                public Command(XmlElement ce) {
                    _name = ce.value("name");
                    List<XmlElement> aes = ce.elements("argument");
                    if (aes != null) {
                        _args = new HashMap<String, String>();
                        for (XmlElement ae : aes) {
                            _args.put(ae.value("name"), ae.value("value"));
                        }
                    }
                }

                public Map<String, String> args() {
                    return _args;
                }

                public boolean hasArgs() {
                    if (_args == null) {
                        return false;
                    }
                    if (_args.isEmpty()) {
                        return false;
                    }
                    return true;
                }

                public String name() {
                    return _name;
                }

                public void describe(XmlWriter w) {
                    w.push("command");
                    w.add("name", _name);
                    if (hasArgs()) {
                        for (String aname : _args.keySet()) {
                            w.push("argument");
                            w.add("name", aname);
                            String avalue = _args.get(aname);
                            if (avalue != null) {
                                w.add("value", avalue);
                            }
                            w.pop();
                        }
                    }
                    w.pop();
                }

                public String argValue(String argName) {
                    if (!hasArgs()) {
                        return null;
                    }
                    return _args.get(argName);
                }
            }

            private String _name;
            private String _version;
            private List<Command> _commands;

            public Software(XmlElement se) {
                _name = se.value("name");
                _version = se.value("version");
                List<XmlElement> ces = se.elements("command");
                if (ces != null) {
                    _commands = new ArrayList<Command>();
                    for (XmlElement ce : ces) {
                        _commands.add(new Command(ce));
                    }
                }
            }

            public String name() {
                return _name;
            }

            public String version() {
                return _version;
            }

            public List<Command> commands() {
                return _commands;
            }

            public void describe(XmlWriter w) {
                w.push("software");
                w.add("name", name());
                if (version() != null) {
                    w.add("version", version());
                }
                if (_commands != null) {
                    for (Command cmd : _commands) {
                        cmd.describe(w);
                    }
                }
                w.pop();
            }
        }

        private String _mid;
        private long _tuid;
        private List<Software> _software;
        private String _notes;

        public Transform(XmlElement te) throws Throwable {
            _mid = te.value("mid");
            _tuid = te.longValue("tuid", 0);
            List<XmlElement> ses = te.elements("software");
            if (ses != null) {
                _software = new ArrayList<Software>();
                for (XmlElement se : ses) {
                    _software.add(new Software(se));
                }
            }
            _notes = te.value("notes");
        }

        public String mid() {
            return _mid;
        }

        public long tuid() {
            return _tuid;
        }

        public List<Software> software() {
            return _software;
        }

        public String notes() {
            return _notes;
        }

        public void describe(XmlWriter w) {
            w.push("transform");
            if (mid() != null) {
                w.add("mid", mid());
            }
            if (tuid() > 0) {
                w.add("tuid", tuid());
            }
            if (software() != null) {
                for (Software s : software()) {
                    s.describe(w);
                }
            }
            if (notes() != null) {
                w.add("notes", notes());
            }
            w.pop();
        }

    }

    private SourceType _sourceType;
    private String _mimeType;
    private String _contentVid;
    private Transform _transform;
    private DataContent _data;

    protected DataSet(XmlElement de) throws Throwable {

        super(de);
        try {
            _sourceType = SourceType.parse(de.stringValue("source/type",
                    SourceType.derivation.toString()));
        } catch (Throwable e) {
            _sourceType = SourceType.derivation;
        }
        _contentVid = de.value("vid");
        _mimeType = de.value("type");
        XmlElement ce = de.element("data");
        if (ce != null) {
            _data = new DataContent(ce);
        }
        XmlElement te = de.element("transform");
        if (te != null) {
            _transform = new Transform(te);
        }
    }

    protected DataSet(String id, String proute, String name,
            String description, boolean editable, int version, boolean isleaf) {
        super(id, proute, name, description, editable, version, isleaf);
    }

    public Transform transform() {
        return _transform;
    }

    public DataContent data() {

        return _data;
    }

    public SourceType sourceType() {

        return _sourceType;
    }

    public String mimeType() {

        return _mimeType;
    }

    public String contentVid() {

        return _contentVid;
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.dataset;
    }

    public static DataSet create(XmlElement oe) throws Throwable {
        String sourceType = oe.value("source/type");
        if (sourceType.equals("derivation")) {
            String mimeType = oe.value("type");
            if (mimeType != null) {
                if (mimeType.equals("dicom/series")) {
                    return new DicomDataSet(oe);
                }
            }
            return new DerivedDataSet(oe);
        } else if (sourceType.equals("primary")) {
            return new PrimaryDataSet(oe);
        }
        throw new IllegalArgumentException(
                "Failed to instantiate data set from XML: " + oe);
    }

    public String contentDownloadUrl() {
        if (data() == null && !RemoteServer.haveSession()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(com.google.gwt.user.client.Window.Location.getProtocol());
        sb.append("//");
        sb.append(com.google.gwt.user.client.Window.Location.getHost());
        sb.append("/mflux/content.mfjp?_skey=");
        sb.append(RemoteServer.sessionId());
        sb.append("&disposition=attachment&id=");
        sb.append(assetId());
        if (fileName() != null) {
            sb.append("&filename=" + fileName());
        } else {
            sb.append("&filename=" + id());
            if (MimeTypes.NIFTI_SERIES.equals(mimeType())) {
                sb.append(".nii");
            }
            if (data().extension() != null) {
                sb.append("." + data().extension());
            }
        }
        return sb.toString();
    }
}
