package nig.mf.plugin.pssd.sc;

import java.util.Collection;
import java.util.List;

import nig.mf.plugin.pssd.user.self.settings.ShoppingCartUserSelfSettings;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ShoppingCart {

    public static final String SERVICE_CART_CREATE = "shopping.cart.create";
    public static final String SERVICE_CART_DESCRIBE = "shopping.cart.describe";
    public static final String SERVICE_CART_LIST = "shopping.cart.list";
    public static final String SERVICE_CART_CONTENT_ADD = "shopping.cart.content.add";
    public static final String SERVICE_CART_CONTENT_REMOVE = "shopping.cart.content.remove";
    public static final String SERVICE_CART_CONTENT_LIST = "shopping.cart.content.list";
    public static final String SERVICE_CART_CONTENT_REFRESH = "shopping.cart.content.refresh";
    public static final String SERVICE_CART_MODIFY = "shopping.cart.modify";
    public static final String SERVICE_CART_ORDER = "shopping.cart.order";
    public static final String SERVICE_CART_DESTROY = "shopping.cart.destroy";

    public static String create(ServiceExecutor executor, String name, String description) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        if (name != null) {
            dm.add("name", name);
        }
        if (description != null) {
            dm.add("description", description);
        }
        dm.add("template", ShoppingCartTemplate.NAME);

        // create the shopping cart
        String cartId = executor.execute(SERVICE_CART_CREATE, dm.root()).value("id");

        // apply default settings in user.self.settings (only if it exists and
        // it is
        // still valid).
        boolean applied = applyUserSelfSettings(executor, cartId);

        if (!applied) {
            // // if user.self.settings is not applied, we need to fix the
            // // inconsistency in the cart settings:
            // // wrong combination 1: delivery=download, packaging=none
            // // wrong combination 2: delivery=deposit, packaging!=none
            // correctArchiveSettings(executor, cartId);

            /*
             * if no user settings was set, use download(browser) as destination
             */
            applyDownloadDestination(executor, cartId);
        }

        // returns the cart id
        return cartId;
    }

    private static boolean applyUserSelfSettings(ServiceExecutor executor, String cartId) throws Throwable {
        XmlDoc.Element usse = ShoppingCartUserSelfSettings.get(executor);
        if (usse == null) {
            return false;
        }
        try {
            ShoppingCartUserSelfSettings.validate(executor, usse);
        } catch (Throwable e) {
            /*
             * If there is anything wrong with the shopping cart settings from
             * the user.self.settings. Just do not use. It is fine. Since the
             * next (successful) shopping.cart.order will overwrite it with a
             * correct one.
             */
            return false;
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);

        /*
         * delivery method, destination and args
         */
        DeliveryMethod method = DeliveryMethod.fromString(usse.value("delivery/method"));
        if (method != null) {
            dm.add("delivery", method);
            if (method == DeliveryMethod.deposit) {
                dm.add("delivery-destination", usse.value("delivery/destination"));
                List<XmlDoc.Element> aes = usse.elements("delivery/arg");
                if (aes != null) {
                    for (XmlDoc.Element ae : aes) {
                        boolean inSecureWallet = ae.booleanValue("@in-secure-wallet", false);
                        dm.add(inSecureWallet ? "secure-wallet-delivery-arg" : "delivery-arg", new String[] { "name",
                                ae.value("@name") }, ae.value());
                    }
                }
            }
        }

        /*
         * archive
         */
        Archive.Type archiveType = Archive.Type.fromString(usse.value("archive/type"));
        if (archiveType != null) {
            dm.push("packaging");
            dm.add("package-method", archiveType.name());
            if (usse.elementExists("archive/parameter")) {
                dm.addAll(usse.elements("archive/parameter"));
            }
            dm.pop();
        }

        /*
         * layout
         */
        Layout.Type layoutType = Layout.Type.fromString(usse.value("layout/type"));
        if (layoutType != null) {
            dm.add("layout", layoutType.name());
            if (layoutType == Layout.Type.custom) {
                dm.add("layout-pattern", usse.value("layout/pattern"));
            }
        }
        executor.execute(SERVICE_CART_MODIFY, dm.root());
        return true;
    }

    private static void correctArchiveSettings(ServiceExecutor executor, String cartId) throws Throwable {
        // get the cart detail
        XmlDoc.Element ce = executor.execute(SERVICE_CART_DESCRIBE, "<args><sid>" + cartId + "</sid></args>", null,
                null).element("cart");

        boolean valid = true;
        DeliveryMethod deliveryMethod = DeliveryMethod.fromString(ce.value("delivery-method"));
        Archive.Type archiveType = Archive.Type.fromString(ce.value("packaging"));
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        if (deliveryMethod == DeliveryMethod.deposit) {
            if (archiveType != Archive.Type.none) {
                dm.push("packaging");
                dm.add("package-method", Archive.Type.zip.name());
                dm.pop();
                valid = false;
            }
        } else {
            if (archiveType == Archive.Type.none) {
                dm.push("packaging");
                dm.add("package-method", Archive.Type.zip.name());
                dm.pop();
                valid = false;
            }
        }
        if (!valid) {
            executor.execute(SERVICE_CART_MODIFY, dm.root());
        }
    }

    private static void applyDownloadDestination(ServiceExecutor executor, String cartId) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        dm.add("delivery", DeliveryMethod.download);
        dm.push("packaging");
        dm.add("package-method", Archive.Type.zip.name());
        dm.pop();
        executor.execute(SERVICE_CART_MODIFY, dm.root());
    }

    public static void refreshContent(ServiceExecutor executor, String cartId) {
        try {
            executor.execute(SERVICE_CART_CONTENT_REFRESH, "<args><sid>" + cartId + "</sid></args>", null, null);
        } catch (Throwable e) {
            // It is fine if it fails to refresh. It may caused by wrong status
            // (i.e. not editable).
            // e.printStackTrace(System.out);
        }
    }

    public static void order(ServiceExecutor executor, String cartId) throws Throwable {

        /*
         * save cart settings to user.self.settings
         */
        XmlDoc.Element ce = executor.execute(SERVICE_CART_DESCRIBE, "<args><sid>" + cartId + "</sid></args>", null,
                null).element("cart");
        if (ce == null) {
            throw new IllegalArgumentException("Shopping cart " + cartId + " does not exist.");
        }
        if (ce.booleanValue("access/can-modify") == false) {
            throw new Exception("Shopping cart " + cartId + " cannot be modified because of its status: "
                    + ce.value("status"));
        }
        saveUserSelfSettings(executor, ce);

        /*
         * order
         */
        executor.execute(SERVICE_CART_ORDER, "<args><sid>" + cartId + "</sid></args>", null, null);
    }

    private static void saveUserSelfSettings(ServiceExecutor executor, XmlDoc.Element ce) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker(ShoppingCartUserSelfSettings.ROOT_ELEMENT_NAME);

        /*
         * delivery
         */
        dm.push("delivery");
        dm.add("method", ce.value("delivery-method"));
        if (ce.elementExists("delivery-destination")) {
            dm.add("destination", ce.value("delivery-destination"));
        }
        if (ce.elementExists("delivery-arg")) {
            List<XmlDoc.Element> aes = ce.elements("delivery-arg");
            for (XmlDoc.Element ae : aes) {
                dm.add("arg", new String[] { "name", ae.value("@name") }, ae.value());
            }
        }
        if (ce.elementExists("secure-delivery-arg")) {
            List<XmlDoc.Element> saes = ce.elements("secure-delivery-arg");
            for (XmlDoc.Element sae : saes) {
                String source = sae.value("@source");
                if (source != null) {
                    dm.add("arg", new String[] { "in-secure-wallet", Boolean.toString(true) }, source);
                }
            }
        }
        dm.pop();

        /*
         * archive (packaging)
         */
        if (ce.elementExists("packaging")) {
            dm.push("archive");
            dm.add("type", ce.value("packaging"));
            if (ce.elementExists("packaging/parameter")) {
                List<XmlDoc.Element> pes = ce.elements("packaging/parameter");
                for (XmlDoc.Element pe : pes) {
                    dm.add("parameter", new String[] { "name", pe.value("@name") }, pe.value());
                }
            }
            dm.pop();
        }

        /*
         * layout
         */
        if (ce.elementExists("layout")) {
            dm.push("layout");
            dm.add("type", ce.value("layout"));
            if (ce.elementExists("layout/layout-pattern")) {
                dm.add("pattern", ce.value("layout/layout-pattern"));
            }
            dm.pop();
        }

        ShoppingCartUserSelfSettings.set(executor, dm.root());
    }

    public static void destroy(ServiceExecutor executor, Collection<String> cartIds) throws Throwable {
        if (cartIds != null) {
            for (String scid : cartIds) {
                destroy(executor, scid);
            }
        }
    }

    public static void destroy(ServiceExecutor executor, String cartId) throws Throwable {
        if (cartId != null) {
            executor.execute(SERVICE_CART_DESTROY, "<args><sid>" + cartId + "</sid></args>", null, null);
        }
    }

    public static void destroy(Collection<String> states, ServiceExecutor executor) throws Throwable {
        if (states != null) {
            XmlDocMaker dm = new XmlDocMaker("args");
            for (String state : states) {
                dm.add("status", state);
            }
            Collection<String> cartIds = executor.execute(SERVICE_CART_LIST, dm.root()).values("cart/@id");
            destroy(executor, cartIds);
        }
    }
}
