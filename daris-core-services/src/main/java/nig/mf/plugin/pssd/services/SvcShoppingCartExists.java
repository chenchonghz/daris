package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartExists extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.exists";

    private Interface _defn;

    public SvcShoppingCartExists() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart id.", 0, 1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Check if the shopping cart exists.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cartId = args.value("sid");
        boolean exists = exists(executor(), cartId);
        w.add("exists", new String[] { "sid", cartId }, exists);
    }

    static boolean exists(ServiceExecutor executor, String cartId) throws Throwable {
        Collection<String> cartIds = executor.execute("shopping.cart.list", "<args><size>infinity</size></args>", null,
                null).values("cart/@id");
        if (cartIds == null) {
            return false;
        }
        return cartIds.contains(cartId);
    }
}
