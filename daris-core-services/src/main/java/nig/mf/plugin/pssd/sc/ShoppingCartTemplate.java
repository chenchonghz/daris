package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.sink.Sink;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ShoppingCartTemplate {

    public static final String NAME = "pssd";

    public static final String DEFAULT_LAYOUT = Layout.Type.custom.name();
    public static final String DEFAULT_LAYOUT_PATTERN = Layout.Pattern.PSSD_DEFAULT.pattern;

    public static final String SERVICE_TEMPLATE_EXISTS = "shopping.cart.template.exists";
    public static final String SERVICE_TEMPLATE_DESCRIBE = "shopping.cart.template.describe";
    public static final String SERVICE_TEMPLATE_CREATE = "shopping.cart.template.create";
    public static final String SERVICE_TEMPLATE_DESTROY = "shopping.cart.template.destroy";

    public static boolean exists(ServiceExecutor executor) throws Throwable {
        return executor.execute(SERVICE_TEMPLATE_EXISTS, "<args><name>" + NAME + "</name></args>", null, null)
                .booleanValue("exists");
    }

    public static String getId(ServiceExecutor executor) throws Throwable {
        return executor.execute(SERVICE_TEMPLATE_DESCRIBE, "<args><name>" + NAME + "</name></args>", null, null).value(
                "shopping-cart-template/@id");
    }

    public static String create(ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("name", NAME);
        dm.add("self-serviced", true);
        dm.add("layout", DEFAULT_LAYOUT);
        dm.add("layout-pattern", DEFAULT_LAYOUT_PATTERN);
        List<String> sinkUrls = Sink.listUrls(executor);
        if (sinkUrls != null && !sinkUrls.isEmpty()) {
            dm.add("delivery-method", DeliveryMethod.deposit.name());
            dm.add("delivery-destination", sinkUrls.get(0));
        } else {
            dm.add("delivery-method", DeliveryMethod.download.name());
        }
        return executor.execute("shopping.cart.template.create", dm.root()).value("id");
    }

    public static void destroy(ServiceExecutor executor, boolean force) throws Throwable {
        List<String> cartIds = getCartIds(executor);
        if (cartIds != null && force == true) {
            ShoppingCart.destroy(executor, cartIds);
        }
        executor.execute(SERVICE_TEMPLATE_DESTROY, "<args><name>" + ShoppingCartTemplate.NAME + "</name></args>", null,
                null);
    }

    /**
     * Gets all the carts using the default template.
     * 
     * @param executor
     * @return
     * @throws Throwable
     */
    private static List<String> getCartIds(ServiceExecutor executor) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("size", "infinity");
        dm.add("list-all", true);
        XmlDoc.Element r = executor.execute("shopping.cart.describe", dm.root());
        List<XmlDoc.Element> sces = r.elements("cart");
        if (sces != null && !sces.isEmpty()) {
            List<String> cartIds = new Vector<String>();
            for (XmlDoc.Element sce : sces) {
                String template = sce.value("template");
                if (ShoppingCartTemplate.NAME.equals(template)) {
                    cartIds.add(sce.value("@id"));
                }
            }
            if (!cartIds.isEmpty()) {
                return cartIds;
            }
        }
        return null;
    }

}
