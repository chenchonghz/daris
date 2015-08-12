package nig.mf.plugin.pssd.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import nig.dicom.util.DicomFileCheck;
import nig.mf.dicom.plugin.util.DICOMPatient;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;

import org.apache.commons.io.FileUtils;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
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

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.network.StorageSOPClassSCU;

public class SvcDICOMSend extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.dicom.send";
    public static final String SERVICE_DESCRIPTION = "Send DICOM DataSets to a remote DICOM Application Entity(server).  Each DataSet is sent in a separate DICOM client call.";
    public static final int DEFAULT_DICOM_PORT = 104;

    public static enum ElementAction {
        unchanged("unchanged"), set("set"), anonymize("anonymize"), use_subject_cid(
                "use-subject-cid"), use_mf_dicom_patient_name(
                "use-mf-dicom-patient-name"), use_mf_dicom_patient_id(
                "use-mf-dicom-patient-id");
        private String _stringValue;

        ElementAction(String stringValue) {
            _stringValue = stringValue;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public final String toString() {
            return stringValue();
        }

        public static final String[] stringValues() {
            ElementAction[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].stringValue();
            }
            return svs;
        }

        public static final ElementAction fromString(String action,
                ElementAction defaultValue) {
            if (action != null) {
                ElementAction[] vs = values();
                for (ElementAction v : vs) {
                    if (v.stringValue().equalsIgnoreCase(action)) {
                        return v;
                    }
                }
            }
            return defaultValue;
        }
    }

    private static class ApplicationEntity {
        public final String title;
        public final String host;
        public final int port;

        ApplicationEntity(String host, int port, String title) {
            this.host = host;
            this.port = port;
            this.title = title;
        }
    }

    private static interface DicomFileEditor {
        void edit(File f) throws Throwable;
    }

    private Interface _defn;

    public SvcDICOMSend() throws Throwable {
        _defn = new Interface();
        /*
         * pid
         */
        Interface.Element me = new Interface.Element(
                "pid",
                CiteableIdType.DEFAULT,
                "The identity of the parent object; can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
                0, Integer.MAX_VALUE);
        _defn.add(me);
        /*
         * where
         */
        _defn.add(new Interface.Element(
                "where",
                StringType.DEFAULT,
                "A query to find the objects to send. All the DICOM datasets contained by the result objects will be sent.",
                0, 1));
        /*
         * asset-type
         */
        _defn.add(new Interface.Element("asset-type", new EnumType(
                DistributedQuery.ResultAssetType.stringValues()),
                "Specify type of asset to send. Defaults to all.", 0, 1));
        /*
         * local application entity
         */
        me = new Interface.Element("local", XmlDocType.DEFAULT,
                "Settings of the local application entity.", 1, 1);
        me.add(new Interface.Element("aet", StringType.DEFAULT,
                "Local/Calling AET(application entity title).", 1, 1));
        _defn.add(me);
        /*
         * remote application entity
         */
        me = new Interface.Element("remote", XmlDocType.DEFAULT,
                "Settings of the remote application entity.", 1, 1);
        me.add(new Interface.Attribute(
                "name",
                StringType.DEFAULT,
                new StringBuilder()
                        .append("A convenience name that the remote application entity may be referred to by. ")
                        .append("In this case, the application entity is looked up in the DICOM AE registry and the children elements host, port, aet are ignored. ")
                        .append("Don't specify the name if you want to specify these children elements directly.")
                        .toString(), 0));
        me.add(new Interface.Element("aet", StringType.DEFAULT,
                "Remote/Called AET(application entity title).", 0, 1));
        me.add(new Interface.Element("host", StringType.DEFAULT,
                "The host address of the remote/called application entity.", 0,
                1));
        me.add(new Interface.Element("port", IntegerType.DEFAULT,
                "The port number of the remote/called application entity.", 0,
                1));
        _defn.add(me);
        /*
         * overridden meta
         */
        me = new Interface.Element("override", XmlDocType.DEFAULT,
                "The DICOM elements to be overridden(modify/anonymize).", 0, 1);
        // generic element
        Interface.Element ee = new Interface.Element("element",
                XmlDocType.DEFAULT,
                "A generic DICOM element to be overridden.", 0,
                Integer.MAX_VALUE);
        ee.add(new Interface.Attribute("group", new StringType(Pattern
                .compile("[0-9a-fA-F]{4}")),
                "The group tag of the DICOM element.", 1));
        ee.add(new Interface.Attribute("element", new StringType(Pattern
                .compile("[0-9a-fA-F]{4}")),
                "The element tag of the DICOM element.", 1));
        ee.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        ee.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(ee);
        /*
         * pre-selected elements
         */
        // patient name
        Interface.Element pne = new Interface.Element(
                "patient-name",
                XmlDocType.DEFAULT,
                "The DICOM patient name element (0010,0010) will be overridden.",
                0, 1);
        pne.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        pne.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(pne);
        // patient id
        Interface.Element pie = new Interface.Element("patient-id",
                XmlDocType.DEFAULT,
                "The DICOM patient id element (0010,0020) will be overridden.",
                0, 1);
        pie.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        pie.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(pie);
        // study id
        Interface.Element sie = new Interface.Element("study-id",
                XmlDocType.DEFAULT,
                "The DICOM study id element (0020,0010) will be overridden.",
                0, 1);
        sie.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        sie.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(sie);
        // performing/attending physician's name
        Interface.Element ppne = new Interface.Element(
                "performing-physician-name",
                XmlDocType.DEFAULT,
                "The DICOM element (0008,1050), performing physician's name, will be overridden.",
                0, 1);
        ppne.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        ppne.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(ppne);
        // referring physician's name
        Interface.Element rpne = new Interface.Element(
                "referring-physician-name",
                XmlDocType.DEFAULT,
                "The DICOM element (0008,0090), referring physician's name, will be overridden.",
                0, 1);
        rpne.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        rpne.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(rpne);
        // referring physician's phone
        Interface.Element rppe = new Interface.Element(
                "referring-physician-phone",
                XmlDocType.DEFAULT,
                "The DICOM element (0008,0094), referring physician's telphone number, will be overridden.",
                0, 1);
        rppe.add(new Interface.Attribute(
                "action",
                new EnumType(ElementAction.stringValues()),
                "The action performed on the element before sending. Defaults to set.",
                0));
        rppe.add(new Interface.Element(
                "value",
                StringType.DEFAULT,
                "The value of the DICOM element. Only required if the action is 'set'.",
                0, 1));
        me.add(rppe);
        _defn.add(me);
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

    @Override
    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {
        // Parse and check
        if (!args.elementExists("pid") && !args.elementExists("where")) {
            throw new Exception(
                    "Expecting either pid or where element. Found none.");
        }
        if (args.elementExists("pid") && args.elementExists("where")) {
            throw new Exception(
                    "Expecting either pid or where element. Found both.");
        }
        String type = args.stringValue("asset-type", "all");
        Collection<String> pids = args.values("pid");
        String where = args.value("where");
        if (where != null) {
            XmlDocMaker dm = new XmlDocMaker("args");
            StringBuilder sb = new StringBuilder(where);
            DistributedQuery.appendResultAssetTypePredicate(sb,
                    DistributedQuery.ResultAssetType.instantiate(type));
            dm.add("where", sb.toString());
            dm.add("size", "infinity");
            dm.add("action", "get-cid");
            XmlDoc.Element r = executor().execute("asset.query", dm.root());
            pids = r.values("cid");
        }
        Boolean exceptionOnFail = args.booleanValue("exception-on-fail", true);
        // remote ae
        ApplicationEntity remoteAE = resolveRemoteAE(args.element("remote"),
                executor());
        String remoteAET = remoteAE.title;
        String remoteAEHost = remoteAE.host;
        int remoteAEPort = remoteAE.port;
        // local ae
        String localAET = args.value("local/aet");
        // override: validate
        if (args.elementExists("override")) {
            List<XmlDoc.Element> oes = args.element("override").elements();
            if (oes != null) {
                for (XmlDoc.Element oe : oes) {
                    ElementAction action = ElementAction.fromString(
                            oe.value("@action"), ElementAction.set);
                    String value = oe.value("value");
                    if (action == ElementAction.set && value == null) {
                        throw new IllegalArgumentException("override/"
                                + oe.name()
                                + "/value is missing because action is set.");
                    }
                }
            }
        }
        //
        int compressionLevel = 0;
        int debugLevel = 0;
        PluginTask.checkIfThreadTaskAborted();
        // Find the DataSets with DICOM content. Do a distributed query.
        // Iterate over all input pid
        ArrayList<String> dataSets = new ArrayList<String>();
        if (pids != null) {
            for (String pid : pids) {
                XmlDocMaker dm = new XmlDocMaker("args");
                StringBuilder query = new StringBuilder("(cid='" + pid
                        + "' or cid starts with '" + pid + "')");
                query.append(" and model='om.pssd.dataset' and type='dicom/series'");
                DistributedQuery.appendResultAssetTypePredicate(query,
                        DistributedQuery.ResultAssetType.instantiate(type));
                dm.add("where", query.toString());
                dm.add("size", "infinity");
                XmlDoc.Element r = executor().execute("asset.query", dm.root());
                Collection<String> ids = r.values("id");
                if (ids != null) {
                    dataSets.addAll(ids);
                }
            }
        }
        if (dataSets == null || dataSets.isEmpty()) {
            return;
        }
        /*
         * Now start extracting the dicom datasets
         */
        int nDataSets = dataSets.size();
        PluginTask.threadTaskBeginSetOf(nDataSets);
        File dir = PluginService.createTemporaryDirectory();
        try {
            for (String id : dataSets) {
                /*
                 * Get subject(patient) metadata and study cid.
                 */
                PluginTask.checkIfThreadTaskAborted();
                String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.idToCid(
                        executor(), id);
                String subjectCid = nig.mf.pssd.CiteableIdUtil
                        .getSubjectId(cid);
                DICOMPatient dicomPatient = null;
                XmlDoc.Element tm = AssetUtil.getAsset(executor(), subjectCid,
                        null);
                if (tm != null) {
                    XmlDoc.Element tm2 = tm
                            .element("asset/meta/mf-dicom-patient");
                    if (tm2 != null) {
                        dicomPatient = new DICOMPatient(tm2);
                    }
                }

                /*
                 * Extract dicom files
                 */
                String msg = "Extracting the content of asset " + id + "(cid="
                        + cid + ")...";
                PluginTask.setCurrentThreadActivity(msg);
                log(EventType.info, msg);
                // Create sub-directory to hold the extracted dicom files.
                File tempDir = new File(dir, id);
                tempDir.mkdirs();
                // Extract asset content
                boolean hasContent = extractAssetContent(executor(), id,
                        tempDir);
                if (!hasContent) {
                    log(EventType.warning, "No content found for asset "
                            + "(cid=" + cid + ")");
                    continue;
                }

                /*
                 * Edit dicom files
                 */
                PluginTask.checkIfThreadTaskAborted();
                if (args.elementExists("override")) {
                    msg = "Editing DICOM file headers for DICOM series asset "
                            + id + "(cid=" + cid + ")...";
                    PluginTask.setCurrentThreadActivity(msg);
                    log(EventType.info, msg);
                    editDicomFiles(tempDir, subjectCid, dicomPatient,
                            args.element("override"), localAET);
                }
                PluginTask.checkIfThreadTaskAborted();
            }

            /*
             * Now send the dicom files.
             */
            // list all dicom files into a set.
            SetOfDicomFiles dcmFiles = listDicomFilesRecursively(dir);
            int total = dcmFiles.size();
            PluginTask.checkIfThreadTaskAborted();
            PluginTask.setCurrentThreadActivity("Sending DICOM data sets...");
            log(EventType.info, "Sending DICOM data sets...");
            final int[] result = new int[4];
            try {
                new StorageSOPClassSCU(remoteAEHost, remoteAEPort, remoteAET,
                        localAET, dcmFiles, compressionLevel,
                        new MultipleInstanceTransferStatusHandler() {

                            @Override
                            public void updateStatus(int nRemaining,
                                    int nCompleted, int nFailed, int nWarning,
                                    String sopInstanceUID) {
                                PluginTask.threadTaskCompletedMultipleOf(
                                        nCompleted, nRemaining + nCompleted);
                                result[0] = nRemaining;
                                result[1] = nCompleted;
                                result[2] = nFailed;
                                result[3] = nWarning;
                            }
                        }, null, 0, debugLevel);
            } catch (Throwable e) {
                log(e);
                if (exceptionOnFail) {
                    e.printStackTrace(System.out);
                    throw e;
                }
            }
            int nCompleted = result[1];
            int nFailed = result[2];

            if (nFailed > 0) {
                log(EventType.warning, "DICOM send completed with " + nFailed
                        + " failed files.");
            } else {
                log(EventType.info, "DICOM send completed succsessfully. "
                        + total + " dicom files sent.");
            }
            w.add("sent",
                    new String[] { "completed", Integer.toString(nCompleted),
                            "failed", Integer.toString(nFailed), "total",
                            Integer.toString(total) }, nFailed <= 0);
            PluginTask.threadTaskCompleted();
        } catch (Throwable e) {
            log(e);
            throw e;
        } finally {
            // Clean up
            forceDelete(dir);
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
            log(EventType.error,
                    "Failed to delete "
                            + (file.isDirectory() ? "directory" : "file")
                            + ": " + file.getAbsolutePath()
                            + ". It will be deleted when jvm exits.");
            FileUtils.forceDeleteOnExit(file);
        }
    }

    private static boolean extractAssetContent(ServiceExecutor executor,
            String id, File dir) throws Throwable {
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + id + "</id></args>", null, outputs).element(
                "asset");
        PluginService.Output output = outputs.output(0);
        if (output == null) {
            return false;
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
            File of = new File(dir, cext == null ? id : (id + "." + cext));
            InputStream is = output.stream();
            try {
                StreamCopy.copy(is, of);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return true;
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

    private static ApplicationEntity resolveRemoteAE(XmlDoc.Element re,
            ServiceExecutor executor) throws Throwable {
        String name = re.value("@name");
        String host = re.value("host");
        int port = re.intValue("port", DEFAULT_DICOM_PORT);
        String aet = re.value("aet");
        if (host == null || aet == null) {
            if (name == null) {
                throw new Exception(
                        "You did not supply the name of remote application entity (remote/@name), so you must give the host, port and aet of the remote application entity (remote/host, remote/port, remote/aet).");
            } else {
                // Get the registry for remote DICOM application entities
                XmlDoc.Element raee = executor.execute(
                        SvcDICOMAERegList.SERVICE_NAME).element(
                        "ae[@name='" + name + "']");
                if (raee == null) {
                    throw new Exception(
                            "Failed to look up the remote application entity name '"
                                    + name
                                    + "'. Run service '"
                                    + SvcDICOMAERegList.SERVICE_NAME
                                    + "' to see all the available remote AE names.");
                }
                host = raee.value("host");
                port = raee.intValue("port");
                aet = raee.value("aet");
                if (host == null || aet == null) {
                    throw new Exception("Failed to retrieve AE:'" + name
                            + "' information from the AE registry.");
                }
            }
        }
        return new ApplicationEntity(host, port, aet);
    }

    private static void editDicomFile(File f, Map<AttributeTag, String> values,
            String localAET) throws Throwable {
        // System.out.print("Modifying " + f.getPath() + "... ");
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
                    TransferSyntax.ExplicitVRLittleEndian, localAET);
        } else {
            FileMetaInformation.addFileMetaInformation(list,
                    TransferSyntax.ExplicitVRLittleEndian, localAET);
        }
        // Put the new tag in place
        if (values != null && !values.isEmpty()) {
            for (AttributeTag aTag : values.keySet()) {
                String aValue = values.get(aTag);
                Attribute attr = list.get(aTag);
                if (attr != null) {
                    attr.setValue(values.get(aTag));
                } else {
                    list.putNewAttribute(aTag).addValue(aValue);
                }
            }
        }
        list.write(new FileOutputStream(f),
                TransferSyntax.ExplicitVRLittleEndian, true, true);
    }

    private static void editDicomElementValue(Map<AttributeTag, String> values,
            AttributeTag tag, ElementAction action, String value,
            String subjectCid, DICOMPatient patient) {
        switch (action) {
        case unchanged:
            return;
        case anonymize:
            value = " ";
            break;
        case use_subject_cid:
            value = subjectCid;
            break;
        case use_mf_dicom_patient_name:
            value = patient.nameForDICOMFile();
            break;
        case use_mf_dicom_patient_id:
            value = patient.getID();
            break;
        default:
            break;
        }
        values.put(tag, value);
    }

    private static SetOfDicomFiles listDicomFiles(File dir,
            final DicomFileEditor editor) throws Throwable {
        SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
        File[] files = dir.listFiles();
        for (File f : files) {
            if (DicomFileCheck.isDicomFile(f)) {
                if (editor != null) {
                    editor.edit(f);
                }
                dcmFiles.add(f);
            }
        }
        return dcmFiles;
    }

    private static SetOfDicomFiles editDicomFiles(File dir, String subjectCid,
            DICOMPatient patient, XmlDoc.Element override, final String localAET)
            throws Throwable {
        final Map<AttributeTag, String> values = new LinkedHashMap<AttributeTag, String>();
        /*
         * generic elements
         */
        List<XmlDoc.Element> genericElements = override.elements("element");
        if (genericElements != null) {
            for (XmlDoc.Element ge : genericElements) {
                AttributeTag tag = new AttributeTag(
                        ge.intValue("@group", 0, 16), ge.intValue("@element",
                                0, 16));
                String value = ge.value("value");
                ElementAction action = ElementAction.fromString(
                        ge.value("@action"), ElementAction.set);
                editDicomElementValue(values, tag, action, value, subjectCid,
                        patient);
            }
        }
        /*
         * pre-selected elements
         */
        if (override.elementExists("patient-name")) {
            XmlDoc.Element pne = override.element("patient-name");
            ElementAction action = ElementAction.fromString(
                    pne.value("@action"), ElementAction.set);
            String value = pne.value("value");
            editDicomElementValue(values, TagFromName.PatientName, action,
                    value, subjectCid, patient);
        }
        if (override.elementExists("patient-id")) {
            XmlDoc.Element pie = override.element("patient-id");
            ElementAction action = ElementAction.fromString(
                    pie.value("@action"), ElementAction.set);
            String value = pie.value("value");
            editDicomElementValue(values, TagFromName.PatientID, action, value,
                    subjectCid, patient);
        }
        if (override.elementExists("study-id")) {
            XmlDoc.Element sie = override.element("study-id");
            ElementAction action = ElementAction.fromString(
                    sie.value("@action"), ElementAction.set);
            String value = sie.value("value");
            editDicomElementValue(values, TagFromName.StudyID, action, value,
                    subjectCid, patient);
        }
        if (override.elementExists("performing-physician-name")) {
            XmlDoc.Element ppne = override.element("performing-physician-name");
            ElementAction action = ElementAction.fromString(
                    ppne.value("@action"), ElementAction.set);
            String value = ppne.value("value");
            editDicomElementValue(values, TagFromName.PerformingPhysicianName,
                    action, value, subjectCid, patient);
        }
        if (override.elementExists("referring-physician-name")) {
            XmlDoc.Element rpne = override.element("referring-physician-name");
            ElementAction action = ElementAction.fromString(
                    rpne.value("@action"), ElementAction.set);
            String value = rpne.value("value");
            editDicomElementValue(values, TagFromName.ReferringPhysicianName,
                    action, value, subjectCid, patient);
        }
        if (override.elementExists("referring-physician-phone")) {
            XmlDoc.Element rppe = override.element("referring-physician-phone");
            ElementAction action = ElementAction.fromString(
                    rppe.value("@action"), ElementAction.set);
            String value = rppe.value("value");
            editDicomElementValue(values,
                    TagFromName.ReferringPhysicianTelephoneNumbers, action,
                    value, subjectCid, patient);
        }
        SetOfDicomFiles dcmFiles = listDicomFiles(dir, values.isEmpty() ? null
                : new DicomFileEditor() {
                    @Override
                    public void edit(File f) throws Throwable {
                        editDicomFile(f, values, localAET);
                    }
                });
        return dcmFiles;
    }

    private static enum EventType {
        info, error, warning, debug
    }

    private static void log(EventType event, String message) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", "dicom-send");
        dm.add("event", event.name().toLowerCase());
        dm.add("msg", message);
        PluginThread.serviceExecutor().execute("server.log", dm.root());
    }

    private static void log(Throwable e) throws Throwable {
        PrintWriter w = new PrintWriter(new StringWriter());
        try {
            e.printStackTrace(w);
            String msg = w.toString();
            log(EventType.error, msg);
        } finally {
            w.close();
        }
    }

}
