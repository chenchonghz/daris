package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentList extends ObjectMessage<List<ContentItem>> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.list";

    private ShoppingCartRef _cart;

    public ShoppingCartContentList(ShoppingCartRef cart) {
        _cart = cart;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("sid", _cart.id());
        w.add("size", "infinity");
    }

    @Override
    protected String messageServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected List<ContentItem> instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            List<XmlElement> aes = xe.elements("asset");
            if (aes != null) {
                return ContentItem.instantiate(aes, _cart);
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

        return Long.toString(_cart.id());
    }

}
