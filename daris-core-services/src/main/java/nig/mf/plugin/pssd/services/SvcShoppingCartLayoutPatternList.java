package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.ApplicationProperty;
import nig.mf.plugin.pssd.sc.Layout;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcShoppingCartLayoutPatternList extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.layout-pattern.list";

    private Interface _defn;

    public SvcShoppingCartLayoutPatternList() {
        _defn = new Interface();
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "lists the pre-defined shopping cart layout patterns.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        List<Layout.Pattern> ps = ApplicationProperty.ShoppingCartLayoutPattern.getAll(executor());
        if (ps != null) {
            for (Layout.Pattern p : ps) {
                w.add("layout-pattern", new String[] { "name", p.name, "description", p.description }, p.pattern);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
