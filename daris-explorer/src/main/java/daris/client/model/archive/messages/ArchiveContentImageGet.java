package daris.client.model.archive.messages;

import java.util.List;

import arc.mf.client.ServerClient.Output;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;
import daris.client.util.OutputHandler;

public class ArchiveContentImageGet extends ObjectMessage<ImageEntry> {

    private ArchiveEntryCollectionRef _arc;
    private int _idx;
    private OutputHandler _oh;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc, int idx,
            OutputHandler oh) {
        _arc = arc;
        _idx = idx;
        _oh = oh;
    }

    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry, OutputHandler oh) {
        this(arc, entry.ordinal(), oh);
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
        // convert the image to browser supported png format.
        w.add("auto-convert", true);
        // _idx starts from 1.
        w.add("idx", _idx);
    }

    @Override
    protected String messageServiceName() {
        return "daris.archive.content.image.get";
    }

    @Override
    protected ImageEntry instantiate(XmlDoc.Element xe) throws Throwable {
        XmlDoc.Element ee = xe.element("entry");
        if (ee != null) {
            return new ImageEntry(ee);
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
    protected void process(ImageEntry ae, List<Output> outputs)
            throws Throwable {
        Output output = outputs.get(0);
        if (_oh != null) {
            _oh.handleOutput(output);
        }
    }

}
