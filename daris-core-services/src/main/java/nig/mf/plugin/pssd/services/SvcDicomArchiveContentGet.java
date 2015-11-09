package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDicomArchiveContentGet extends PluginService {

    public static final String SERVICE_NAME = "daris.dicom.archive.content.get";

    public static final String SERVICE_DESCRIPTION = "retrieve a DICOM file entry from the specified DICOM series asset, which must have zip/aar archive as content and a mf-dicom-series docment in the metadata.";

    private Interface _defn;

    public SvcDicomArchiveContentGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The id of the DICOM series asset", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the DICOM series asset", 0, 1));
        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE,
                "The ordinal position. Defaults to one.", 0, 1));
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
        if (!ae.elementExists("meta/mf-dicom-series")) {
            throw new Exception(
                    "No mf-dicom-series is found in the asset meta. Not a valid DICOM series.");
        }
        int size = ae.intValue("meta/mf-dicom-series/size");
        if (idx > size) {
            throw new IndexOutOfBoundsException(
                    "Invalid idx: " + idx + ". Expects value <= " + size);
        }

        SvcArchiveContentGet.getArchiveEntry(executor, id, cid, idx, ae, so,
                outputs, w);
    }

}
