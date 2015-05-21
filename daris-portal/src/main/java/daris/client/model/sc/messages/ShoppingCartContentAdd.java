package daris.client.model.sc.messages;

import java.util.ArrayList;
import java.util.Collection;

import arc.mf.client.util.ListUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentAdd extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.add";

    private ShoppingCartRef _cart;
    private Collection<String> _cids;
    private boolean _recursive = true;
    private Collection<Long> _ids;
    private String _where;

    protected ShoppingCartContentAdd(ShoppingCartRef cart, Collection<String> cids, boolean recursive,
            Collection<Long> ids, String where) {

        _cart = cart;
        _cids = cids == null ? null : new ArrayList<String>(cids);
        _recursive = recursive;
        _ids = ids == null ? null : new ArrayList<Long>(ids);
        _where = where;
    }

    public ShoppingCartContentAdd(ShoppingCartRef cart, boolean recursive, Collection<DObjectRef> os) {
        _cart = cart;
        _cids = new ArrayList<String>(os.size());
        for (DObjectRef o : os) {
            _cids.add(o.id());
        }
        _recursive = recursive;
        _ids = null;
        _where = null;
    }

    public ShoppingCartContentAdd(ShoppingCartRef cart, Collection<String> cids, boolean recursive) {
        this(cart, cids, recursive, null, null);
    }

    public ShoppingCartContentAdd(ShoppingCartRef cart, DObjectRef o, boolean recursive) {
        this(cart, ListUtil.list(o.id()), recursive, null, null);
    }

    public ShoppingCartContentAdd(ShoppingCartRef cart, String where) {
        this(cart, null, true, null, where);
    }

    public ShoppingCartContentAdd(ShoppingCartRef cart, Collection<Long> ids) {
        this(cart, null, true, ids, null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("sid", _cart.id());
        if (_cids != null) {
            for (String cid : _cids) {
                w.add("cid", cid);
            }
            w.add("recursive", _recursive);
        }
        if (_where != null) {
            w.add("where", _where);
        }
        if (_ids != null) {
            for (long id : _ids) {
                w.add("id", id);
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

        return Long.toString(_cart.id());
    }

}
