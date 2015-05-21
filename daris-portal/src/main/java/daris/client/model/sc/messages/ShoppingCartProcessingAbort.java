package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartProcessingAbort extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "shopping.cart.processing.abort";
    private long _cartId;

    public ShoppingCartProcessingAbort(ShoppingCartRef cart) {
        _cartId = cart.id();
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("sid", _cartId);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return ShoppingCart.TYPE_NAME;
    }

    @Override
    protected String idToString() {
        return Long.toString(_cartId);
    }

}
