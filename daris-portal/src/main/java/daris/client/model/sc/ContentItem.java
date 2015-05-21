package daris.client.model.sc;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;

public class ContentItem {

    private String _assetId;
    private String _cid;
    private int _version;
    private String _mimeType;
    private long _size;
    private String _status;
    private String _objectType;
    private String _objectName;
    private String _objectDescription;

    private ShoppingCartRef _cart;

    public ContentItem(XmlElement ae, ShoppingCartRef cart) throws Throwable {

        _cart = cart;
        _assetId = ae.value("@id");
        _cid = ae.value("@cid");
        _version = ae.intValue("@version", 0);
        _mimeType = ae.value("@type");
        _status = ae.value("@status");
        _size = ae.longValue("@size", 0);
        _objectType = ae.value("object/@type");
        _objectName = ae.value("object/@name");
        _objectDescription = ae.value("object/@description");

    }

    public static ContentItem instantiate(XmlElement ae, ShoppingCartRef sc) throws Throwable {

        if (ae != null) {
            return new ContentItem(ae, sc);
        } else {
            return null;
        }
    }

    public static List<ContentItem> instantiate(List<XmlElement> aes, ShoppingCartRef sc) throws Throwable {

        if (aes != null) {
            List<ContentItem> items = new ArrayList<ContentItem>(aes.size());
            for (XmlElement oe : aes) {
                items.add(instantiate(oe, sc));
            }
            if (!items.isEmpty()) {
                return items;
            }
        }
        return null;
    }

    public String assetId() {

        return _assetId;
    }

    public String cid() {
        return _cid;
    }

    public int version() {

        return _version;
    }

    public String mimeType() {

        return _mimeType;
    }

    public long size() {

        return _size;
    }

    public String status() {

        return _status;
    }

    public String objectType() {

        return _objectType;
    }

    public String objectName() {

        return _objectName;
    }

    public String objectDescription() {

        return _objectDescription;
    }

    public ShoppingCartRef cart() {
        return _cart;
    }

    public String toHTML() {
        String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Item:</th></tr><thead>";
        html += "<tbody>";
        html += "<tr><td><b>id:</b></td><td>" + _cid + "</td></tr>";
        if (_objectName != null) {
            html += "<tr><td><b>name:</b></td><td>" + _objectName + "</td></tr>";
        }
        if (_objectDescription != null) {
            html += "<tr><td><b>description:</b></td><td>" + _objectDescription + "</td></tr>";
        }
        html += "<tr><td><b>MIME type:</b></td><td>" + _mimeType + " bytes</td></tr>";
        html += "<tr><td><b>size:</b></td><td>" + _size + " bytes</td></tr>";
        html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
        html += "</tbody></table>";
        return html;
    }

}
