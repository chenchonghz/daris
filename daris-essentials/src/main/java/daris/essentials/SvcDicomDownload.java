package daris.essentials;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import nig.dicom.util.DicomFileCheck;

import org.apache.commons.io.FileUtils;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mime.MimeType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

/*
 * TBD - this service has PSSD knowledge.  It shoulod really be in daris-core-services not here in essentials
 * 
 */
public class SvcDicomDownload extends PluginService {

    public static final String SERVICE_NAME = "daris.dicom.download";
    public static final String SERVICE_DESCRIPTION = "Download the data sets within the specified subject/patient/study/series";
    public static final String DEFAULT_AE_TITLE = "DaRIS";
    public static final String MIME_TYPE_ZIP = "application/zip";
    public static final String MIME_TYPE_AAR = "application/arc-archive";
    public static final int COMPRESSION_LEVEL = 6;

    public static enum ArchiveType {
        aar("aar", MIME_TYPE_AAR), zip("zip", MIME_TYPE_ZIP);
        public final String extension;
        public final String mimeType;

        ArchiveType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }

        public static ArchiveType fromString(String s) {
            if (s != null) {
                ArchiveType[] vs = values();
                for (ArchiveType v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private Interface _defn;

    public SvcDicomDownload() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The asset id of the dicom patient/study/series to download.",
                0, 1));

        _defn.add(new Interface.Element(
                "cid",
                CiteableIdType.DEFAULT,
                "The citeable id of the dicom patient/study/series to download.",
                0, 1));

        Interface.Element override = new Interface.Element("override",
                XmlDocType.DEFAULT, "The dicom elements to override.", 0, 1);
        Interface.Element ee = new Interface.Element("element",
                XmlDocType.DEFAULT, "The dicom element to override", 0,
                Integer.MAX_VALUE);
        ee.add(new Interface.Attribute("group", new StringType(Pattern
                .compile("[0-9a-fA-F]{4}")),
                "The group tag of the dicom element.", 1));
        ee.add(new Interface.Attribute("element", new StringType(Pattern
                .compile("[0-9a-fA-F]{4}")),
                "The element tag of the dicom element.", 1));
        ee.add(new Interface.Attribute(
                "anonymize",
                BooleanType.DEFAULT,
                "Anonymize the element. Defaults to false. If it is set to true, the element value will be ignored.",
                0));
        ee.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The new value of the dicom element. Only required if anonymize is false.",
                0, 1));
        override.add(ee);
        _defn.add(override);

        _defn.add(new Interface.Element("atype", new EnumType(ArchiveType
                .values()), "The type of archive to output. Defaults to zip.",
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new Exception("Either id or cid is required. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("Either id or cid is required. Found both.");
        }
        ArchiveType aType = ArchiveType.fromString(args.stringValue("atype",
                "zip"));
        Map<AttributeTag, String> override = null;
        if (args.elementExists("override")) {
            override = parseOverriddenElements(args.element("override"));
        }
        XmlDoc.Element ae = null;
        if (cid == null) {
            ae = executor().execute("asset.get",
                    "<args><id>" + id + "</id></args>", null, null).element(
                    "asset");
            cid = ae.value("cid");
        } else {
            ae = executor().execute("asset.get",
                    "<args><cid>" + cid + "</cid></args>", null, null).element(
                    "asset");
            id = ae.value("@id");
        }
        if (!ae.elementExists("meta/mf-dicom-patient")
                && !ae.elementExists("meta/mf-dicom-study")
                && !ae.elementExists("meta/mf-dicom-series")
                && !"om.pssd.subject".equals(ae.value("model"))) {
            throw new Exception(
                    "asset "
                            + id
                            + " is not a valid dicom patient/study/series or a daris pssd subject.");
        }

        /*
         * add data sets
         */
        Set<String> datasetIds = new TreeSet<String>();
        PluginTask.setCurrentThreadActivity("Adding dicom series(dataset)...");
        PluginTask.checkIfThreadTaskAborted();
        if (ae.elementExists("meta/mf-dicom-series")) {
            datasetIds.add(id);
        } else if (ae.elementExists("meta/mf-dicom-study")) {
            if (ae.elementExists("related[@type='contains']/to")) {
                addDicomStudy(executor(), datasetIds, ae);
            } else if (cid != null) {
                addPssdDicomStudy(executor(), datasetIds, cid);
            }
        } else if (ae.elementExists("meta/mf-dicom-patient")) {
            if (ae.elementExists("related[@type='has']/to")) {
                addDicomPaitent(executor(), datasetIds, ae);
            } else if (cid != null) {
                addPssdDicomSubject(executor(), datasetIds, cid);
            }
        } else if ("om.pssd.subject".equals(ae.value("model")) && cid != null) {
            addPssdDicomSubject(executor(), datasetIds, cid);
        }
        if (datasetIds.isEmpty()) {
            throw new Exception("No dicom series(dataset) is found in asset.");
        }

        /*
         * extract dicom files
         */
        PluginTask.setCurrentThreadActivity("Extracting dicom files...");
        PluginTask.checkIfThreadTaskAborted();

        File dir = PluginTask.createTemporaryDirectory();
        try {
            for (String datasetId : datasetIds) {
                PluginTask
                        .setCurrentThreadActivity("Extracting dicom files from asset "
                                + datasetId + "...");
                PluginTask.checkIfThreadTaskAborted();
                File assetDir = new File(dir, datasetId);
                assetDir.mkdir();
                extractAssetContent(executor(), datasetId, assetDir);
                if (override != null && !override.isEmpty()) {
                    PluginTask
                            .setCurrentThreadActivity("Editting dicom files from asset "
                                    + datasetId + "...");
                    PluginTask.checkIfThreadTaskAborted();
                    editDicomFiles(assetDir, override, DEFAULT_AE_TITLE);
                }
            }
            PluginTask
                    .setCurrentThreadActivity("Adding dicom files to an archive...");
            File of = PluginTask.createTemporaryFile("dcm." + aType.extension);
            ArchiveOutput ao = ArchiveRegistry.createOutput(of, aType.mimeType,
                    COMPRESSION_LEVEL, null);
            try {
                File[] subDirs = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                });
                if (subDirs.length == 1) {
                    addDicomDirToArchive(ao, subDirs[0], false);
                } else {
                    for (File subDir : subDirs) {
                        addDicomDirToArchive(ao, subDir, true);
                    }
                }
            } finally {
                ao.close();
            }
            outputs.output(0).setData(
                    new PluginTask.DeleteOnCloseFileInputStream(of),
                    of.length(), aType.mimeType);
            for (String datasetId : datasetIds) {
                w.add("id", datasetId);
            }
            PluginTask.threadTaskCompleted();
        } finally {
            forceDelete(dir);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    private static void addDicomPaitent(ServiceExecutor executor,
            Set<String> datasetIds, XmlDoc.Element patientAsset)
            throws Throwable {
        Collection<String> studyIds = patientAsset
                .values("related[@type='has']/to");
        for (String studyId : studyIds) {
            XmlDoc.Element studyAsset = executor.execute("asset.get",
                    "<args><id>" + studyId + "</id></args>", null, null)
                    .element("asset");
            addDicomStudy(executor, datasetIds, studyAsset);
        }
    }

    private static void addDicomStudy(ServiceExecutor executor,
            Set<String> datasetIds, XmlDoc.Element studyAsset) throws Throwable {
        if (studyAsset.elementExists("related[@type='contains']/to")) {
            datasetIds
                    .addAll(studyAsset.values("related[@type='contains']/to"));
        }
    }

    private static void addPssdDicomStudy(ServiceExecutor executor,
            Set<String> datasetIds, String studyCid) throws Throwable {
        Collection<String> ids = executor
                .execute(
                        "asset.query",
                        "<args><size>infinity</size><where>asset has content and mf-dicom-series has value and cid in '"
                                + studyCid + "'</where></args>", null, null)
                .values("id");
        if (ids != null && !ids.isEmpty()) {
            datasetIds.addAll(ids);
        }
    }

    private static void addPssdDicomSubject(ServiceExecutor executor,
            Set<String> datasetIds, String subjectCid) throws Throwable {
        Collection<String> ids = executor
                .execute(
                        "asset.query",
                        "<args><size>infinity</size><where>asset has content and mf-dicom-series has value and cid starts with '"
                                + subjectCid + "'</where></args>", null, null)
                .values("id");
        if (ids != null && !ids.isEmpty()) {
            datasetIds.addAll(ids);
        }
    }

    private static Map<AttributeTag, String> parseOverriddenElements(
            XmlDoc.Element oe) throws Throwable {
        List<XmlDoc.Element> ees = oe.elements("element");
        if (ees != null) {
            Map<AttributeTag, String> map = new TreeMap<AttributeTag, String>();
            for (XmlDoc.Element ee : ees) {
                String group = ee.value("@group");
                String element = ee.value("@element");
                AttributeTag tag = new AttributeTag(
                        Integer.parseInt(group, 16), Integer.parseInt(element,
                                16));
                if (ee.booleanValue("@anonymize", false)) {
                    map.put(tag, " ");
                } else {
                    String value = ee.value("value");
                    if (value != null) {
                        map.put(tag, value);
                    } else {
                        throw new Exception(
                                "Missing value for override/element[@group='"
                                        + group + "' @element='" + element
                                        + "']/value");
                    }
                }
            }
            if (!map.isEmpty()) {
                return map;
            }
        }
        return null;
    }

    private static void extractAssetContent(ServiceExecutor executor,
            String assetId, File dir) throws Throwable {
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + assetId + "</id></args>", null, outputs)
                .element("asset");
        PluginService.Output output = outputs.output(0);
        if (output == null) {
            throw new Exception("No content found in asset " + assetId + ".");
        }
        String ctype = output.mimeType();
        if (ctype == null) {
            ctype = ae.value("content/type");
        }
        long csize = output.length();
        if (csize < 0) {
            csize = ae.longValue("content/size", -1);
        }
        String cext = null;
        if (!MimeType.CONTENT_UNKNOWN.equals(ctype)) {
            cext = ae.value("content/type/@ext");
        }
        MimeType mtype = new NamedMimeType(ctype);
        if (ArchiveRegistry.isAnArchive(mtype)) {
            ArchiveInput ai = ArchiveRegistry.createInput(new SizedInputStream(
                    output.stream(), csize), mtype);
            try {
                ArchiveExtractor.extract(ai, dir, false, true, false);
            } finally {
                ai.close();
            }
        } else {
            File of = new File(dir, cext == null ? assetId
                    : (assetId + "." + cext));
            InputStream is = output.stream();
            try {
                StreamCopy.copy(is, of);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private static void editDicomFile(File f,
            Map<AttributeTag, String> overriddenValues, String fromAET)
            throws Throwable {
        AttributeList list = new AttributeList();
        list.read(f);
        Attribute mediaStorageSOPClassUIDAttr = list
                .get(TagFromName.MediaStorageSOPClassUID);
        String mediaStorageSOPClassUID = null;
        if (mediaStorageSOPClassUIDAttr != null) {
            mediaStorageSOPClassUID = mediaStorageSOPClassUIDAttr
                    .getSingleStringValueOrNull();
        }
        Attribute mediaStorageSOPInstanceUIDAttr = list
                .get(TagFromName.MediaStorageSOPInstanceUID);
        String mediaStorageSOPInstanceUID = null;
        if (mediaStorageSOPInstanceUIDAttr != null) {
            mediaStorageSOPInstanceUID = mediaStorageSOPInstanceUIDAttr
                    .getSingleStringValueOrNull();
        }
        // String implementationClassUID =
        // list.get(TagFromName.ImplementationClassUID).getSingleStringValueOrNull();
        // String implementationVersionName =
        // list.get(TagFromName.ImplementationVersionName).getSingleStringValueOrNull();
        /*
         * Cleanup
         */
        list.removeGroupLengthAttributes();
        list.removeMetaInformationHeaderAttributes();
        list.remove(TagFromName.DataSetTrailingPadding);
        list.correctDecompressedImagePixelModule();
        list.insertLossyImageCompressionHistoryIfDecompressed();
        if (mediaStorageSOPClassUID != null
                && mediaStorageSOPInstanceUID != null) {
            FileMetaInformation.addFileMetaInformation(list,
                    mediaStorageSOPClassUID, mediaStorageSOPInstanceUID,
                    TransferSyntax.ExplicitVRLittleEndian, fromAET);
        } else {
            FileMetaInformation.addFileMetaInformation(list,
                    TransferSyntax.ExplicitVRLittleEndian, fromAET);
        }
        // Put the new tag in place
        if (overriddenValues != null && !overriddenValues.isEmpty()) {
            for (AttributeTag aTag : overriddenValues.keySet()) {
                String aValue = overriddenValues.get(aTag);
                Attribute attr = list.get(aTag);
                if (attr != null) {
                    attr.setValue(overriddenValues.get(aTag));
                } else {
                    list.putNewAttribute(aTag).addValue(aValue);
                }
            }
        }
        list.write(new FileOutputStream(f),
                TransferSyntax.ExplicitVRLittleEndian, true, true);
    }

    private static void editDicomFiles(File dir,
            Map<AttributeTag, String> overriddenValues, String fromAET)
            throws Throwable {
        List<File> files = new Vector<File>();
        listFilesRecursively(dir, files);
        for (File f : files) {
            if (DicomFileCheck.isDicomFile(f)) {
                editDicomFile(f, overriddenValues, fromAET);
            }
        }
    }

    private static void listFilesRecursively(File dir, final List<File> files) {
        File[] fs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    listFilesRecursively(f, files);
                    return false;
                } else {
                    return true;
                }
            }
        });
        if (fs != null) {
            for (File f : fs) {
                files.add(f);
            }
        }
    }

    private static void forceDelete(File file) throws Throwable {
        try {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                FileUtils.forceDelete(file);
            }
        } catch (Throwable e) {
            FileUtils.forceDeleteOnExit(file);
        }
    }

    private static void addDicomDirToArchive(ArchiveOutput ao, File dir,
            boolean parent) throws Throwable {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (DicomFileCheck.isDicomFile(file)) {
                String name = parent ? (dir.getName() + File.separator + file
                        .getName()) : file.getName();
                ao.add("application/dicom", name, file);
            }
        }
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
