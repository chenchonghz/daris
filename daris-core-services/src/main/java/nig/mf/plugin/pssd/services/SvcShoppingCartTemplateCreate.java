package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCartTemplate;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartTemplateCreate extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.template.create";

    private Interface _defn;

    public SvcShoppingCartTemplateCreate() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element(
                "replace",
                BooleanType.DEFAULT,
                "Sets to true to destroy and re-create the template if the template named pssd already exists. Defaults to false.",
                0, 1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Create the shopping cart template for PSSD data model.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        boolean replace = args.booleanValue("replace", false);
        String tid = null;
        if (ShoppingCartTemplate.exists(executor())) {
            if (replace) {
                ShoppingCartTemplate.destroy(executor(), true);
                tid = ShoppingCartTemplate.create(executor());
            } else {
                tid = ShoppingCartTemplate.getId(executor());
            }
        } else {
            tid = ShoppingCartTemplate.create(executor());
        }
        w.add("tid", new String[] { "name", ShoppingCartTemplate.NAME }, tid);
    }

}