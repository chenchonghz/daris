package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.ApplicationProperty;
import nig.mf.plugin.pssd.sc.Layout;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcShoppingCartLayoutPatternAdd extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.layout-pattern.add";
    private Interface _defn;

    public SvcShoppingCartLayoutPatternAdd() {
        _defn = new Interface();
        _defn.add(new Interface.Element("name", StringType.DEFAULT, "The name for the pattern.", 1, 1));
        _defn.add(new Interface.Element("description", StringType.DEFAULT, "The description for the pattern",
                1, 1));
        _defn.add(new Interface.Element("pattern", StringType.DEFAULT, "The shopping cart layout pattern.", 1, 1));
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
        return "Adds a shopping cart layout pattern.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String name = args.value("name");
        String description = args.value("description");
        String pattern = args.value("pattern");
        ApplicationProperty.ShoppingCartLayoutPattern.add(executor(), new Layout.Pattern(name, description, pattern));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
