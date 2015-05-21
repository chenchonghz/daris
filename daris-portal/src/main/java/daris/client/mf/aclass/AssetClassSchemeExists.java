package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class AssetClassSchemeExists extends ObjectMessage<Boolean> {

    private AssetClassSchemeRef _scheme;

    public AssetClassSchemeExists(String path) {
        _scheme = new AssetClassSchemeRef(path);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("scheme", new String[] { "lang", _scheme.language() }, _scheme.scheme());
    }

    @Override
    protected String messageServiceName() {
        return "asset.class.scheme.exists";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {
        return xe.booleanValue("exists");
    }

    @Override
    protected String objectTypeName() {
        return AssetClassSchemeRef.class.getName();
    }

    @Override
    protected String idToString() {
        return _scheme.path();
    }

}
