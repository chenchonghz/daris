package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartCreate extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.create";

    private Interface _defn;

    public SvcShoppingCartCreate() throws Throwable {

        _defn = new Interface();

        /*
         * name
         */
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "A human readable name (could be non-unique) for the shopping-cart.", 0, 1));

        /*
         * description
         */
        _defn.add(new Interface.Element("description", StringType.DEFAULT, "A description for thr shopping cart.", 0, 1));

    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Creates a shopping-cart based on default shopping-cart template.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        /*
         * name
         */
        String name = args.value("name");

        /*
         * description
         */
        String description = args.value("description");

        /*
         * create the cart
         */
        String cartId = ShoppingCart.create(executor(), name, description);
        w.add("sid", cartId);
    }

}
