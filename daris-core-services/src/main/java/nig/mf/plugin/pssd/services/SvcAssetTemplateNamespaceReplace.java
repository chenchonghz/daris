package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetTemplateNamespaceReplace extends PluginService {

    public static final String SERVICE_NAME = "daris.asset.template.namespace.replace";

    private Interface _defn;

    public SvcAssetTemplateNamespaceReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("old-namespace", StringType.DEFAULT,
                "The old namespace for the asset template", 1, 1));
        _defn.add(new Interface.Element("new-namespace", StringType.DEFAULT,
                "The new namespace for the asset template", 1, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the asset. If not specified, 'id' must be specified.",
                0, 1));
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The asset id. If not specified, 'cid' must be specified.", 0,
                1));
    }

    public static void replaceAssetTemplateNamespace(ServiceExecutor executor,
            XmlDoc.Element ae, String oldNamespace, String newNamespace)
                    throws Throwable {
        String assetId = ae.value("@id");
        List<XmlDoc.Element> tes = ae.elements("template");
        if (tes == null || tes.isEmpty()) {
            return;
        }
        // remove existing templates
        executor.execute("asset.template.remove",
                "<args><id>" + assetId + "</id></args>", null, null);
        // add updated templates
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        for (XmlDoc.Element te : tes) {
            if (oldNamespace.equals(te.value("@ns"))) {
                te.attribute("ns").setValue(newNamespace);
            }
            dm.add(te, true);
        }
        executor.execute("asset.template.set", dm.root());
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
        return "Set/Change the namespace attribute of the object (asset) template. It is useful when changing the cid of an subject or ex-method.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3)
            throws Throwable {
        final String oldNamespace = args.value("old-namespace");
        final String newNamespace = args.value("new-namespace");

        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new IllegalArgumentException(
                    "No 'id' or 'cid' is specified.");
        }
        if (id != null && cid != null) {
            throw new IllegalArgumentException(
                    "Both 'id' and 'cid' are specified. Expects only one.");
        }

        final XmlDoc.Element ae = getAssetMeta(executor(), cid, id);
        if (!ae.elementExists("template[@ns='" + oldNamespace + "']")) {
            throw new Exception("No template with ns='" + oldNamespace
                    + "' is found in object " + cid);
        }
        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor arg0) throws Throwable {
                replaceAssetTemplateNamespace(executor(), ae, oldNamespace,
                        newNamespace);
                return false;
            }
        }).execute(executor());

    }

    private static XmlDoc.Element getAssetMeta(ServiceExecutor executor,
            String cid, String id) throws Throwable {
        if (cid != null) {
            return executor.execute("asset.get",
                    "<args><cid>" + cid + "</cid></args>", null, null)
                    .element("asset");
        } else {
            return executor.execute("asset.get",
                    "<args><id>" + id + "</id></args>", null, null)
                    .element("asset");
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }
}
