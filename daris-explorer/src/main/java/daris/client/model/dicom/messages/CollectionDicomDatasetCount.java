package daris.client.model.dicom.messages;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc.Element;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectRef;

public class CollectionDicomDatasetCount extends ObjectMessage<Integer> {

    public static final String SERVICE_NAME = "daris.collection.dicom.dataset.count";

    private String _cid;

    public CollectionDicomDatasetCount(String citeableId) {
        _cid = citeableId;
    }

    public CollectionDicomDatasetCount(DObjectRef root) {
        this(root.citeableId());
    }

    @Override
    protected String idToString() {
        return _cid;
    }

    @Override
    protected Integer instantiate(Element xe) throws Throwable {
        return xe.intValue("value", 0);
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        w.add("cid", _cid);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String objectTypeName() {
        return CiteableIdUtils.getTypeNameFromCID(_cid);
    }

}
