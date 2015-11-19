package daris.client.idp;

import arc.xml.XmlDoc;

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

}
