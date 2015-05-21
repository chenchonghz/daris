package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class AssetClassExists extends ObjectMessage<Boolean> {

    private AssetClassRef _class;

    public AssetClassExists(String path) {
        this(new AssetClassRef(path));
    }
    
    public AssetClassExists(AssetClassRef cls){
        _class = cls;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("scheme", new String[] { "lang", _class.scheme().language() }, _class.scheme().scheme());
        String lang = _class.language();
        w.add("class", (lang == null ? "" : (lang + ":")) + _class.name());
    }

    @Override
    protected String messageServiceName() {
        return "asset.class.exists";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {
        return xe.booleanValue("exists");
    }

    @Override
    protected String objectTypeName() {
        return AssetClassRef.class.getName();
    }

    @Override
    protected String idToString() {
        return _class.path();
    }

}
