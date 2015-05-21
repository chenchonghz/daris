package nig.mf.plugin.pssd.user.self.settings;

import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.DeliveryMethod;
import nig.mf.plugin.pssd.sc.Layout;
import nig.mf.plugin.pssd.sink.Sink;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ShoppingCartUserSelfSettings {

    public static final String ROOT_ELEMENT_NAME = "shoppingcart";

    public static void validate(ServiceExecutor executor, XmlDoc.Element se) throws Throwable {
        XmlDoc.Element de = se.element("delivery");
        if (de == null) {
            throw new Exception("delivery element is not found.");
        }
        XmlDoc.Element dme = de.element("method");
        if (dme == null) {
            throw new Exception("delivery/method is not specified.");
        }
        DeliveryMethod dm = DeliveryMethod.fromString(dme.value());
        if (dm == null) {
            throw new Exception("Failed to parse delivery/method: " + dme.value());
        }
        String dd = de.value("destination");
        if (dm == DeliveryMethod.deposit) {
            if (dd == null) {
                throw new Exception("delivery/destination is not specified.");
            }
            if (!dd.startsWith(Sink.URL_PREFIX)) {
                throw new Exception("Invalid destination: " + dd + ". It should be in the form of sink:sink_name.");
            }
            String sinkName = Sink.nameFromUrl(dd);
            if (!Sink.exists(executor, sinkName)) {
                throw new Exception("Sink: " + sinkName + " does not exist.");
            }
        } else {
            if (dd != null) {
                throw new Exception("delivery/destination should not be specified if the delivery/method is download.");
            }
        }

        XmlDoc.Element ae = se.element("archive");
        if (ae == null) {
            throw new Exception("archive element is not found.");
        }
        XmlDoc.Element ate = ae.element("type");
        if (ate == null) {
            throw new Exception("archive/type is not specified.");
        }
        Archive.Type at = Archive.Type.fromString(ate.value());
        if (dm == DeliveryMethod.download) {
            if (at == Archive.Type.none) {
                throw new Exception(
                        "archive type (or packaging method) cannot be none if the delivery method is download.");
            }
        } else {
            if (at != Archive.Type.none) {
                throw new Exception(
                        "archive type (or packaging method) must be none if the delivery method is deposit.");
            }
        }

        XmlDoc.Element le = se.element("layout");
        if (le == null) {
            throw new Exception("layout element is not found");
        }
        XmlDoc.Element lte = le.element("type");
        if (lte == null) {
            throw new Exception("layout/type is not specifed.");
        }
        Layout.Type lt = Layout.Type.fromString(lte.value());
        if (lt == null) {
            throw new Exception("Failed to parse layout/type: " + lte.value());
        }
        String pattern = le.value("pattern");
        if (lt == Layout.Type.custom) {
            if (pattern == null) {
                throw new Exception("layout/pattern is not specified.");
            }
        }
    }

    public static XmlDoc.Element get(ServiceExecutor executor) throws Throwable {
        XmlDoc.Element se = DarisUserSelfSettings.getElement(executor, ROOT_ELEMENT_NAME);
        if (se == null || !se.hasSubElements()) {
            return null;
        }
        return se;
    }

    public static void set(ServiceExecutor executor, XmlDoc.Element se) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker(ROOT_ELEMENT_NAME);
        dm.add(se, false);
        DarisUserSelfSettings.setElement(executor, dm.root());
    }

    public static void remove(ServiceExecutor executor) throws Throwable {
        DarisUserSelfSettings.removeElement(executor, ROOT_ELEMENT_NAME);
    }

}
