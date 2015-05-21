package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.object.DObjectRef;

public class DataSetProcessedDestroyableExists extends ObjectMessage<Boolean> {

    public static String SERVICE_NAME = "om.pssd.dataset.processed.destroyable.exists";

    private String _cid;

    public DataSetProcessedDestroyableExists(DObjectRef o) {
        _cid = o.id();
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("cid", _cid);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {
        return xe.booleanValue("exists", false);
    }

    @Override
    protected String objectTypeName() {
        return IDUtil.typeNameFromId(_cid);
    }

    @Override
    protected String idToString() {
        return _cid;
    }

}
