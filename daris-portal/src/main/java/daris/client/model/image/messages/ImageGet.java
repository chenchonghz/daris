package daris.client.model.image.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.image.RemoteImage;

public abstract class ImageGet<T extends RemoteImage> extends ObjectMessage<T> {

    public static final String SERVICE_NAME = "nig.image.get";

    private String _assetId;
    private int _index;
    private boolean _lossless;

    protected ImageGet(String assetId, int index, boolean lossless) {
        _assetId = assetId;
        _index = index;
        _lossless = lossless;
    }

    protected String assetId() {
        return _assetId;
    }

    protected int index() {
        return _index;
    }

    protected boolean lossless() {
        return _lossless;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("id", _assetId);
        w.add("idx", _index);
        w.add("lossless", _lossless);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String idToString() {
        return "asset_id=" + _assetId + ", index=" + _index + "";
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

    @Override
    protected void process(T ri, List<Output> outputs) throws Throwable {
        if (ri != null) {
            if (outputs != null) {
                ri.setUrl(outputs.get(0).url());
            }
        }
    }

}
