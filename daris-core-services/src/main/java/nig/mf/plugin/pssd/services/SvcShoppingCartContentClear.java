package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentClear extends PluginService {
    
    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.clear";

    private Interface _defn;

    public SvcShoppingCartContentClear() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart id.", 1, 1));

    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Clear the contents of the specified shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String cartId = args.value("sid");
        clearContentItems(executor(), cartId);
    }

    private static void clearContentItems(ServiceExecutor executor, String cartId) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        dm.add("size", "infinity");
        dm.add("count", true);
        XmlDoc.Element r = executor.execute("shopping.cart.content.list", dm.root());
        Collection<String> assetIds = r.values("cart/item/asset/@id");
        if (assetIds != null) {
            dm = new XmlDocMaker("args");
            dm.add("sid", cartId);
            for (String assetId : assetIds) {
                dm.add("id", assetId);
            }
            executor.execute("shopping.cart.content.remove", dm.root());
        }
    }

}
