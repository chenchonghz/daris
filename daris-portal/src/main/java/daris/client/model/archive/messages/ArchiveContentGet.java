package daris.client.model.archive.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.util.DownloadUtil;

public class ArchiveContentGet extends ObjectMessage<ArchiveEntry> {

    private ArchiveEntryCollectionRef _arc;
    private ArchiveEntry _entry;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry) {
        _arc = arc;
        _entry = entry;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_arc.assetId() != null) {
            w.add("id", _arc.assetId());
        } else {
            w.add("cid", _arc.citeableId());
        }
        // idx starts from 1.
        w.add("idx", _entry.ordinal());
    }

    @Override
    protected String messageServiceName() {
        return "daris.archive.content.get";
    }

    @Override
    protected ArchiveEntry instantiate(XmlElement xe) throws Throwable {
        return _entry;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return String.valueOf(_entry.ordinal());
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

    @Override
    protected void process(ArchiveEntry entry, List<Output> outputs)
            throws Throwable {
        Output output = outputs.get(0);
        DownloadUtil.download(output, entry.fileName());
    }
}
