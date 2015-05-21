package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class SinkRef extends ObjectRef<Sink> {

    public static final String SERVICE_NAME = "sink.describe";

    private String _name;

    private Sink.Type _type;

    public SinkRef(Sink sink) {
        super(sink);
        _name = sink.name();
        _type = sink.type();
    }

    public SinkRef(Sink.Type type, String name) {
        _type = type;
        _name = name;
    }

    public String name() {
        return _name;
    }

    public Sink.Type type() {
        return _type;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("name", _name);
    }

    @Override
    protected String resolveServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Sink instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            XmlElement se = xe.element("sink");
            if (se != null) {
                Sink sink = Sink.instantiate(se);
                _type = sink.type();
                return sink;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "sink";
    }

    @Override
    public String idToString() {
        return _name;
    }

    public String url() {
        return Sink.URL_PREFIX + _name;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof SinkRef) {
            return _name.equals(((SinkRef) o).name());
        }
        return false;
    }

}
