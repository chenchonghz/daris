package daris.client.model.archive.messages;

import arc.mf.client.ServerClient.Output;
import arc.mf.client.xml.XmlWriterNe;
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
    private boolean _lossless;
    private Integer _size;

    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry) {
        this(arc, entry, false, null);
    }

    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry, boolean lossless, Integer size) {
        super("daris.archive.content.image.get");
        _arc = arc;
        _entry = entry;
        _lossless = lossless;
        _size = size;
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
        w.add("lossless", _lossless);
        if (_size != null) {
            w.add("size", _size);
        }
    }

    @Override
    public ImageEntry instantiate(Element xe) {
        ImageEntry ie = new ImageEntry(_entry, _lossless);
        return ie;
    }

}
