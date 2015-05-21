package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentAdd extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.add";

    private Interface _defn;

    public SvcShoppingCartContentAdd() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart id.", 1, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citable id of the asset to be added.", 0,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
                "Should the descendants of the specified cid(s) be included? Defaults to true.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the asset to be added.", 0,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("where", StringType.DEFAULT, "A query to select assets to add to the cart.", 0,
                1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Add objects into the shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String cartId = args.value("sid");
        boolean recursive = args.booleanValue("recursive", true);
        Collection<String> cids = args.values("cid");
        if (cids != null && !cids.isEmpty()) {
            addContentItems(executor(), cartId, cids, recursive);
        }

        Collection<String> ids = args.values("id");
        if (ids != null && !ids.isEmpty()) {
            addContentItems(executor(), cartId, ids);
        }

        String where = args.value("where");
        if (where != null) {
            executor().execute(ShoppingCart.SERVICE_CART_CONTENT_ADD,
                    "<args><sid>" + cartId + "</sid><where>" + where + "</where></args>", null, null);
        }
    }

    private static void addContentItems(ServiceExecutor executor, String cartId, Collection<String> ids)
            throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        for (String id : ids) {
            dm.add("id", id);
        }
        executor.execute(ShoppingCart.SERVICE_CART_CONTENT_ADD, dm.root());
    }

    private static void addContentItems(ServiceExecutor executor, String cartId, Collection<String> cids,
            boolean recursive) throws Throwable {
        if (cids != null) {
            for (String cid : cids) {
                addContentItem(executor, cartId, cid, recursive);
            }
        }
    }

    private static void addContentItem(ServiceExecutor executor, String cartId, String cid, boolean recursive)
            throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("(asset has content) and");
        sb.append(" (cid='" + cid + "'");
        if (recursive) {
            sb.append(" or cid starts with '" + cid + "'");
        }
        sb.append(")");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        dm.add("where", sb.toString());
        executor.execute(ShoppingCart.SERVICE_CART_CONTENT_ADD, dm.root());
    }
}
