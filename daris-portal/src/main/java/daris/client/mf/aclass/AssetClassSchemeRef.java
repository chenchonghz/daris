package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class AssetClassSchemeRef extends ObjectRef<AssetClassScheme>  {

    private String _lang;
    private String _scheme;
    private boolean _count;

    public AssetClassSchemeRef(String lang, String scheme, boolean count) {
        _lang = lang;
        _scheme = scheme;
        _count = count;
    }

    public AssetClassSchemeRef(String lang, String scheme) {
        this(lang, scheme, false);
    }

    public AssetClassSchemeRef(String path) {
        String[] tokens = path.split(":");
        if (tokens.length == 1) {
            _lang = null;
            _scheme = path;
        } else {
            _lang = tokens[0];
            _scheme = tokens[1];
        }
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        if (_lang != null) {
            w.add("language", _lang);
        }
        w.add("scheme", _scheme);
        if (_count) {
            w.add("count", _count);
        }
    }

    @Override
    protected String resolveServiceName() {
        return "asset.class.scheme.describe";
    }

    @Override
    protected AssetClassScheme instantiate(XmlElement xe) throws Throwable {
        return new AssetClassScheme(xe.element("scheme"));
    }

    @Override
    public String referentTypeName() {
        return AssetClassScheme.class.getName();
    }

    @Override
    public String idToString() {
        return path();
    }

    public String scheme() {
        return _scheme;
    }

    public String language() {
        return _lang;
    }

    public String path() {
        if (_lang == null) {
            return _scheme;
        }
        return _lang + ":" + _scheme;
    }

    public String toString() {
        return path();
    }

}
