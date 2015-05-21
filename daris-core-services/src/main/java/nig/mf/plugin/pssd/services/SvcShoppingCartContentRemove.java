package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentRemove extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.remove";

    private Interface _defn;

    public SvcShoppingCartContentRemove() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart id.", 1, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citable id of the object to be removed.",
                0, Integer.MAX_VALUE));
        _defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
                "Should the descendants of the cid(s) be included? Defaults to false.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the asset to be removed.", 0,
                Integer.MAX_VALUE));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Remove contents from the specified shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String cartId = args.value("sid");
        boolean recursive = args.booleanValue("recursive", false);

        Collection<String> cids = args.values("cid");
        Collection<String> ids = args.values("id");
        /*
         * refresh contents
         */
        if (cids != null || ids != null) {
            ShoppingCart.refreshContent(executor(), cartId);
        }

        /*
         * remove contents
         */
        if (cids != null && !cids.isEmpty()) {
            removeContentItems(executor(), cartId, cids, recursive);
        }

        if (ids != null && !ids.isEmpty()) {
            removeContentItems(executor(), cartId, ids);
        }
    }

    private static void removeContentItems(ServiceExecutor executor, String cartId, Collection<String> ids)
            throws Throwable {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        for (String id : ids) {
            dm.add("id", id);
        }
        executor.execute(ShoppingCart.SERVICE_CART_CONTENT_REMOVE, dm.root());
    }

    private static void removeContentItems(ServiceExecutor executor, String cartId, Collection<String> cids,
            boolean recursive) throws Throwable {
        if (cids == null || cids.isEmpty()) {
            return;
        }
        if (recursive) {
            Collection<String> assetIds = executor.execute(ShoppingCart.SERVICE_CART_CONTENT_LIST,
                    "<args><sid>" + cartId + "</sid><size>infinity</size></args>", null, null).values(
                    "cart/item/asset/@id");
            if (assetIds == null) {
                return;
            }
            Map<String, String> ccids = new HashMap<String, String>(assetIds.size());
            for (String assetId : assetIds) {
                ccids.put(assetId, Asset.getCidById(executor, assetId));
            }
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("sid", cartId);
            int count = 0;
            for (String assetId : ccids.keySet()) {
                String ccid = ccids.get(assetId);
                if (ccid != null) {
                    for (String cid : cids) {
                        String prefix = cid + '.';
                        if (ccid.equals(cid) || ccid.startsWith(prefix)) {
                            dm.add("id", assetId);
                            count++;
                            break;
                        }
                    }
                }
            }
            if (count > 0) {
                executor.execute(ShoppingCart.SERVICE_CART_CONTENT_REMOVE, dm.root());
            }
        } else {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("sid", cartId);
            for (String cid : cids) {
                String assetId = Asset.getIdByCid(executor, cid);
                dm.add("id", assetId);
            }
            executor.execute(ShoppingCart.SERVICE_CART_CONTENT_REMOVE, dm.root());
        }
    }
}
