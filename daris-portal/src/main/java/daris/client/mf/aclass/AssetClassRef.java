package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class AssetClassRef extends ObjectRef<AssetClass> {

    private AssetClassSchemeRef _scheme;
    private String _lang;
    private String _name;
    private String _description;
    private boolean _count;

    public AssetClassRef(AssetClassSchemeRef scheme, String lang, String className, String classDescription,
            boolean count) {
        _scheme = scheme;
        _lang = lang;
        _name = className;
        _description = classDescription;
        _count = count;
    }

    public AssetClassRef(AssetClassSchemeRef scheme, String lang, String className, String classDescription) {
        this(scheme, lang, className, classDescription, false);
    }

    public AssetClassRef(AssetClassSchemeRef scheme, String lang, String className) {
        this(scheme, lang, className, null, false);
    }

    public AssetClassRef(String path) {
        String[] tokens = path.split(":");
        if (tokens.length == 4) {
            _scheme = new AssetClassSchemeRef(tokens[0], tokens[1]);
            _lang = tokens[2];
            _name = tokens[3];
        } else if (tokens.length == 3) {
            _scheme = new AssetClassSchemeRef(tokens[0], tokens[1]);
            _lang = null;
            _name = tokens[2];
        } else if (tokens.length == 2) {
            _scheme = new AssetClassSchemeRef(null, tokens[0]);
            _lang = null;
            _name = tokens[1];
        } else {
            throw new IllegalArgumentException(path + " is not valid.");
        }
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public String language() {
        return _lang;
    }

    public AssetClassSchemeRef scheme() {
        return _scheme;
    }

    public String path() {
        StringBuilder sb = new StringBuilder();
        if (_scheme != null) {
            sb.append(_scheme.path());
            sb.append(":");
        }

        if (_lang != null) {
            sb.append(_lang);
            sb.append(":");
        }

        sb.append(_name);
        return sb.toString();
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("scheme", new String[] { "lang", _scheme.language() }, _scheme.scheme());
        w.add("class", new String[] { "lang", _lang }, _name);
        if (_count) {
            w.add("count", "all");
        }
    }

    @Override
    protected String resolveServiceName() {
        return "asset.class.describe";
    }

    @Override
    protected AssetClass instantiate(XmlElement xe) throws Throwable {
        return new AssetClass(_scheme, xe.element("class"));
    }

    @Override
    public String referentTypeName() {
        return AssetClass.class.getName();
    }

    @Override
    public String idToString() {
        return path();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (_lang != null) {
            sb.append(_lang);
            sb.append(":");
        }

        sb.append(_name);
        return sb.toString();
    }

}
