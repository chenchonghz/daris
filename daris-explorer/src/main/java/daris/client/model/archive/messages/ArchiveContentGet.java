package daris.client.model.archive.messages;

import java.util.List;

import arc.mf.client.ServerClient.Output;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.util.OutputHandler;

public class ArchiveContentGet extends ObjectMessage<ArchiveEntry> {

    private ArchiveEntryCollectionRef _arc;
    private int _idx;
    private OutputHandler _oh;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentGet(ArchiveEntryCollectionRef arc, int idx,
            OutputHandler oh) {
        _arc = arc;
        _idx = idx;
        _oh = oh;
    }

    public void setOutputHandler(OutputHandler oh) {
        _oh = oh;
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        if (_arc.assetId() != null) {
            w.add("id", _arc.assetId());
        } else {
            w.add("cid", _arc.citeableId());
        }
        // _idx starts from 1.
        w.add("idx", _idx);
    }

    @Override
    protected String messageServiceName() {
        return "daris.archive.content.get";
    }

    @Override
    protected ArchiveEntry instantiate(XmlDoc.Element xe) throws Throwable {
        XmlDoc.Element ee = xe.element("entry");
        if (ee != null) {
            return new ArchiveEntry(ee);
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return String.valueOf(_idx);
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

    @Override
    protected void process(ArchiveEntry o, List<Output> outputs)
            throws Throwable {
        Output output = outputs.get(0);
        if (_oh != null) {
            _oh.handleOutput(output);
        }
    }

}
