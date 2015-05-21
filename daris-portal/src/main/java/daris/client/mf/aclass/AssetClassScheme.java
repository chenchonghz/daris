package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;

public class AssetClassScheme {

    private String _lang;
    private String _scheme;
    private int _nbMembers = -1;

    public AssetClassScheme(XmlElement se) throws Throwable {
        _lang = se.value("@lang");
        _scheme = se.value();
        if (se.element("total") != null) {
            _nbMembers = se.intValue("total", 0);
        }
    }

    protected AssetClassScheme(String language, String scheme, int nbMembers) {
        _lang = language;
        _scheme = scheme;
        _nbMembers = nbMembers;
    }

    public String scheme() {
        return _scheme;
    }

    public String language() {
        return _lang;
    }

    public int numberOfMembers() {
        return _nbMembers;
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
