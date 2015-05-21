package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import nig.mf.plugin.pssd.sc.Status;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartDestroy extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.destroy";

    private Interface _defn;

    public SvcShoppingCartDestroy() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The id of the shopping cart.", 0, Integer.MAX_VALUE));
        _defn.add(new Interface.Element("status", new EnumType(new String[] { Status.data_ready.status(), Status.aborted.status(),
                Status.rejected.status(), Status.withdrawn.status(), Status.error.status() }),
                "All the shopping carts in the give status will be destroyed.", 0, 5));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Destroy the specified shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        Collection<String> cartIds = args.values("sid");
        if (cartIds != null && !cartIds.isEmpty()) {
            ShoppingCart.destroy(executor(), cartIds);
        }

        Collection<String> states = args.values("status");
        if (states != null && !states.isEmpty()) {
            ShoppingCart.destroy(states, executor());
        }
    }
}
