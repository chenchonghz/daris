package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCartTemplate;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartTemplateDestroy extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.template.destroy";

    private Interface _defn;

    public SvcShoppingCartTemplateDestroy() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("force", BooleanType.DEFAULT,
                "set to true to destroy all shopping carts that still use this template. Defaults to false.", 0, 1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Destroy the shopping cart template for PSSD data model.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        boolean force = args.booleanValue("force", false);
        ShoppingCartTemplate.destroy(executor(), force);
    }

}