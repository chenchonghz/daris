package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.DeliveryDestination;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartDestinationList extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.destination.list";

    private Interface _defn;

    public SvcShoppingCartDestinationList() throws Throwable {

        _defn = new Interface();

    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "List the deposit destinations on the server side for shopping carts.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        DeliveryDestination.list(executor(), w);
    }

}
