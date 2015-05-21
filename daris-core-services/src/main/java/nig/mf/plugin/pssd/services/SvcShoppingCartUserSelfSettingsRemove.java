package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.user.self.settings.ShoppingCartUserSelfSettings;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcShoppingCartUserSelfSettingsRemove extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.user.self.settings.remove";

    private Interface _defn;

    public SvcShoppingCartUserSelfSettingsRemove() {
        _defn = new Interface();
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Remove the user's default shopping cart settings.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        ShoppingCartUserSelfSettings.remove(executor());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
