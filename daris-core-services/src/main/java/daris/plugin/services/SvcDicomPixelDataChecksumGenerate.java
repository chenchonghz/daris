package daris.plugin.services;

import java.io.BufferedInputStream;
import java.io.InputStream;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mime.NamedMimeType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;
import daris.dicom.util.DicomChecksumUtils;

public class SvcDicomPixelDataChecksumGenerate extends PluginService {

    public static final String DOC_TYPE = "daris:dicom-pixel-data-checksum";

    public static final String SERVICE_NAME = "daris.dicom.pixel-data.checksum.generate";

    public static class PixelDataChecksum {
        public final String SOPInstanceUID;
        public final AttributeTag tag;
        public final String VR;
        public final long VL;
        public final boolean bigEndian;
        public final String checksum;
        public final String checksumType;

        PixelDataChecksum(String SOPInstanceUID, AttributeTag tag, String VR, long VL, boolean bigEndian,
                String checksum, String checksumType) {
            this.SOPInstanceUID = SOPInstanceUID;
            this.tag = tag;
            this.VR = VR;
            this.VL = VL;
            this.bigEndian = bigEndian;
            this.checksum = checksum;
            this.checksumType = checksumType;
        }

        PixelDataChecksum(String SOPInstanceUID, Attribute pixelDataAttr, boolean bigEndian, String checksum,
                String checksumType) {
            this(SOPInstanceUID, pixelDataAttr.getTag(), pixelDataAttr.getVRAsString(), pixelDataAttr.getVL(),
                    bigEndian, checksum, checksumType);
        }

        public String tagAsString() {
            return String.format("%04X%04X", this.tag.getGroup(), this.tag.getElement());
        }
    }

    private static PixelDataChecksum getPixelDataChecksum(ServiceExecutor executor, XmlDoc.Element args)
            throws Throwable {

        String checksumType = args.value("type");
        long idx = args.longValue("idx", 0);
        String id = args.value("id");
        String cid = args.value("cid");

        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        Outputs os = new Outputs(1);
        XmlDoc.Element ae = executor.execute("asset.get", dm.root(), null, os).element("asset");
        if (id == null) {
            id = ae.value("@id");
        }
        if (cid == null) {
            cid = ae.value("cid");
        }
        String ctype = ae.value("content/type");
        long csize = ae.longValue("content/size");
        if (!ae.elementExists("meta/mf-dicom-series") || !ae.elementExists("content")
                || !"dicom/series".equals(ae.value("type")) || ctype == null) {
            throw new Exception("Asset " + ae.value("@id") + " is not a valid dicom/series asset.");
        }

        Output o = os.output(0);
        ArchiveInput ai = ArchiveRegistry.createInput(
                new arc.streams.SizedInputStream(o.stream(), o.length() <= 0 ? csize : o.length()),
                new NamedMimeType(ctype));
        try {
            ArchiveInput.Entry e = ai.get(idx);
            if (e.isDirectory()) {
                throw new Exception("The " + idx + "th entry is not a dicom file but a directory.");
            }
            return getPixelDataChecksum(e.stream(), checksumType);
        } finally {
            ai.close();
        }
    }

    private static PixelDataChecksum getPixelDataChecksum(ServiceExecutor executor, Input input, String checksumType)
            throws Throwable {
        try {
            return getPixelDataChecksum(input.stream(), checksumType);
        } finally {
            input.stream().close();
        }
    }

    private static PixelDataChecksum getPixelDataChecksum(InputStream in, String checksumType) throws Throwable {
        DicomInputStream dis = new DicomInputStream(new BufferedInputStream(in));
        try {
            AttributeList l = new AttributeList();
            l.read(dis);
            Attribute pixelDataAttr = l.getPixelData();
            if (pixelDataAttr == null) {
                throw new Exception("No PixelData element found.");
            }
            String SOPInstanceUID = Attribute.getSingleStringValueOrNull(l, TagFromName.SOPInstanceUID);
            String transferSyntaxUID = Attribute.getSingleStringValueOrDefault(l, TagFromName.TransferSyntaxUID,
                    TransferSyntax.DeflatedExplicitVRLittleEndian);
            boolean bigEndian = TransferSyntax.isBigEndian(transferSyntaxUID);
            String checksum = DicomChecksumUtils.getPixelDataChecksum(pixelDataAttr, bigEndian, checksumType);
            return new PixelDataChecksum(SOPInstanceUID, pixelDataAttr, bigEndian, checksum, checksumType);
        } finally {
            dis.close();
        }
    }

    private static void savePixelDataChecksum(Long idx, PixelDataChecksum info, XmlWriter w) throws Throwable {
        w.push("object", new String[] { "idx", idx == null ? null : Long.toString(idx), "uid", info.SOPInstanceUID,
                "big-endian", Boolean.toString(info.bigEndian) });
        w.push("pixel-data", new String[] { "tag", info.tagAsString(), "vr", info.VR, "vl", Long.toString(info.VL) });
        w.add("csum", new String[] { "type", info.checksumType }, info.checksum);
        w.pop();
        w.pop();
    }

    private static void savePixelDataChecksum(ServiceExecutor executor, String id, long idx, PixelDataChecksum info)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        XmlDocWriter w = new XmlDocWriter(dm);
        w.add("id", id);
        w.push("meta");
        w.push(DOC_TYPE);
        savePixelDataChecksum(idx, info, w);
        w.pop();
        w.pop();
        executor.execute("asset.set", dm.root());
    }

    private Interface _defn;

    public SvcDicomPixelDataChecksumGenerate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "Asset id of the DICOM series. Either id or cid must be specified.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "Citeable id of the DICOM series. Either id or cid must be specified.", 0, 1));
        _defn.add(new Interface.Element("idx", IntegerType.POSITIVE,
                "This specifies the idx'th file to start analyzing. Starts from zero. Defaults to zero.", 0, 1));
        _defn.add(new Interface.Element("save", BooleanType.DEFAULT,
                "Save the result checksum to asset metadata. Defaults to false.", 0, 1));
        _defn.add(new Interface.Element("type", new EnumType(new String[] { "crc32", "md5", "sha1", "sha256" }),
                "The type/algorithm of the checksum.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Calculate the checksum for the PixelData (7FE0,0010) of a DICOM file object in a specified DICOM (series) asset, or a given service input (URL). If DICOM asset is specified, the checksum can be optionally saved to asset metadata.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        PluginService.Input input = (inputs == null || inputs.size() <= 0) ? null : inputs.input(0);

        String type = args.value("type");
        String id = args.value("id");
        String cid = args.value("cid");

        PixelDataChecksum pixelDataInfo = null;
        if (input == null) {
            if (id == null && cid == null) {
                throw new IllegalArgumentException("Asset id (or cid), or service input (URL) must be specified.");
            }
            if (id != null && cid != null) {
                throw new IllegalArgumentException("Both id and cid are specified. Expects only one.");
            }
            pixelDataInfo = getPixelDataChecksum(executor(), args);
            boolean save = args.booleanValue("save", false);
            long idx = args.longValue("idx", 0);
            if (save) {
                savePixelDataChecksum(executor(), id, idx, pixelDataInfo);
            }
        } else {
            if (id != null || cid != null) {
                throw new IllegalArgumentException(
                        "Both asset id/cid and service input (URL) are specified. Expects only one.");
            }
            pixelDataInfo = getPixelDataChecksum(executor(), input, type);
        }
        if (pixelDataInfo != null) {
            savePixelDataChecksum(null, pixelDataInfo, w);
        }
    }

    @Override
    public int maxNumberOfInputs() {
        return 1;
    }

    @Override
    public int minNumberOfInputs() {
        return 0;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
