package nig.mf.plugin.pssd.services;

import java.io.File;
import java.io.FileOutputStream;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveContentGet extends PluginService {
    public static final String SERVICE_NAME = "daris.archive.content.get";

    public static final String SERVICE_DESCRIPTION = "retrieve a file entry from the specified asset's content.";

    private Interface _defn;

    public SvcArchiveContentGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The id of the asset that has the archive as content", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the asset that has the archive as content",
                0, 1));
        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE,
                "The ordinal position. Defaults to 1.", 0, 1));
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
        /*
         * parse & validate arguments
         */
        String id = args.value("id");
        String cid = args.value("cid");
        long idx = args.longValue("idx", 1);
        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }
        if (outputs == null) {
            throw new Exception("Expect 1 out. Found none.");
        }
        if (outputs.size() != 1) {
            throw new Exception("Expect 1 out. Found " + outputs.size() + ".");
        }

        /*
         * get asset metadata & content
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        Outputs sos = new Outputs(1);
        XmlDoc.Element ae = executor()
                .execute("asset.get", dm.root(), null, sos).element("asset");

        /*
         * get archive entry
         */
        getArchiveEntry(executor(), id, cid, idx, ae, sos.output(0), outputs,
                w);
    }

    public static void getArchiveEntry(ServiceExecutor executor, String id,
            String cid, long idx, XmlDoc.Element ae, Output so, Outputs outputs,
            XmlWriter w) throws Throwable {

        StringBuilder sb = new StringBuilder();
        sb.append("asset(");
        if (id != null) {
            sb.append("id=");
            sb.append(id);
        } else {
            sb.append("cid=");
            sb.append(cid);
        }
        sb.append(")");
        String idString = sb.toString();

        XmlDoc.Element ce = ae.element("content");
        if (ce == null || so == null) {
            throw new Exception(idString + " has no content.");
        }

        /*
         * get the entry
         */
        try {
            long csize = ce.longValue("size");
            String ext = ce.value("type/@ext");
            if (!("zip".equalsIgnoreCase(ext) || "jar".equalsIgnoreCase(ext)
                    || "aar".equalsIgnoreCase(ext)
                    || "tar".equalsIgnoreCase(ext))) {
                throw new Exception("Unsupported archive format: " + ext + ".");
            }
            ArchiveInput in = ArchiveRegistry.createInputForExtension(
                    new SizedInputStream(so.stream(), csize), ext,
                    ArchiveInput.ACCESS_RANDOM);
            try {
                ArchiveInput.Entry entry = in.get(((int) idx) - 1);
                if (entry == null) {
                    throw new Exception("Failed to retrieve entry " + idx
                            + " from content of " + idString + ".");
                }
                File tf = PluginService.createTemporaryFile();
                arc.streams.StreamCopy.copy(entry.stream(),
                        new FileOutputStream(tf));
                Output out = outputs.output(0);
                out.setData(PluginService.deleteOnCloseInputStream(tf),
                        entry.size(), null);
                w.add("entry", new String[] { "idx", String.valueOf(idx),
                        "size", String.valueOf(entry.size()) }, entry.name());
            } finally {
                in.close();
            }
        } finally {
            if (so.stream() != null) {
                so.stream().close();
            }
            so.close();
        }

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
