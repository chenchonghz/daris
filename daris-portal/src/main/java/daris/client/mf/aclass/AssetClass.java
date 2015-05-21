package daris.client.mf.aclass;

import arc.mf.client.xml.XmlElement;

public class AssetClass {

    private AssetClassSchemeRef _scheme;
    private String _lang;
    private String _name;
    private String _description;
    private int _nbMembers = -1;
    private int _nbMembersAll = -1;

    public AssetClass(AssetClassSchemeRef scheme, String lang, String name, String description, int nbMembers,
            int nbMembersAll) {
        _scheme = scheme;
        _lang = lang;
        _name = name;
        _description = description;
        _nbMembers = nbMembers;
        _nbMembersAll = nbMembersAll;
    }

    public AssetClass(AssetClassSchemeRef scheme, XmlElement ce) throws Throwable {
        _scheme = scheme;
        _lang = ce.value("@lang");
        _name = ce.value("name");
        _description = ce.value("description");
        if (ce.element("count") != null) {
            _nbMembers = ce.intValue("connt/self", 0);
            _nbMembersAll = ce.intValue("count/total", 0);
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

    public String toString() {
        return path();
    }

    public int numberOfSelfMembers() {
        return _nbMembers;
    }

    public int numberOfAllMembers() {
        return _nbMembersAll;
    }

}
