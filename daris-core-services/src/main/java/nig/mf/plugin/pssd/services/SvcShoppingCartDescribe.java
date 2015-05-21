package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.ApplicationProperty;
import nig.mf.plugin.pssd.sc.Layout;
import nig.mf.plugin.pssd.sc.Status;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartDescribe extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.describe";
    public static final String SINK_URL_PREFIX = "sink:";

    private Interface _defn;

    public SvcShoppingCartDescribe() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("sid", LongType.DEFAULT, "The shopping cart to describe.", 0, 1));
        _defn.add(new Interface.Element("idx", IntegerType.DEFAULT,
                "The starting position of the result set. Defaults to 1.", 0, 1));
        _defn.add(new Interface.Element("size", IntegerType.DEFAULT,
                "The number of entries to retrieve. Defaults to 100.", 0, 1));
        _defn.add(new Interface.Element("status", new EnumType(Status.values()),
                "If set, only cart matching the specified status will be returned.", 0, 1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Return the details of the specified carts by id or status. If neither of id nor status is specified, all the carts owned by the user are described.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String sid = args.value("sid");
        String idx = args.value("idx");
        String size = args.value("size");
        String status = args.value("status");
        XmlDocMaker dm = new XmlDocMaker("args");
        if (sid != null) {
            dm.add("sid", sid);
        }
        if (idx != null) {
            dm.add("idx", idx);
        }
        if (size != null) {
            dm.add("size", size);
        }
        if (status != null) {
            dm.add("status", status);
        }
        dm.add("count", true);
        XmlDoc.Element r = executor().execute("shopping.cart.describe", dm.root());
        List<XmlDoc.Element> ces = r.elements("cart");
        if (ces != null) {
            for (XmlDoc.Element ce : ces) {
                describe(executor(), ce, w);
            }
            if (r.elementExists("cursor")) {
                w.add(r.element("cursor"));
            }
        }
    }

    private static void describe(ServiceExecutor executor, XmlDoc.Element ce, XmlWriter w) throws Throwable {
        /*
         * inject some extra metadata
         */

        // @formatter:off
        /*
         * :delivery-destination -sink-type file-system sink:data
         */
        // @formatter:on
        XmlDoc.Element dde = ce.element("delivery-destination");
        if (dde != null) {
            String url = dde.value();
            if (url.startsWith(SINK_URL_PREFIX)) {
                String sinkName = url.substring(SINK_URL_PREFIX.length());
                String sinkType = executor.execute("sink.describe", "<args><name>" + sinkName + "</name></args>", null,
                        null).value("sink/destination/type");
                dde.add(new XmlDoc.Attribute("sink-type", sinkType));
            }
        }

        // @formatter:off
        /*
         * :layout custom
         *     :layout-pattern -name pssd-default -description pssd-default xxxyyyxxx
         */
        // @formatter:on
        XmlDoc.Element lpe = ce.element("layout/layout-pattern");
        if (lpe != null) {
            Layout.Pattern lp = ApplicationProperty.ShoppingCartLayoutPattern.getPattern(executor, lpe.value());
            if (lp != null) {
                lpe.add(new XmlDoc.Attribute("name", lp.name));
                if (lp.description != null) {
                    lpe.add(new XmlDoc.Attribute("description", lp.description));
                }
            }
        }
        w.add(ce);
    }
}
