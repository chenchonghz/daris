package daris.client.model.sc.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentRemove extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.remove";

    private ShoppingCartRef _cart;
    private List<String> _ids;

    public ShoppingCartContentRemove(ShoppingCartRef cart, List<ContentItem> items) {

        _cart = cart;
        _ids = new ArrayList<String>();
        for (ContentItem item : items) {
            _ids.add(item.assetId());
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("sid", _cart.id());
        for (String id : _ids) {
            w.add("id", id);
        }
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
