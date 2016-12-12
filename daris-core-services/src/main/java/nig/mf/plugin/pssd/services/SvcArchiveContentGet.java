package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveContentGet extends PluginService {

    public static final String SERVICE_NAME = "daris.archive.content.get";

    private Interface _defn;

    public SvcArchiveContentGet() {
        _defn = new Interface();

        Interface.Element id = new Interface.Element("id", AssetType.DEFAULT,
                "The asset identifier (or path).", 0, 1);
        id.add(new Interface.Attribute("version", IntegerType.POSITIVE,
                "Version of the asset. A value of zero means the latest version. Defaults to zero.",
                0));
        _defn.add(id);

        Interface.Element cid = new Interface.Element("cid",
                CiteableIdType.DEFAULT, "The citeable identifier of the asset.",
                0, 1);
        cid.add(new Interface.Attribute("version", IntegerType.POSITIVE,
                "Version of the asset. A value of zero means the latest version. Defaults to zero.",
                0));
        _defn.add(cid);

        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE,
                "The ordinal position of the file entry. Defaults to 1.", 1,
                1));
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
        return "Retrieves a file entry from the specified asset's content archive. Note: the service wraps asset.archive.content.get service with support of citeable identifier.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        /*
         * parse & validate arguments
         */
        String id = args.value("id");
        String cid = args.value("cid");

        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }

        long idx = args.longValue("idx");

        Integer version = null;
        if (id == null) {
            version = args.intOrNullValue("cid/@version");
            id = idFromCid(executor(), cid, version);
        } else {
            version = args.intOrNullValue("id/@version");
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", new String[] { "version",
                version == null ? null : version.toString() }, id);
        dm.add("idx", idx);

        executor().execute("asset.archive.content.get", dm.root(), null,
                outputs);
    }

    static String idFromCid(ServiceExecutor executor, String cid,
            Integer version) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", new String[] { "version",
                version == null ? null : version.toString() }, cid);
        return executor.execute("asset.get", dm.root()).value("asset/@id");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

}
