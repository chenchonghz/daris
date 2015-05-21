package daris.client.model.sink;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class SinkSetRef extends ObjectRef<List<Sink>> {

    public static final String SERVICE_NAME = "sink.describe";

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {

    }

    @Override
    protected String resolveServiceName() {
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
    public String referentTypeName() {
        return "sinks";
    }

    @Override
    public String idToString() {
        return null;
    }

}
