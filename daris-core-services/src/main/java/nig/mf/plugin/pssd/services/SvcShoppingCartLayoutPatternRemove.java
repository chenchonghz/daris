package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.ApplicationProperty;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcShoppingCartLayoutPatternRemove extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.layout-pattern.remove";

    private Interface _defn;

    public SvcShoppingCartLayoutPatternRemove() {
        _defn = new Interface();
        _defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of the shopping cart layout pattern.", 1,
                1));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Removes a shopping cart layout pattern.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        ApplicationProperty.ShoppingCartLayoutPattern.remove(executor(), args.value("name"));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
