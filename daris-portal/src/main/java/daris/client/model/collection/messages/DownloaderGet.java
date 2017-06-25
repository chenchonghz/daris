package daris.client.model.collection.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.collection.download.DownloaderSettings;

public class DownloaderGet extends ObjectMessage<Null> {

    private DownloaderSettings _settings;

    public DownloaderGet(DownloaderSettings settings) {
        _settings = settings;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        _settings.save(w);
    }

    @Override
    protected String messageServiceName() {
        return "daris.downloader.get";
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return null;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return null;
    }

    @Override
    protected void process(Null o, List<Output> outputs) {
        Output output = outputs.get(0);
        output.download(_settings.targetPlatform().filename());
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

}
