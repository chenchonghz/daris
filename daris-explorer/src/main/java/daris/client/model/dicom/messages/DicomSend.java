package daris.client.model.dicom.messages;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.BackgroundObjectMessage;
import arc.mf.object.BackgroundObjectMessageResponse;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectRef;
import daris.client.model.service.BackgroundServiceManager;

public class DicomSend extends BackgroundObjectMessage {

    public static final String SERVICE_NAME = "daris.dicom.send";

    public static class OverriddenElement {
        private int _group;
        private int _element;
        private boolean _anonymize;
        private String _value;

        public OverriddenElement(int group, int element, boolean anonymize,
                String value) {
            _group = group;
            _element = element;
            _anonymize = anonymize;
            _value = value;
        }

        public int group() {
            return _group;
        }

        public int element() {
            return _element;
        }

        public boolean anonymize() {
            return _anonymize;
        }

        public String value() {
            return _value;
        }

        public String tag() {
            return String.format("%04x%04x", _group, _element);
        }

        public void save(XmlWriterNe w) {
            String gggg = String.format("%04x", _group);
            String eeee = String.format("%04x", _element);
            if (_anonymize) {
                w.push("element", new String[] { "group", gggg, "element", eeee,
                        "anonymize", Boolean.toString(true) });
            } else {
                w.push("element",
                        new String[] { "group", gggg, "element", eeee });
                w.add("value", _value);
            }
            w.pop();
        }
    }

    private String _callingAETitle;
    private String _calledAETitle;
    private String _calledAEHost;
    private int _calledAEPort;
    private String _assetId;
    private String _citeableId;
    private String _where;
    private Map<String, OverriddenElement> _oes;

    public DicomSend(String cid) {
        _citeableId = cid;
    }

    public DicomSend(DObjectRef o) {
        this(o.citeableId());
    }

    public void setCiteableId(String cid) {
        _citeableId = cid;
    }

    public void setAssetId(String id) {
        _assetId = id;
    }

    public void setQuery(String where) {
        _where = where;
    }

    public void setCallingAETitle(String aet) {
        _callingAETitle = aet;
    }

    public void setCalledAETitle(String aet) {
        _calledAETitle = aet;
    }

    public void setCalledAEHost(String host) {
        _calledAEHost = host;
    }

    public void setCalledAEPort(int port) {
        _calledAEPort = port;
    }

    public void setOverriddenElement(int group, int element, boolean anonymize,
            String value) {
        if (_oes == null) {
            _oes = new TreeMap<String, OverriddenElement>();
        }
        OverriddenElement oe = new OverriddenElement(group, element, anonymize,
                value);
        _oes.put(oe.tag(), oe);
    }

    @Override
    protected String idToString() {
        return _assetId != null ? _assetId : _citeableId;
    }

    @Override
    public void send(BackgroundObjectMessageResponse rh) {
        StringBuilder sb = new StringBuilder("Sending DICOM ");
        if (_citeableId != null) {
            sb.append(CiteableIdUtils.getTypeNameFromCID(_citeableId))
                    .append(" ").append(_citeableId);
        } else if (_assetId != null) {
            sb.append("asset ").append(_assetId);
        } else {
            sb.append("data");
        }
        sb.append(" to ").append(_calledAETitle).append("(")
                .append(_calledAEHost).append(":").append(_calledAEPort)
                .append(")");
        setDescription(sb.toString());
        super.send(new BackgroundObjectMessageResponse() {

            @Override
            public void responded(Long id) {
                BackgroundServiceManager.get().addService(id);
                if (rh != null) {
                    rh.responded(id);
                }
            }
        });
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        if (_where != null) {
            w.add("where", _where);
        }
        if (_assetId != null) {
            w.add("id", _assetId);
        }
        if (_citeableId != null) {
            w.add("cid", _citeableId);
            setDescription(
                    "Sending " + CiteableIdUtils.getTypeNameFromCID(_citeableId)
                            + " " + _citeableId + " to " + _calledAETitle);
        }
        w.push("calling-ae");
        w.add("title", _callingAETitle);
        w.pop();
        w.push("called-ae");
        w.add("title", _calledAETitle);
        w.add("host", _calledAEHost);
        w.add("port", _calledAEPort);
        w.pop();
        if (_oes != null && !_oes.isEmpty()) {
            w.push("override");
            Collection<OverriddenElement> oes = _oes.values();
            for (OverriddenElement oe : oes) {
                oe.save(w);
            }
            w.pop();
        }
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String objectTypeName() {
        return _citeableId == null ? null
                : CiteableIdUtils.getTypeNameFromCID(_citeableId);
    }

}
