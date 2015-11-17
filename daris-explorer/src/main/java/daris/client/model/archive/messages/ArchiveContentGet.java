package daris.client.model.archive.messages;

import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlWriterNe;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.util.ServiceTask;

public class ArchiveContentGet extends ServiceTask<ArchiveEntry> {

    private ArchiveEntryCollectionRef _arc;
    private int _idx;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentGet(ArchiveEntryCollectionRef arc, int idx) {
        super("daris.archive.content.get");
        _arc = arc;
        _idx = idx;
    }

    @Override
    public void setServiceArgs(XmlWriterNe w) {
        if (_arc.assetId() != null) {
            w.add("id", _arc.assetId());
        } else {
            w.add("cid", _arc.citeableId());
        }
        // _idx starts from 1.
        w.add("idx", _idx);
    }

    @Override
    public ArchiveEntry instantiate(Element xe) {
        try {
            XmlDoc.Element ee = xe.element("entry");
            if (ee != null) {
                return new ArchiveEntry(ee);
            } else {
                return null;
            }
        } catch (Throwable e) {
            UnhandledException.report("Instantiating archive entry", e);
            return null;
        }
    }

}
