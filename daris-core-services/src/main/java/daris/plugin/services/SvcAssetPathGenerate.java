package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.plugin.asset.path.AssetPathCompiler;

public class SvcAssetPathGenerate extends PluginService {

    public static final String SERVICE_NAME = "daris.asset.path.generate";

    private Interface _defn;

    public SvcAssetPathGenerate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "Asset identifier.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "Citeable identifier.", 0, 1));
        _defn.add(new Interface.Element("expr", StringType.DEFAULT, "The expression used to generate the path.", 1, 1));
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
        return "Generates a (file) path for the given result asset using the specified expression.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String assetId = args.value("id");
        String cid = args.value("cid");
        if (cid == null && assetId == null) {
            throw new IllegalArgumentException("id or cid must be specified.");
        }
        if (cid != null && assetId != null) {
            throw new IllegalArgumentException("both id and cid are specified. Expects only one.");
        }
        String expr = args.value("expr");

        XmlDoc.Element assetMeta = ServiceUtils.getAssetMeta(executor(), assetId, cid);
        String assetVersion = assetMeta.value("@version");

        String path = generatePath(executor(), assetMeta, expr);
        w.add("path", new String[] { "id", assetId, "version", assetVersion }, path);
    }

    public static String generatePath(ServiceExecutor executor, XmlDoc.Element assetMeta, String expr)
            throws Throwable {
        String path;
        if (expr.startsWith(AssetPathCompiler.PREFIX)) {
            AssetPathCompiler compiler = AssetPathCompiler.parse(expr);
            path = compiler.compile(executor, assetMeta);
        } else {
            XmlDoc.Element pathElement = executor
                    .execute("asset.path.generate",
                            "<args><id>" + assetMeta.value("@id") + "</id><expr>" + expr + "</expr></args>", null, null)
                    .element("path");
            path = pathElement.value();
        }
        // replace space with underscore
        return path.trim().replaceAll("\\ *\\/\\ *", "/").replaceAll("\\ +", "_");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
