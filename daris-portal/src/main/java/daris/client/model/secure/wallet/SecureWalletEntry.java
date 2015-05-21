package daris.client.model.secure.wallet;

import arc.mf.client.xml.XmlElement;

public class SecureWalletEntry {

    public static enum Type {
        string, xml;

        public static final Type fromString(String s) {
            if (s != null) {
                Type[] vs = values();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i].name().equals(s)) {
                        return vs[i];
                    }
                }
            }
            return null;
        }
    }

    private String _key;
    private String _value;
    private XmlElement _xvalue;
    private Type _type;

    public SecureWalletEntry(XmlElement e) {
        _key = e.value("@key");
        _value = e.value("value");
        _type = Type.fromString(e.value("@type"));
        if (_type == Type.xml) {
            _xvalue = e.element("value");
        } else {
            _value = e.value();
        }
    }

    public SecureWalletEntry(String key, XmlElement xvalue) {
        _key = key;
        _xvalue = xvalue;
        _type = Type.xml;
    }

    public SecureWalletEntry(String key, String value) {
        _key = key;
        _value = value;
        _type = Type.string;
    }

    public String key() {
        return _key;
    }

    public String value() {
        return _value;
    }

    public XmlElement xvalue() {
        return _xvalue;
    }
}
