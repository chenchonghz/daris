package daris.client.model.archive.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;

public class ArchiveContentImageGet extends ObjectMessage<ImageEntry> {

    private ArchiveEntryCollectionRef _arc;
    private int _idx;

    /**
     * 
     * @param arc
     * @param idx
     *            starts from one.
     */
    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc, int idx) {
        _arc = arc;
        _idx = idx;
    }
    
    public ArchiveContentImageGet(ArchiveEntryCollectionRef arc,
            ArchiveEntry entry){
        this(arc, entry.ordinal());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
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
    protected ImageEntry instantiate(XmlElement xe) throws Throwable {
        XmlElement ee = xe.element("entry");
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
        ae.setOutputUrl(output.url());
    }

}
