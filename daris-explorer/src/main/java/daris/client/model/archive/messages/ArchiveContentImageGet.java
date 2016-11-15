package daris.client.model.archive.messages;

import arc.mf.client.ServerClient.Output;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlWriterNe;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;
import daris.client.util.ServiceTask;

public class ArchiveContentImageGet extends ServiceTask<ImageEntry> {

    public static interface ResponseHandler {
        void responded(ImageEntry e, Output output);
    }

    private ArchiveEntryCollectionRef _arc;
    private ArchiveEntry _entry;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry) {
        super("daris.archive.content.image.get");
        _arc = arc;
        _entry = entry;
    }

    @Override
    public void setServiceArgs(XmlWriterNe w) {
        if (_arc.assetId() != null) {
            w.add("id", _arc.assetId());
        } else {
            w.add("cid", _arc.citeableId());
        }
        w.add("idx", _entry.ordinal());
        if (_entry.name() != null) {
            w.add("name", _entry.name());
        }
        w.add("lossless", false);
    }

    @Override
    public ImageEntry instantiate(Element xe) {
        try {
            XmlDoc.Element ee = xe.element("entry");
            if (ee != null) {
                return new ImageEntry(ee);
            } else {
                return null;
            }
        } catch (Throwable e) {
            UnhandledException.report("Instantiating archive entry", e);
            return null;
        }
    }

}
