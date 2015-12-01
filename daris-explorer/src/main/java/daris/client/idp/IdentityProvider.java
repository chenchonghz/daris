package daris.client.idp;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class IdentityProvider {

    private String _id;
    private String _label;
    private String _description;
    private String _shortName;

    public IdentityProvider(XmlDoc.Element e) throws Throwable {
        _id = e.value("@id");
        _label = e.stringValue("label", _id);
        _description = e.stringValue("description", _label);
        _shortName = e.stringValue("shortname", "");
    }

    @Override
    public String toString() {
        return _label;
    }

    public String id() {
        return _id;
    }

    public String shortName() {
        return _shortName;
    }

    public String description() {
        return _description;
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof String) {
                return ((String) o).equals(_id);
            } else if (o instanceof IdentityProvider) {
                return _id.equals(((IdentityProvider) o).id());
            }
        }
        return false;
    }

    public void saveXml(XmlWriter w) throws Throwable {
        w.push("provider", new String[] { "id", _id });
        if (_label != null) {
            w.add("label", _label);
        }
        if (_shortName != null) {
            w.add("shortname", _shortName);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        w.pop();
    }

}
