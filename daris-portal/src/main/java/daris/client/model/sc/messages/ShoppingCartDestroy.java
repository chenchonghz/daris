package daris.client.model.sc.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;

public class ShoppingCartDestroy extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.destroy";

    private List<Long> _cartIds;
    private Set<Status> _states;

    public ShoppingCartDestroy(List<ShoppingCartRef> carts) {
        _cartIds = new ArrayList<Long>(carts.size());
        for (ShoppingCartRef cart : carts) {
            _cartIds.add(cart.id());
        }
    }

    public ShoppingCartDestroy(ShoppingCartRef cart) {
        this(cart.id());
    }

    public ShoppingCartDestroy(long cartId) {
        _cartIds = new ArrayList<Long>(1);
        _cartIds.add(cartId);
    }

    public ShoppingCartDestroy(Collection<Status> states) {
        _states = new HashSet<Status>(states);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_cartIds != null) {
            for (Long cartId : _cartIds) {
                w.add("sid", cartId);
            }
        }
        if (_states != null) {
            for (Status state : _states) {
                w.add("status", state);
            }
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

        return null;
    }

}
