package daris.client.model.query;

public class XPathValue {

    private String _xpath;
    private String _name;
    private String _value;

    public XPathValue(String xpath, String name, String value) {
        _xpath = xpath;
        _name = name;
        _value = value;
    }

    public String xpath() {
        return _xpath;
    }

    public String name() {
        return _name;
    }

    public String value() {
        return _value;
    }
}
