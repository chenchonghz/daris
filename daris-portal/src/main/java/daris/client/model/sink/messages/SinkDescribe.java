package daris.client.model.sink.messages;

import java.util.ArrayList;
import java.util.List;

import daris.client.model.sink.Sink;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class SinkDescribe extends ObjectMessage<List<Sink>> {

    public static final String SERVICE_NAME = "sink.describe";

    @Override
    protected void messageServiceArgs(XmlWriter w) {

    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected List<Sink> instantiate(XmlElement xe) throws Throwable {

        List<XmlElement> ses = xe.elements("sink");
        if (ses != null && !ses.isEmpty()) {
            List<Sink> sinks = new ArrayList<Sink>(ses.size());
            for (XmlElement se : ses) {
                sinks.add(Sink.instantiate(se));
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
