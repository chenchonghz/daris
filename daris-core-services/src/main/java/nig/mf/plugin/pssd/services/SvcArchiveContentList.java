package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcArchiveContentList extends PluginService {

    public static final String SERVICE_NAME = "daris.archive.content.list";

    private Interface _defn;

    public SvcArchiveContentList() {
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
                "The start ordinal position. Defaults to 1.", 0, 1));

        _defn.add(new Interface.Element("eidx", LongType.POSITIVE_ONE,
                "The end ordinal position - that is, the position from the end (specify 1 for the last entry in the archive). Ignored if 'idx' specified.",
                0, 1));

        _defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
                "The number of entries to list. Defaults to 100.", 0, 1));

        _defn.add(new Interface.Element("suffix", StringType.DEFAULT,
                "An optional filter listing only entries that match the supplied extension. If supplied then you can not provide an expression based filter.",
                0, 1));

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
        return "Returns a listing of the contents of an asset archive - asset must be an archive.";
    }

    @Override
    public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (args.elementExists("id") && args.elementExists("cid")) {
            throw new Exception("id or cid is expected. Found both.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }
        if (id == null) {
            Integer version = args.intOrNullValue("cid/@version");
            id = SvcArchiveContentGet.idFromCid(executor(), cid, version);
            args.remove(args.element("cid"));
            XmlDoc.Element ide = new XmlDoc.Element("id");
            ide.setValue(id);
            if (version != null) {
                ide.add(new XmlDoc.Attribute("version", version));
            }
            args.add(ide);
        }
        XmlDoc.Element re = executor().execute("asset.archive.content.list",
                args);
        w.add(re, false);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
