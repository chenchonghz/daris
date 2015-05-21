package daris.client.model.sink.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sink.user.SinkUserSettings;

public class SinkUserSelfSettingsSet extends ObjectMessage<Null> {

    public static String SERVICE_NAME = "user.self.settings.set";

    private SinkUserSettings _settings;

    public SinkUserSelfSettingsSet(SinkUserSettings settings) {
        _settings = settings;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        _settings.save(w);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return "sink user settings";
    }

    @Override
    protected String idToString() {
        return _settings.sink().url();
    }

}
