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

import org.apache.commons.io.FileUtils;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.network.StorageSOPClassSCU;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mime.MimeType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.dicom.util.DicomFileCheck;

public class SvcDarisDicomSend extends PluginService {

    public static final String SERVICE_NAME = "daris.dicom.send";

    public static final String SERVICE_DESCRIPTION = "Sends DICOM data to the given DICOM application entity(storageSCP server).";

    private Interface _defn;

    public SvcDarisDicomSend() throws Throwable {

        _defn = new Interface();

        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The asset id of the dicom patient/study/series to be sent.", 0,
                1));

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the dicom patient/study/series to be sent.",
                0, 1));

        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "A query to find the DICOM datasets/series to send.", 0, 1));

        Interface.Element from = new Interface.Element("calling-ae",
                XmlDocType.DEFAULT,
                "The details about calling application entity.", 1, 1);
        from.add(new Interface.Element("title", StringType.DEFAULT,
                "The title of the calling application entity.", 1, 1));
        _defn.add(from);

        Interface.Element to = new Interface.Element("called-ae",
                XmlDocType.DEFAULT,
                "The details about called application entity.", 1, 1);
        to.add(new Interface.Element("host", StringType.DEFAULT,
                "The host address of the called application entity.", 1, 1));
        to.add(new Interface.Element("port", new IntegerType(0, 65535),
                "The port number of the called application entity.", 1, 1));
        to.add(new Interface.Element("title", StringType.DEFAULT,
                "The title of the called application entity.", 1, 1));
        _defn.add(to);

        Interface.Element override = new Interface.Element("override",
                XmlDocType.DEFAULT, "The dicom elements to override.", 0, 1);
        Interface.Element ee = new Interface.Element("element",
                XmlDocType.DEFAULT, "The dicom element to override", 0,
                Integer.MAX_VALUE);
        ee.add(new Interface.Attribute("group",
                new StringType(Pattern.compile("[0-9a-fA-F]{4}")),
                "The group tag of the dicom element.", 1));
        ee.add(new Interface.Attribute("element",
                new StringType(Pattern.compile("[0-9a-fA-F]{4}")),
                "The element tag of the dicom element.", 1));
        ee.add(new Interface.Attribute("anonymize", BooleanType.DEFAULT,
                "Anonymize the element. Defaults to false. If it is set to true, the element value will be ignored.",
                0));
        ee.add(new Interface.Element("value", StringType.DEFAULT,
                "The new value of the dicom element. Only required if anonymize is false.",
                0, 1));
        override.add(ee);
        _defn.add(override);
    }

    @Override
    public String name() {

        return SERVICE_NAME;
    }

    @Override
    public String description() {

        return SERVICE_DESCRIPTION;
    }

    @Override
    public Interface definition() {

        return _defn;
    }

    @Override
    public Access access() {

        return ACCESS_ACCESS;
    }

    @Override
    public boolean canBeAborted() {

        return true;
    }

    private static void addDicomPaitent(ServiceExecutor executor,
            Set<String> datasetIds, XmlDoc.Element patientAsset)
                    throws Throwable {
        Collection<String> studyIds = patientAsset
                .values("related[@type='has']/to");
        for (String studyId : studyIds) {
            XmlDoc.Element studyAsset = executor
                    .execute("asset.get",
                            "<args><id>" + studyId + "</id></args>", null, null)
                    .element("asset");
            addDicomStudy(executor, datasetIds, studyAsset);
        }
    }

    private static void addDicomStudy(ServiceExecutor executor,
            Set<String> datasetIds, XmlDoc.Element studyAsset)
                    throws Throwable {
        if (studyAsset.elementExists("related[@type='contains']/to")) {
            datasetIds
                    .addAll(studyAsset.values("related[@type='contains']/to"));
        }
    }

    private static void addPssdDicomStudy(ServiceExecutor executor,
            Set<String> datasetIds, String studyCid) throws Throwable {
        Collection<String> ids = executor
                .execute("asset.query",
                        "<args><size>infinity</size><where>asset has content and mf-dicom-series has value and cid in '"
                                + studyCid + "'</where></args>",
                        null, null)
                .values("id");
        if (ids != null && !ids.isEmpty()) {
            datasetIds.addAll(ids);
        }
    }

    private static void addPssdDicomSubject(ServiceExecutor executor,
            Set<String> datasetIds, String subjectCid) throws Throwable {
        Collection<String> ids = executor
                .execute("asset.query",
                        "<args><size>infinity</size><where>asset has content and mf-dicom-series has value and cid starts with '"
                                + subjectCid + "'</where></args>",
                        null, null)
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
                AttributeTag tag = new AttributeTag(Integer.parseInt(group, 16),
                        Integer.parseInt(element, 16));
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
            ArchiveInput ai = ArchiveRegistry.createInput(
                    new SizedInputStream(output.stream(), csize), mtype);
            try {
                ArchiveExtractor.extract(ai, dir, false, true, false);
            } finally {
                ai.close();
            }
        } else {
            File of = new File(dir,
                    cext == null ? assetId : (assetId + "." + cext));
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

    private static SetOfDicomFiles listDicomFilesRecursively(File dir)
            throws Throwable {
        List<File> files = new Vector<File>();
        listFilesRecursively(dir, files);
        SetOfDicomFiles dicomFiles = new SetOfDicomFiles();
        for (File f : files) {
            if (DicomFileCheck.isDicomFile(f)) {
                dicomFiles.add(f);
            }
        }
        return dicomFiles;
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

    private static void addByAssetMeta(ServiceExecutor executor,
            XmlDoc.Element ae, Set<String> datasetAssetIds) throws Throwable {
        if (!ae.elementExists("meta/mf-dicom-patient")
                && !ae.elementExists("meta/mf-dicom-study")
                && !ae.elementExists("meta/mf-dicom-series")
                && !"om.pssd.subject".equals(ae.value("model"))) {
            // Not a DICOM object.
            return;
        }
        String id = ae.value("@id");
        String cid = ae.value("cid");
        if (ae.elementExists("meta/mf-dicom-series")) {
            datasetAssetIds.add(id);
        } else if (ae.elementExists("meta/mf-dicom-study")) {
            if (ae.elementExists("related[@type='contains']/to")) {
                addDicomStudy(executor, datasetAssetIds, ae);
            } else if (cid != null) {
                addPssdDicomStudy(executor, datasetAssetIds, cid);
            }
        } else if (ae.elementExists("meta/mf-dicom-patient")) {
            if (ae.elementExists("related[@type='has']/to")) {
                addDicomPaitent(executor, datasetAssetIds, ae);
            } else if (cid != null) {
                addPssdDicomSubject(executor, datasetAssetIds, cid);
            }
        } else if ("om.pssd.subject".equals(ae.value("model")) && cid != null) {
            addPssdDicomSubject(executor, datasetAssetIds, cid);
        }
    }

    private static void addByAssetId(ServiceExecutor executor, String assetId,
            Set<String> datasetAssetIds) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + assetId + "</id></args>", null, null)
                .element("asset");
        addByAssetMeta(executor, ae, datasetAssetIds);
    }

    private static void addByCiteableId(ServiceExecutor executor,
            String citeableId, Set<String> datasetAssetIds) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><cid>" + citeableId + "</cid></args>", null, null)
                .element("asset");
        addByAssetMeta(executor, ae, datasetAssetIds);
    }

    private static void addByQuery(ServiceExecutor executor, String where,
            Set<String> datasetAssetIds) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(where).append(
                ") and asset has content and mf-dicom-series has value");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        dm.add("size", "infinity");
        Collection<String> ids = executor.execute("asset.query", dm.root())
                .values("id");
        if (ids != null && !ids.isEmpty()) {
            datasetAssetIds.addAll(ids);
        }
    }

    @Override
    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {
        /*
         * parse arguments
         */
        String id = args.value("id");
        String cid = args.value("cid");
        String where = args.value("where");
        if (id == null && cid == null && where == null) {
            throw new Exception("Argument: id, cid or where is required.");
        }
        String callingAETitle = args.value("calling-ae/title");
        String calledAETitle = args.value("called-ae/title");
        String calledAEHost = args.value("called-ae/host");
        int calledAEPort = args.intValue("called-ae/port");
        Map<AttributeTag, String> override = null;
        if (args.elementExists("override")) {
            override = parseOverriddenElements(args.element("override"));
        }

        /*
         * add DICOM datasets
         */
        Set<String> datasetAssetIds = new TreeSet<String>();
        PluginTask.setCurrentThreadActivity("Adding DICOM datasets/series...");
        if (id != null) {
            PluginTask.checkIfThreadTaskAborted();
            addByAssetId(executor(), id, datasetAssetIds);
        }
        if (cid != null) {
            PluginTask.checkIfThreadTaskAborted();
            addByCiteableId(executor(), cid, datasetAssetIds);
        }
        if (where != null) {
            PluginTask.checkIfThreadTaskAborted();
            addByQuery(executor(), where, datasetAssetIds);
        }
        if (datasetAssetIds.isEmpty()) {
            // No DICOM dataset/series found.
            return;
        }

        /*
         * extract dicom files
         */
        PluginTask.setCurrentThreadActivity("Extracting DICOM files...");
        PluginTask.checkIfThreadTaskAborted();

        File dir = PluginTask.createTemporaryDirectory();
        try {
            for (String datasetAssetId : datasetAssetIds) {
                PluginTask.setCurrentThreadActivity(
                        "Extracting dicom files from asset " + datasetAssetId
                                + "...");
                PluginTask.checkIfThreadTaskAborted();
                File assetDir = new File(dir, datasetAssetId);
                assetDir.mkdir();
                extractAssetContent(executor(), datasetAssetId, assetDir);
                if (override != null && !override.isEmpty()) {
                    PluginTask.setCurrentThreadActivity(
                            "Editting dicom files from asset " + datasetAssetId
                                    + "...");
                    PluginTask.checkIfThreadTaskAborted();
                    editDicomFiles(assetDir, override, callingAETitle);
                }
            }
            SetOfDicomFiles dicomFiles = listDicomFilesRecursively(dir);
            int total = dicomFiles.size();
            PluginTask.checkIfThreadTaskAborted();
            PluginTask.setCurrentThreadActivity("Sending dicom data...");
            final int[] result = new int[4];
            new StorageSOPClassSCU(calledAEHost, calledAEPort, calledAETitle,
                    callingAETitle, dicomFiles, 0,
                    new MultipleInstanceTransferStatusHandler() {

                        @Override
                        public void updateStatus(int nRemaining, int nCompleted,
                                int nFailed, int nWarning,
                                String sopInstanceUID) {
                            PluginTask.threadTaskCompletedMultipleOf(nCompleted,
                                    nRemaining + nCompleted);
                            result[0] = nRemaining;
                            result[1] = nCompleted;
                            result[2] = nFailed;
                            result[3] = nWarning;
                        }
                    }, null, 0, 0);
            int nCompleted = result[1];
            int nFailed = result[2];
            w.add("sent",
                    new String[] { "completed", Integer.toString(nCompleted),
                            "failed", Integer.toString(nFailed), "total",
                            Integer.toString(total) },
                    nFailed <= 0);
            PluginTask.threadTaskCompleted();
        } finally {
            forceDelete(dir);
        }
    }
}
