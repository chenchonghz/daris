package daris.client.model.sink.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sink.SinkRef;
import daris.client.model.sink.user.SinkUserSettings;

public class SinkUserSelfSettingsGet extends ObjectMessage<SinkUserSettings> {

    public static final String SERVICE_NAME = "user.self.settings.get";

    private SinkRef _sink;

    public SinkUserSelfSettingsGet(SinkRef sink) {
        _sink = sink;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("app", SinkUserSettings.appFromSink(_sink));
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected SinkUserSettings instantiate(XmlElement xe) throws Throwable {
        return new SinkUserSettings(_sink, xe.element("settings"));
    }

    @Override
    protected String objectTypeName() {
        return "sink user settings";
    }

    @Override
    protected String idToString() {
        return _sink.url();
    }

}
