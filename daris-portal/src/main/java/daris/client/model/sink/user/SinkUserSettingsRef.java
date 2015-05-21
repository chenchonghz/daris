package daris.client.model.sink.user;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.client.model.sink.SinkRef;

public class SinkUserSettingsRef extends ObjectRef<SinkUserSettings> {

    public static final String SERVICE_NAME = "user.self.settings.get";

    private SinkRef _sink;

    public SinkUserSettingsRef(SinkRef sink) {
        _sink = sink;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("app", SinkUserSettings.appFromSink(_sink));
    }

    @Override
    protected String resolveServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected SinkUserSettings instantiate(XmlElement xe) throws Throwable {
        return new SinkUserSettings(_sink, xe.element("settings"));
    }

    @Override
    public String referentTypeName() {
        return "sink user settings";
    }

    @Override
    public String idToString() {
        return _sink.url();
    }

}
