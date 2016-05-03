package nig.mf.plugin.pssd.services;

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
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.network.StorageSOPClassSCU;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginLog;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.Session;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mime.MimeType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc.Element;
import nig.dicom.util.DicomFileCheck;
import nig.mf.pssd.CiteableIdUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDicomSend extends PluginService {

    public static final String SERVICE_NAME = "daris.dicom.send";

    public static final String LOG_NAME = "dicom-send";

    static class Logger {

        private String _prefix;

        Logger(String callingAET, String calledAET, String host, int port)
                throws Throwable {
            StringBuilder sb = new StringBuilder();
            if (Session.user() != null) {
                sb.append("[").append(Session.user().domain()).append(":")
                        .append(Session.user().name()).append("]");
            }
            sb.append("[calling AE:").append(callingAET).append(", called AE:")
                    .append(calledAET).append("@").append(host).append(":")
                    .append(port).append("] ");
            _prefix = sb.toString();
        }

        public void logInfo(String msg) {
            StringBuilder sb = new StringBuilder(_prefix);
            PluginLog.log(LOG_NAME).add(PluginLog.WARNING,
                    sb.append(msg).toString());
        }

        public void logWarning(String msg) {
            StringBuilder sb = new StringBuilder(_prefix);
            PluginLog.log(LOG_NAME).add(PluginLog.WARNING,
                    sb.append(msg).toString());
        }

        public void logError(String msg, Throwable t) {
            StringBuilder sb = new StringBuilder(_prefix);
            PluginLog.log(LOG_NAME).add(PluginLog.ERROR,
                    sb.append(msg).toString(), t);
        }

        public void logError(String msg) {
            StringBuilder sb = new StringBuilder(_prefix);
            PluginLog.log(LOG_NAME).add(PluginLog.ERROR,
                    sb.append(msg).toString());
        }
    }

    public static enum ElementName {
        PATIENT_NAME("patient.name", "00100010"), PATIENT_ID("patient.id",
                "00100020"), STUDY_ID("study.id",
                        "00200010"), PERFORMING_PHYSICIAN_NAME(
                                "performing.physician.name",
                                "00081050"), REFERRING_PHYSICIAN_NAME(
                                        "referring.physician.name",
                                        "00080090"), REFERRING_PHYSICIAN_PHONE(
                                                "referring.physician.phone",
                                                "00080094");
        private String _stringValue;
        private String _tag;

        ElementName(String stringValue, String tag) {
            _stringValue = stringValue;
            _tag = tag;
        }

        @Override
        public final String toString() {
            return _stringValue;
        }

        public String tag() {
            return _tag;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static final String[] stringValues() {
            ElementName[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].stringValue();
            }
            return svs;
        }

        public static ElementName fromString(String s) {
            if (s != null) {
                ElementName[] vs = values();
                for (ElementName v : vs) {
                    if (s.equalsIgnoreCase(v.stringValue())) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    public static enum ValueReference {
        SUBJECT_CID("subject.cid"), STUDY_CID("study.cid"), PATIENT_NAME(
                "patient.name"), PATIENT_ID("patient.id");

        private String _stringValue;

        ValueReference(String stringValue) {
            _stringValue = stringValue;
        }

        @Override
        public final String toString() {
            return _stringValue;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static final String[] stringValues() {
            ValueReference[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].stringValue();
            }
            return svs;
        }

        public static ValueReference fromString(String s) {
            if (s != null) {
                ValueReference[] vs = values();
                for (ValueReference v : vs) {
                    if (s.equalsIgnoreCase(v.stringValue())) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private Interface _defn;

    public SvcDicomSend() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The asset id of the dicom patient/study/series to be sent.", 0,
                Integer.MAX_VALUE));

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the dicom patient/study/series to be sent.",
                0, Integer.MAX_VALUE));

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
        ee.add(new Interface.Attribute("name",
                new EnumType(ElementName.stringValues()),
                "The name of the dicom element. Should not specified if 'tag' is specified.",
                0));
        ee.add(new Interface.Attribute("tag",
                new StringType(Pattern.compile("[0-9a-fA-F]{8}")),
                "The tag of the dicom element. Should not specified if 'name' is specified.",
                0));
        ee.add(new Interface.Attribute("anonymize", BooleanType.DEFAULT,
                "Anonymize the element. Defaults to false. If it is set to true, the element value will be ignored.",
                0));
        ee.add(new Interface.Element("value", StringType.DEFAULT,
                "The new value of the dicom element. It will be ignored if 'anonymize' is set to true. 'value' and 'value-reference' arguments should not be specified at the same time.",
                0, 1));
        ee.add(new Interface.Element("value-reference",
                new EnumType(ValueReference.stringValues()),
                "The new value refers to meta data of parent subject/study. It will be ignored if 'anonymize' is set to true. 'value' and 'value-reference' arguments should not be specified at the same time.",
                0, 1));
        override.add(ee);
        _defn.add(override);
        _defn.add(new Interface.Element("log", BooleanType.DEFAULT,
                "On/off logging. Defaults to true.", 0, 1));
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
        return null;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public boolean canBeAborted() {

        return true;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {

        /*
         * parse args
         */
        boolean log = args.booleanValue("log", true);
        Collection<String> ids = args.values("id");
        Collection<String> cids = args.values("cid");
        String where = args.value("where");
        if ((ids == null || ids.isEmpty()) && (cids == null || cids.isEmpty())
                && where == null) {
            Exception ex = new Exception(
                    "Argument 'id', 'cid' or 'where' is required.");
            if (log) {
                PluginLog.log(LOG_NAME).add(PluginLog.ERROR, ex.getMessage(),
                        ex);
            }
            throw ex;
        }
        String callingAETitle = args.value("calling-ae/title");
        String calledAETitle = args.value("called-ae/title");
        String calledAEHost = args.value("called-ae/host");
        int calledAEPort = args.intValue("called-ae/port");
        final Logger logger = log ? new Logger(callingAETitle, calledAETitle,
                calledAEHost, calledAEPort) : null;
        /*
         * elements to override
         */
        boolean hasValueRefs = args
                .elementExists("override/element/value-reference");
        Map<AttributeTag, Object> override = null;
        if (args.elementExists("override")) {
            override = parseOverriddenElements(args.element("override"));
        }

        /*
         * find dicom datasets
         */
        Set<String> datasetAssetIds = new TreeSet<String>();
        PluginTask.setCurrentThreadActivity("adding dicom datasets/series...");
        if (ids != null) {
            PluginTask.checkIfThreadTaskAborted();
            for (String id : ids) {
                ensureAssetExists(executor(), id, false, logger);
                if (logger != null) {
                    logger.logInfo("adding dicom dataset " + id);
                }
                addByAssetId(executor(), id, datasetAssetIds);
            }
        }
        if (cids != null) {
            PluginTask.checkIfThreadTaskAborted();
            for (String cid : cids) {
                ensureAssetExists(executor(), cid, true, logger);
                if (logger != null) {
                    logger.logInfo("adding dicom asset " + cid);
                }
                addByCiteableId(executor(), cid, datasetAssetIds);
            }
        }
        if (where != null) {
            PluginTask.checkIfThreadTaskAborted();
            addByQuery(executor(), where, datasetAssetIds);
        }
        if (datasetAssetIds.isEmpty()) {
            // No DICOM dataset/series found.
            if (logger != null) {
                logger.logInfo("no dicom dataset is found. Finish.");
            }
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
                if (logger != null) {
                    logger.logInfo(
                            "extracting dicom dataset " + datasetAssetId);
                }
                extractAssetContent(executor(), datasetAssetId, assetDir);
                if (override != null && !override.isEmpty()) {
                    PluginTask.setCurrentThreadActivity(
                            "Editting dicom files from asset " + datasetAssetId
                                    + "...");
                    PluginTask.checkIfThreadTaskAborted();
                    if (hasValueRefs) {
                        if (logger != null) {
                            logger.logInfo(
                                    "resolve value reference for dicom dataset "
                                            + datasetAssetId);
                        }
                        updateOverriddenElementsWithValueRefs(executor(),
                                datasetAssetId, override);
                    }
                    if (logger != null) {
                        logger.logInfo(
                                "editing dicom dataset " + datasetAssetId);
                    }
                    editDicomFiles(assetDir, override, callingAETitle);
                }
            }
            SetOfDicomFiles dicomFiles = listDicomFilesRecursively(dir);
            int total = dicomFiles.size();
            PluginTask.checkIfThreadTaskAborted();
            PluginTask.setCurrentThreadActivity("Sending dicom data...");
            final int[] result = new int[4];
            if (logger != null) {
                logger.logInfo("sending 0/" + total + " dicom files...");
            }
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
                            if (logger != null && nCompleted % 100 == 0) {
                                logger.logInfo("sending " + nCompleted + "/"
                                        + (nRemaining + nCompleted)
                                        + " dicom files...");
                            }
                        }
                    }, null, 0, 0);
            int nCompleted = result[1];
            int nFailed = result[2];
            w.add("sent",
                    new String[] { "completed", Integer.toString(nCompleted),
                            "failed", Integer.toString(nFailed), "total",
                            Integer.toString(total) },
                    nFailed <= 0);
            if (logger != null) {
                logger.logInfo(
                        "sent " + nCompleted + "/" + total + " dicom files.");
            }
            if (nCompleted < total || nFailed > 0) {
                if (logger != null) {
                    logger.logError("failed. " + nCompleted + " of " + total
                            + " dicom files were sent.");
                }
                throw new DicomException(
                        "Failed to send DICOM files to " + calledAETitle + "@"
                                + calledAEHost + ":" + calledAEPort);
            }
            PluginTask.threadTaskCompleted();
        } catch (Throwable t) {
            PluginLog.log(LOG_NAME).add(PluginLog.ERROR, t.getMessage(), t);
            throw t;
        } finally {
            forceDelete(dir);
        }
    }

    private static void ensureAssetExists(ServiceExecutor executor, String id,
            boolean cid, Logger logger) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (cid) {
            dm.add("cid", id);
        } else {
            dm.add("id", id);
        }
        boolean exists = executor.execute("asset.exists", dm.root())
                .booleanValue("exists", false);
        if (!exists) {
            StringBuilder sb = new StringBuilder("asset(");
            if (cid) {
                sb.append("cid=");
            } else {
                sb.append("id=");
            }
            sb.append(id);
            sb.append(") does not exist.");
            Exception ex = new IllegalArgumentException(sb.toString());
            if (logger != null) {
                logger.logError(ex.getMessage());
            }
            throw ex;
        }
    }

    private static void updateOverriddenElementsWithValueRefs(
            ServiceExecutor executor, String datasetAssetId,
            Map<AttributeTag, Object> override) throws Throwable {
        for (AttributeTag tag : override.keySet()) {
            Object v = override.get(tag);
            if (v instanceof ValueReference) {
                switch ((ValueReference) v) {
                case SUBJECT_CID:
                    override.put(tag, getSubjectCid(executor, datasetAssetId));
                    break;
                case STUDY_CID:
                    override.put(tag, getStudyCid(executor, datasetAssetId));
                    break;
                case PATIENT_NAME:
                    override.put(tag, getPatientName(executor, datasetAssetId));
                    break;
                case PATIENT_ID:
                    override.put(tag, getPatientId(executor, datasetAssetId));
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static String getSubjectCid(ServiceExecutor executor,
            String datasetAssetId) throws Throwable {
        String cid = executor.execute("asset.get",
                "<args><id>" + datasetAssetId + "</id></args>", null, null)
                .value("asset/cid");
        if (cid != null) {
            String subjectCid = CiteableIdUtil.getParentId(cid, 3);
            if (subjectCid != null) {
                return subjectCid;
            }
        }
        return null;
    }

    private static String getStudyCid(ServiceExecutor executor,
            String datasetAssetId) throws Throwable {
        String cid = executor.execute("asset.get",
                "<args><id>" + datasetAssetId + "</id></args>", null, null)
                .value("asset/cid");
        if (cid != null) {
            String studyCid = CiteableIdUtil.getParentId(cid);
            if (studyCid != null) {
                return studyCid;
            }
        }
        return null;
    }

    private static XmlDoc.Element getPatientMeta(ServiceExecutor executor,
            String datasetAssetId) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + datasetAssetId + "</id></args>", null, null)
                .element("asset");
        String cid = ae.value("cid");
        if (cid != null) {
            // has cid: pssd data
            String subjectCid = CiteableIdUtil.getParentId(cid, 3);
            if (subjectCid != null) {
                return executor
                        .execute("asset.get",
                                "<args><cid>" + subjectCid + "</cid></args>",
                                null, null)
                        .element("asset/meta/mf-dicom-patient");
            }
        } else {
            // has no cid: pss data
            String studyAssetId = ae.value("related[@type='container']/to");
            if (studyAssetId != null) {
                XmlDoc.Element studyAE = executor.execute("asset.get",
                        "<args><id>" + studyAssetId + "</id></args>", null,
                        null).element("asset");
                String patientAssetId = studyAE
                        .value("related[@type='had-by']/to");
                if (patientAssetId != null) {
                    return executor
                            .execute("asset.get",
                                    "<args><id>" + patientAssetId
                                            + "</id></args>",
                                    null, null)
                            .element("asset/meta/mf-dicom-patient");
                }
            }
        }
        return null;
    }

    private static String getPatientId(ServiceExecutor executor,
            String datasetAssetId) throws Throwable {
        XmlDoc.Element pe = getPatientMeta(executor, datasetAssetId);
        if (pe != null && pe.nameEquals("mf-dicom-patient")) {
            return pe.value("id");
        }
        return null;
    }

    private static String getPatientName(ServiceExecutor executor,
            String datasetAssetId) throws Throwable {
        XmlDoc.Element pe = getPatientMeta(executor, datasetAssetId);
        if (pe != null && pe.nameEquals("mf-dicom-patient")) {
            String fn = pe.value("name[@type='first']");
            String mn = pe.value("name[@type='middle']");
            String ln = pe.value("name[@type='last']");
            if (fn != null || mn != null || ln != null) {
                StringBuilder sb = new StringBuilder();
                if (ln != null) {
                    sb.append(ln);
                }
                if (fn != null || mn != null) {
                    sb.append("^");
                    if (fn != null) {
                        sb.append(fn);
                    }
                    if (mn != null) {
                        sb.append("^");
                        sb.append(mn);
                    }
                }
                return sb.toString();
            }
        }
        return null;
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

    private static Map<AttributeTag, Object> parseOverriddenElements(
            XmlDoc.Element oe) throws Throwable {
        List<XmlDoc.Element> ees = oe.elements("element");
        if (ees != null) {
            Map<AttributeTag, Object> map = new TreeMap<AttributeTag, Object>();
            for (XmlDoc.Element ee : ees) {
                ElementName name = ElementName.fromString(ee.value("@name"));
                String tagStr = ee.value("@tag");
                if (name == null && tagStr == null) {
                    throw new Exception(
                            "Either override/element/@tag or override/element/@name must be specified.");
                }
                if (name != null && tagStr != null) {
                    throw new Exception(
                            "Both override/element/@tag and override/element/@name are specified. Expects only one.");
                }
                if (name != null) {
                    tagStr = name.tag();
                }
                String group = tagStr.substring(0, 4);
                String element = tagStr.substring(4);
                AttributeTag tag = new AttributeTag(Integer.parseInt(group, 16),
                        Integer.parseInt(element, 16));
                if (ee.booleanValue("@anonymize", false)) {
                    map.put(tag, " ");
                } else {

                    String value = ee.value("value");
                    ValueReference valueRef = ValueReference
                            .fromString(ee.value("value-reference"));
                    if (value != null) {
                        map.put(tag, value);
                    } else if (valueRef != null) {
                        map.put(tag, valueRef);
                    } else {
                        throw new Exception("override/element[@tag='" + tagStr
                                + "']/value or override/element[@tag='" + tagStr
                                + "']/value-reference must be specified.");
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
            Map<AttributeTag, Object> overriddenValues, String fromAET)
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
                Object ov = overriddenValues.get(aTag);
                // NOTE: null values are ignored
                if (ov != null && (ov instanceof String)) {
                    String aValue = (String) ov;
                    Attribute attr = list.get(aTag);
                    if (attr != null) {
                        attr.setValue(aValue);
                    } else {
                        list.putNewAttribute(aTag).addValue(aValue);
                    }
                }
            }
        }
        list.write(new FileOutputStream(f),
                TransferSyntax.ExplicitVRLittleEndian, true, true);
    }

    private static void editDicomFiles(File dir,
            Map<AttributeTag, Object> overriddenValues, String fromAET)
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

}
