package daris.client.model.dicom.messages;

import daris.client.model.object.DObjectRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class CollectionDicomDatasetCount extends ObjectMessage<Integer> {

    public static final String SERVICE_NAME = "daris.collection.dicom.dataset.count";

    private String _cid;

    public CollectionDicomDatasetCount(String cid) {

        _cid = cid;
    }

    public CollectionDicomDatasetCount(DObjectRef root) {

        this(root.id());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_cid != null) {
            w.add("cid", _cid);
        }
    }

    @Override
    protected String messageServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected Integer instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            return xe.intValue("value", 0);
        }
        return 0;
    }

    @Override
    protected String objectTypeName() {

        return null;
    }

    @Override
    protected String idToString() {

        return _cid;
    }

}
