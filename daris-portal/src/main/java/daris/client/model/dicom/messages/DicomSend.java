package daris.client.model.dicom.messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlWriter;
import arc.mf.object.BackgroundObjectMessage;
import daris.client.model.object.DObjectRef;

public class DicomSend extends BackgroundObjectMessage {

    public static enum ElementName {
        PATIENT_NAME("patient.name", "00100010"), PATIENT_ID("patient.id",
                "00100020"), STUDY_ID("study.id",
                        "00200010"), PERFORMING_PHYSICIAN_NAME(
                                "performing.physician.name",
                                "00081050"), REFERRING_PHYSICIAN_NAME(
                                        "referring.physician.name",
                                        "00080090"), REFERRING_PHYSICIAN_PHONE(
                                                "referring.physician.phone",
                                                "00080094");
        private String _stringValue;
        private String _tag;

        ElementName(String stringValue, String tag) {
            _stringValue = stringValue;
            _tag = tag;
        }

        @Override
        public final String toString() {
            return _stringValue;
        }

        public String tag() {
            return _tag;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static final String[] stringValues() {
            ElementName[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].stringValue();
            }
            return svs;
        }

        public static ElementName fromString(String s) {
            if (s != null) {
                ElementName[] vs = values();
                for (ElementName v : vs) {
                    if (s.equalsIgnoreCase(v.stringValue())) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    public static enum ValueReference {
        SUBJECT_CID("subject.cid"), STUDY_CID("study.cid"), PATIENT_NAME(
                "patient.name"), PATIENT_ID("patient.id");

        private String _stringValue;

        ValueReference(String stringValue) {
            _stringValue = stringValue;
        }

        @Override
        public final String toString() {
            return _stringValue;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static final String[] stringValues() {
            ValueReference[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].stringValue();
            }
            return svs;
        }

        public static ValueReference fromString(String s) {
            if (s != null) {
                ValueReference[] vs = values();
                for (ValueReference v : vs) {
                    if (s.equalsIgnoreCase(v.stringValue())) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    public static class DicomElement {
        private ElementName _name;
        private String _tag;
        private String _value;
        private ValueReference _valueReference;
        private boolean _anonymize;

        public DicomElement(String tag, String value) {
            _tag = tag;
            _name = ElementName.fromString(tag);
            _value = value;
            _valueReference = null;
            _anonymize = false;
        }

        public DicomElement(String tag, ValueReference valueReference) {
            _tag = tag;
            _name = ElementName.fromString(tag);
            _value = null;
            _valueReference = valueReference;
            _anonymize = false;
        }

        public DicomElement(String tag) {
            _tag = tag;
            _name = ElementName.fromString(tag);
            _value = null;
            _valueReference = null;
            _anonymize = true;
        }

        public DicomElement(ElementName name, String value) {
            _tag = name.tag();
            _name = name;
            _value = value;
            _valueReference = null;
            _anonymize = false;
        }

        public DicomElement(ElementName name, ValueReference valueReference) {
            _tag = name.tag();
            _name = name;
            _value = null;
            _valueReference = valueReference;
            _anonymize = false;
        }

        public DicomElement(ElementName name) {
            _tag = name.tag();
            _name = name;
            _value = null;
            _valueReference = null;
            _anonymize = true;
        }

        public String tag() {
            return _tag;
        }

        public ElementName name() {
            return _name;
        }

        public String value() {
            return _value;
        }

        public ValueReference valueReference() {
            return _valueReference;
        }

        public boolean anonymize() {
            return _anonymize;
        }
    }

    private String _cid;
    private String _where;
    private String _callingAET;
    private String _calledHost;
    private int _calledPort;
    private String _calledAET;
    private Map<String, DicomElement> _elements;

    public DicomSend(DObjectRef o) {
        this(o.id(), null);
    }

    public DicomSend(String where) {
        this(null, where);
    }

    protected DicomSend(String cid, String where) {
        _cid = cid;
        _where = where;
        _callingAET = null;
        _calledHost = null;
        _calledPort = 104;
        _calledAET = null;
        _elements = new HashMap<String, DicomElement>();
    }

    public void setCallingAETitle(String title) {
        _callingAET = title;
    }

    public void setCalledAEHost(String host) {
        _calledHost = host;
    }

    public void setCalledAEPort(int port) {
        _calledPort = port;
    }

    public void setCalledAETitle(String title) {
        _calledAET = title;
    }

    public void anonymizeElement(ElementName name) {
        _elements.put(name.tag(), new DicomElement(name));
    }

    public void anonymizeElement(String tag) {
        _elements.put(tag, new DicomElement(tag));
    }

    public void setElement(ElementName name, String value) {
        _elements.put(name.tag(), new DicomElement(name, value));
    }

    public void setElement(ElementName name, ValueReference valueReference) {
        _elements.put(name.tag(), new DicomElement(name, valueReference));
    }

    public void setElement(String tag, String value) {
        _elements.put(tag, new DicomElement(tag, value));
    }

    public void setElement(String tag, ValueReference valueReference) {
        _elements.put(tag, new DicomElement(tag, valueReference));
    }

    public void removeElement(ElementName name) {
        _elements.remove(name.tag());
    }

    public void removeElement(String tag) {
        if (_elements.containsKey(tag)) {
            _elements.remove(tag);
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_cid != null) {
            w.add("cid", _cid);
        }
        if (_where != null) {
            w.add("where", _where);
        }
        w.push("calling-ae");
        w.add("title", _callingAET);
        w.pop();
        w.push("called-ae");
        w.add("host", _calledHost);
        w.add("port", _calledPort);
        w.add("title", _calledAET);
        w.pop();
        if (_elements != null && !_elements.isEmpty()) {
            w.push("override");
            for (String tag : _elements.keySet()) {
                DicomElement de = _elements.get(tag);
                if (de.anonymize()) {
                    w.push("element", new String[] { "tag", tag, "anonymize",
                            Boolean.toString(de.anonymize()) });
                } else {
                    w.push("element", new String[] { "tag", tag });
                    if (de.value() != null) {
                        w.add("value", de.value());
                    }
                    if (de.valueReference() != null) {
                        w.add("value-reference", de.valueReference());
                    }
                }
                w.pop();
            }
            w.pop();
        }
    }

    @Override
    protected String messageServiceName() {
        return "daris.dicom.send";
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return _cid;
    }

    public String cid() {
        return _cid;
    }

    public String where() {
        return _where;
    }

    public String calledAETitle() {
        return _calledAET;
    }

    public void setElements(List<DicomElement> des) {
        if (des != null) {
            for (DicomElement de : des) {
                _elements.put(de.tag(), de);
            }
        }

    }

}
