package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.DeliveryDestination;
import nig.mf.plugin.pssd.sc.DeliveryMethod;
import nig.mf.plugin.pssd.sc.Layout;
import nig.mf.plugin.pssd.user.self.settings.ShoppingCartUserSelfSettings;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcShoppingCartUserSelfSettingsSet extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.user.self.settings.set";

    private Interface _defn;

    public SvcShoppingCartUserSelfSettingsSet() {

        _defn = new Interface();

        // @formatter:off
        /*
         * :delivery
         *     :method deposit
         *     :destination sink:edward
         *      :arg -name user wilson
         *      :arg -name password -in-secure-wallet true passwd_wliu5@edward
         */
        // @formatter:on
        Interface.Element delivery = new Interface.Element("delivery", XmlDocType.DEFAULT, "delivery setttings.", 1, 1);
        delivery.add(new Interface.Element("method", new EnumType(DeliveryMethod.values()), "delivery method.", 1, 1));
        delivery.add(new Interface.Element("destination", StringType.DEFAULT,
                "The delivery destination. Must be a sink in the form of sink:<sink_name>.", 0, 1));
        Interface.Element arg = new Interface.Element("arg", StringType.DEFAULT, "The arg for the destination (sink).",
                0, Integer.MAX_VALUE);
        arg.add(new Interface.Attribute("name", StringType.DEFAULT, "The name of the arg.", 1));
        arg.add(new Interface.Attribute(
                "in-secure-wallet",
                BooleanType.DEFAULT,
                "Indicates the value of the arg is a secure wallet key and the actual value is kept in secure wallet. Defaults to false.",
                0));
        delivery.add(arg);
        _defn.add(delivery);

        // @formatter:off
        /* 
         * :archive
         *     :type zip
         *     :parameter -name compression-level 6
         */ 
        // @formatter:on
        Interface.Element archive = new Interface.Element("archive", XmlDocType.DEFAULT,
                "The archive settings for the shopping-cart output. Note: archive settings is only used when the destination is "
                        + DeliveryDestination.BROWSER + ".", 1, 1);
        archive.add(new Interface.Element("type", new EnumType(Archive.Type.values()), "type of the archive.", 1, 1));
        Interface.Element parameter = new Interface.Element("parameter", StringType.DEFAULT,
                " Additional parameter for the archive type.", 0, Integer.MAX_VALUE);
        parameter.add(new Interface.Attribute("name", StringType.DEFAULT, "The parameter name.", 1));
        archive.add(parameter);
        _defn.add(archive);

        // @formatter:off
        /* 
         * :layout
         *     :type custom
         *     :pattern XXXXXXXXX
         */ 
        // @formatter:on
        Interface.Element layout = new Interface.Element("layout", XmlDocType.DEFAULT,
                "The output layout settings, which determines the output directory/archive structure.", 1, 1);
        layout.add(new Interface.Element("type", new EnumType(Layout.Type.values()), "The type of the layout.", 1, 1));
        layout.add(new Interface.Element("pattern", StringType.DEFAULT,
                "The layout pattern. Only required if the type is custom.", 0, 1));
        _defn.add(layout);
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
        return "Sets the current user's default shopping cart settings.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        if (!args.hasSubElements()) {
            return;
        }
        ShoppingCartUserSelfSettings.validate(executor(), args);
        ShoppingCartUserSelfSettings.set(executor(), args);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
