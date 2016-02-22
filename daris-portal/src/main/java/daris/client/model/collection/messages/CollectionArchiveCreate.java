package daris.client.model.collection.messages;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.collection.archive.ArchiveFormat;
import daris.client.model.collection.archive.ArchiveOptions;
import daris.client.model.collection.archive.Parts;
import daris.client.model.object.DObjectRef;
import daris.client.model.transcode.Transcode;

public class CollectionArchiveCreate extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "daris.collection.archive.create";

    private DObjectRef _o;
    private String _where;
    private Parts _parts;
    private boolean _includeAttachments;
    private boolean _decompress;
    private ArchiveFormat _format;
    private Map<String, Transcode> _transcodes;

    public CollectionArchiveCreate(DObjectRef o,
            ArchiveOptions archiveOptions) {
        _o = o;
        _parts = archiveOptions.parts();
        _includeAttachments = archiveOptions.includeAttachments();
        _decompress = archiveOptions.decompress();
        _format = archiveOptions.archiveFormat();
        if (archiveOptions.hasTranscodes()) {
            Map<String, String> ts = archiveOptions.transcodes();
            for (String k : ts.keySet()) {
                addTranscode(k, ts.get(k));
            }
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("cid", _o.id());
        if (_where != null) {
            w.add("where", _where);
        }
        if (_parts != null) {
            w.add("parts", _parts);
        }
        w.add("include-attachments", _includeAttachments);
        w.add("decompress", _decompress);
        w.add("format", _format.toString());
        if (_transcodes != null && !_transcodes.isEmpty()) {
            Collection<Transcode> ts = _transcodes.values();
            for (Transcode t : ts) {
                w.push("transcode");
                w.add("from", t.from());
                w.add("to", t.to());
                w.pop();
            }
        }
    }

    public void setParts(Parts parts) {
        _parts = parts;
    }

    public void setIncludeAttachments(boolean includeAttachments) {
        _includeAttachments = includeAttachments;
    }

    public void setDecompress(boolean decompress) {
        _decompress = decompress;
    }

    public void setArchiveFormat(ArchiveFormat format) {
        _format = format;
    }

    public void addTranscode(String from, String to) {
        if (_transcodes == null) {
            _transcodes = new TreeMap<String, Transcode>();
        }
        _transcodes.put(from, new Transcode(from, to));
    }

    public void removeTranscode(String from) {
        if (_transcodes != null && _transcodes.containsKey(from)) {
            _transcodes.remove(from);
        }
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return _o.referentTypeName();
    }

    @Override
    protected String idToString() {
        return _o.id();
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

    @Override
    protected void process(Null o, List<Output> outputs) {
        if (outputs != null) {
            for (Output output : outputs) {
                String filename = _o.referentTypeName() + "_" + _o.id() + "."
                        + _format.name();
                output.download(filename);
            }
        }
    }

}
