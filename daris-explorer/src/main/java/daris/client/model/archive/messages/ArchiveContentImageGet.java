package daris.client.model.archive.messages;

import arc.mf.client.ServerClient.Output;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlWriterNe;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;
import daris.client.util.ServiceTask;

public class ArchiveContentImageGet extends ServiceTask<ImageEntry> {

    public static interface ResponseHandler {
        void responded(ImageEntry e, Output output);
    }

    private ArchiveEntryCollectionRef _arc;
    private int _idx;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc, int idx) {
        super("daris.archive.content.image.get");
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
        // convert the image to browser supported png format.
        w.add("auto-convert", true);
        // _idx starts from 1.
        w.add("idx", _idx);
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
