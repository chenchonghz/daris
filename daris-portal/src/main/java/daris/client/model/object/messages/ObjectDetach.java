package daris.client.model.object.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;

public class ObjectDetach extends ObjectMessage<Null> {

    private String _cid;
    private Collection<String> _aids;
    private boolean _detachAll;

    public ObjectDetach(String cid, List<Attachment> attachments, boolean detachAll) {

        _cid = cid;
        if (attachments != null && !attachments.isEmpty()) {
            _aids = new ArrayList<String>(attachments.size());
            for (Attachment a : attachments) {
                _aids.add(a.assetId());
            }
        }
        _detachAll = detachAll;
    }

    public ObjectDetach(String cid) {

        this(cid, null, true);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("cid", _cid);
        if (_aids != null) {
            for (String aid : _aids) {
                w.add("aid", aid);
            }
        }
        if(_detachAll){
            w.add("detach-all", _detachAll);
        }
    }

    @Override
    protected String messageServiceName() {

        return "daris.object.detach";
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {

        return new Null();
    }

    @Override
    protected String objectTypeName() {

        return "attachment";
    }

    @Override
    protected String idToString() {

        return _cid;
    }

}
