package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartOrder extends PluginService {

    private Interface _defn;

    public SvcShoppingCartOrder() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The id of the shopping cart.", 1, 1));
    }

    public String name() {

        return "om.pssd.shoppingcart.order";
    }

    public String description() {

        return "Start processing the specified shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        ShoppingCart.order(executor(), args.value("sid"));
    }
}
