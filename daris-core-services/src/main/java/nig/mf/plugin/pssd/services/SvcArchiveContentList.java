package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveContentList extends PluginService {

    public static final String SERVICE_NAME = "daris.archive.content.list";

    public static final String SERVICE_DESCRIPTION = "list the archive entries of the given asset's content.";

    private Interface _defn;

    public SvcArchiveContentList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The id of the asset with an archive as its content.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the asset with an archive as its content.",
                0, 1));
        _defn.add(new Interface.Element("size", LongType.POSITIVE_ONE,
                "The number of entries to list. Defaults to 100", 0, 1));
        _defn.add(new Interface.Element("idx", IntegerType.POSITIVE_ONE,
                "The start ordinal position. Defaults to 1.", 0, 1));

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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }
        int size = args.intValue("size", 100);
        long idx = args.longValue("idx", 1);
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        Outputs sos = new Outputs(1);
        Output so = null;
        try {
            XmlDoc.Element ae = executor()
                    .execute("asset.get", dm.root(), null, sos)
                    .element("asset");
            so = sos.output(0);
            XmlDoc.Element ce = ae.element("content");
            if (ce == null || so == null) {
                throw new Exception(
                        "asset(" + (id == null ? ("cid=" + cid) : ("id=" + id))
                                + ") has not content.");
            }
            String ctype = ce.value("type");
            String ext = ce.value("type/@ext");
            long csize = ce.longValue("size");
            if (!("zip".equalsIgnoreCase(ext) || "jar".equalsIgnoreCase(ext)
                    || "aar".equalsIgnoreCase(ext)
                    || "tar".equalsIgnoreCase(ext))) {
                throw new Exception("Unsupported archive format: " + ext);
            }
            dm = new XmlDocMaker("args");
            dm.add("format", ext);
            dm.add("size", size);
            dm.add("idx", idx);
            Input si = new Input(so.stream(), csize, ctype, null);
            try {
                XmlDoc.Element re = executor().execute("archive.content.list",
                        dm.root(), new Inputs(si), null);
                w.add(re, false);
            } finally {
                si.close();
            }
        } finally {
            if (so != null) {
                if (so.stream() != null) {
                    so.stream().close();
                }
                so.close();
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
