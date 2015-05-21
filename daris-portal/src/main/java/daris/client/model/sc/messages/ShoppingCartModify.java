package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;

public class ShoppingCartModify extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "shopping.cart.modify";

    private ShoppingCart _cart;

    public ShoppingCartModify(ShoppingCart cart) {

        _cart = cart;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        _cart.saveUpdateArgs(w);
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

        return Long.toString(_cart.id());
    }

}
