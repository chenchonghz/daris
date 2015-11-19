package daris.client.model.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.desktop.server.Session;
import arc.xml.XmlDoc;
import daris.client.model.method.MethodRef;
import daris.client.model.method.MethodStep;
import daris.client.model.mime.MimeTypes;
import daris.client.model.object.DObject;

public abstract class DataSet extends DObject {

    public static class Transform {

        public static class Software {

            public static class Command {

                private String _name;
                private Map<String, String> _args;

                public Command(XmlDoc.Element ce) throws Throwable {
                    _name = ce.value("name");
                    List<XmlDoc.Element> aes = ce.elements("argument");
                    if (aes != null) {
                        _args = new HashMap<String, String>();
                        for (XmlDoc.Element ae : aes) {
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

                public void describe(XmlWriterNe w) {
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

            public Software(XmlDoc.Element se) throws Throwable {
                _name = se.value("name");
                _version = se.value("version");
                List<XmlDoc.Element> ces = se.elements("command");
                if (ces != null) {
                    _commands = new ArrayList<Command>();
                    for (XmlDoc.Element ce : ces) {
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

            public void describe(XmlWriterNe w) {
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

        public Transform(XmlDoc.Element te) throws Throwable {
            _mid = te.value("mid");
            _tuid = te.longValue("tuid", 0);
            List<XmlDoc.Element> ses = te.elements("software");
            if (ses != null) {
                _software = new ArrayList<Software>();
                for (XmlDoc.Element se : ses) {
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

        public void describe(XmlWriterNe w) {
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

    private SourceType _source;
    private Transform _transform;
    private String _fileName;
    private String _dataSetVid;

    protected DataSet(XmlDoc.Element oe) throws Throwable {
        super(oe);
        _source = SourceType.fromString(oe.stringValue("source/type"),
                SourceType.DERIVATION);
        if (oe.elementExists("transform")) {
            _transform = new Transform(oe.element("transform"));
        }
        _fileName = oe.value("filename");
        _dataSetVid = oe.value("vid");
    }

    public Transform transform() {
        return _transform;
    }

    public SourceType sourceType() {
        return _source;
    }

    public String fileName() {
        return _fileName;
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.DATASET;
    }

    public String dataSetVid() {
        return _dataSetVid;
    }

    public boolean isNiftiSeries() {
        return hasContent() && MimeTypes.NIFTI_SERIES.equals(mimeType());
    }

    public boolean isDicomSeries() {
        return hasContent() && MimeTypes.DICOM_SERIES.equals(mimeType());
    }

    public String niftiViewerUrl() {
        if (!isNiftiSeries()) {
            return null;
        }
        String baseUrl = Session.baseUrl();
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("/daris/nifti.mfjp?_skey=");
        sb.append(Session.sessionId());
        sb.append("&module=view&id=");
        sb.append(assetId());
        return sb.toString();
    }

    public String dicomViewerUrl() {
        if (!isDicomSeries()) {
            return null;
        }
        String baseUrl = Session.baseUrl();
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("/daris/dicom.mfjp?_skey=");
        sb.append(Session.sessionId());
        sb.append("&module=view&id=");
        sb.append(assetId());
        return sb.toString();
    }

    public String contentDownloadUrl() {
        if (!hasContent() || !Session.haveSession()) {
            return null;
        }
        String baseUrl = Session.baseUrl();
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("/mflux/content.mfjp?_skey=");
        sb.append(Session.sessionId());
        sb.append("&disposition=attachment&id=");
        sb.append(assetId());
        if (fileName() != null) {
            sb.append("&filename=" + fileName());
        } else {
            sb.append("&filename=" + citeableId());
            if (MimeTypes.NIFTI_SERIES.equals(mimeType())) {
                sb.append(".nii");
            }
            if (content().extension != null) {
                sb.append("." + content().extension);
            }
        }
        return sb.toString();
    }

    public static DataSet create(XmlDoc.Element oe) throws Throwable {
        SourceType sourceType = SourceType.fromString(
                oe.stringValue("source/type"), SourceType.DERIVATION);
        if (sourceType == SourceType.DERIVATION) {
            return new DerivedDataSet(oe);
        } else {
            return new PrimaryDataSet(oe);
        }
    }

    public abstract MethodRef method();

    public abstract MethodStep step();

}
