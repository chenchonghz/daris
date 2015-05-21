package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartCreate extends ObjectMessage<ShoppingCartRef> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.create";

    private String _name;
    private String _description;

    public ShoppingCartCreate(String name, String description) {

        _name = name;
        _description = description;
    }

    public ShoppingCartCreate() {
        this(null, null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
    }

    @Override
    protected String messageServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected ShoppingCartRef instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            long cartId = xe.longValue("sid");
            return new ShoppingCartRef(cartId);
        }
        return null;
    }

    @Override
    protected String objectTypeName() {

        return ShoppingCart.TYPE_NAME;
    }

    @Override
    protected String idToString() {

        return null;
    }

}
