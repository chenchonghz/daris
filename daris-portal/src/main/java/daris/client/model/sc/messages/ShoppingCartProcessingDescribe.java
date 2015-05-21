package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.Progress;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartProcessingDescribe extends ObjectMessage<Progress> {

    public static final String SERVICE_NAME = "shopping.cart.processing.describe";

    private long _cartId;

    public ShoppingCartProcessingDescribe(long scid) {
        _cartId = scid;
    }

    public ShoppingCartProcessingDescribe(ShoppingCartRef cart) {
        this(cart.id());
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
    protected Progress instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            XmlElement pe = xe.element("process");
            if (pe != null) {
                return new Progress(pe);
            }
        }
        return null;
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
