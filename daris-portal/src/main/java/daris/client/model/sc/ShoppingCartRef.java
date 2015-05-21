package daris.client.model.sc;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.Timer;

public class ShoppingCartRef extends ObjectRef<ShoppingCart> implements Comparable<ShoppingCartRef> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.describe";

    private long _cartId;
    private String _name;
    private Status _status;

    public ShoppingCartRef(ShoppingCart cart) {
        super(cart);
        _cartId = cart.id();
        _name = cart.name();
        _status = cart.status();
    }

    public ShoppingCartRef(long sid) {

        this(sid, null, null);
    }

    public ShoppingCartRef(long sid, String name, Status status) {

        _cartId = sid;
        _name = name;
        _status = status;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {

        w.add("sid", _cartId);
    }

    @Override
    protected String resolveServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected ShoppingCart instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            XmlElement ce = xe.element("cart");
            if (ce != null) {
                ShoppingCart sc = new ShoppingCart(ce);
                _name = sc.name();
                _status = sc.status();
                return sc;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {

        return ShoppingCart.TYPE_NAME;
    }

    @Override
    public String idToString() {

        return Long.toString(_cartId);
    }

    public long id() {

        return _cartId;
    }

    public String name() {

        return _name;
    }

    public Status status() {

        return _status;
    }

    @Override
    public boolean equals(Object o) {

        if (o != null) {
            if (o instanceof ShoppingCartRef) {
                return _cartId == ((ShoppingCartRef) o).id();
            }
        }
        return false;
    }

    @Override
    public int compareTo(ShoppingCartRef o) {

        if (o == null) {
            return -1;
        }
        if (_cartId > o.id()) {
            return -1;
        } else if (_cartId == o.id()) {
            return 0;
        } else {
            return 1;
        }
    }

    public String toHTML() {

        if (referent() != null) {
            return referent().toHTML();
        } else {
            String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Shopping-cart</th></tr><thead>";
            html += "<tbody>";
            html += "<tr><td><b>id:</b></td><td>" + _cartId + "</td></tr>";
            if (_name != null) {
                html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
            }
            html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
            html += "</tbody></table>";
            return html;
        }
    }

    public String summary() {

        return "Shopping Cart (ID: " + _cartId + ", Status:" + _status + ")";
    }

    public void statusDescription(final ObjectResolveHandler<String> rh) {

        resolve(new ObjectResolveHandler<ShoppingCart>() {

            @Override
            public void resolved(ShoppingCart cart) {
                DeliveryMethod m = cart.destination().method();
                String desc;
                switch (cart.status()) {
                case editable:
                    desc = "ready to use";
                    break;
                case await_processing:
                    desc = "ordered, await processing";
                    break;
                case assigned:
                    desc = "assigned";
                    break;
                case processing:
                    if (DeliveryMethod.deposit.equals(m)) {
                        desc = "checking out to: " + cart.destination().name();
                    } else {
                        desc = "preparing archive";
                    }
                    break;
                case data_ready:
                    if (DeliveryMethod.deposit.equals(m)) {
                        desc = "completed, data transfered to " + cart.destination().name();
                    } else {
                        desc = "archive is ready to download";
                    }
                    break;
                case fulfilled:
                    desc = cart.status().toString();
                    break;
                case rejected:
                    desc = cart.status().toString();
                    break;
                case error:
                    desc = "error occured";
                    break;
                case withdrawn:
                    desc = cart.status().toString();
                    break;
                default:
                    desc = cart.status().toString();
                    break;
                }
                rh.resolved(desc);
            }
        });
    }

    public Timer monitorProgress(int delay, ProgressHandler ph) {
        return ShoppingCart.monitorProgress(id(), delay, ph);
    }

    public boolean isActive() {
        return ActiveShoppingCart.isActive(_cartId);
    }

}
