package daris.client.model.sink.messages;

import java.util.ArrayList;
import java.util.List;

import daris.client.model.sink.Sink;
import daris.client.model.sink.SinkRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class SinkList extends ObjectMessage<List<SinkRef>> {

    public static final String SERVICE_NAME = "sink.describe";

    @Override
    protected void messageServiceArgs(XmlWriter w) {

    }

    @Override
    protected String messageServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected List<SinkRef> instantiate(XmlElement xe) throws Throwable {
        List<XmlElement> ses = xe.elements("sink");
        if (ses != null && !ses.isEmpty()) {
            List<SinkRef> sinks = new ArrayList<SinkRef>(ses.size());
            for (XmlElement se : ses) {
                sinks.add(new SinkRef(Sink.instantiate(se)));
            }
            return sinks;
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "sinks";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
