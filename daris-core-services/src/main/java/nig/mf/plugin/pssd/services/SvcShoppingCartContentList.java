package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentList extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.content.list";

    private Interface _defn;

    public SvcShoppingCartContentList() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart id.", 1, 1));
        _defn.add(new Interface.Element("idx", IntegerType.DEFAULT,
                "The starting position of the result set. Defaults to 1.", 0, 1));
        _defn.add(new Interface.Element("size", IntegerType.DEFAULT,
                "The number of content items to retrieve. Defaults to infinity.", 0, 1));

    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "List the contents in the specified shopping cart.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String cartId = args.value("sid");
        String idx = args.value("idx");
        String size = args.value("size");

        /*
         * refresh the content
         */
        ShoppingCart.refreshContent(executor(), cartId);

        /*
		 * 
		 */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cartId);
        if (idx != null) {
            dm.add("idx", idx);
        }
        if (size != null) {
            dm.add("size", size);
        } else {
            dm.add("size", "infinity");
        }
        dm.add("count", true);
        XmlDoc.Element r = executor().execute("shopping.cart.content.list", dm.root());
        XmlDoc.Element cursorElement = r.element("cart/cursor");
        XmlDoc.Element sizeElement = r.element("cart/size");
        List<XmlDoc.Element> aes = r.elements("cart/item/asset");

        /*
         * reformat xml and output to xml writer.
         */
        listContentItems(executor(), aes, w);

        if ((idx != null || size != null) && cursorElement != null) {
            w.add(cursorElement);
        }
        if ((idx != null || size != null) && sizeElement != null) {
            w.add(sizeElement);
        }
    }

    private static void listContentItems(ServiceExecutor executor, List<XmlDoc.Element> ies, XmlWriter w)
            throws Throwable {
        if (ies != null) {
            for (XmlDoc.Element ie : ies) {
                listContentItem(executor, ie, w);
            }
        }

    }

    private static void listContentItem(ServiceExecutor executor, XmlDoc.Element ie, XmlWriter w) throws Throwable {
        String assetId = ie.value("@id");
        XmlDoc.Element ae = executor.execute("asset.get", "<args><id>" + assetId + "</id></args>", null, null).element(
                "asset");
        String cid = ae.value("cid");
        w.push("asset",
                new String[] { "id", assetId, "cid", cid, "version", ie.value("@version"), "type", ie.value("@type"),
                        "status", ie.value("@status"), "size", ie.value("@size") });

        if (ae.elementExists("meta/daris:pssd-object")) {
            XmlDoc.Element oe = ae.element("meta/daris:pssd-object");
            w.add("object",
                    new String[] { "type", oe.value("type"), "name", oe.value("name"), "description",
                            oe.value("descriptioin") });
        }
        w.pop();
    }
}
