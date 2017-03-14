package daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

public class SvcCollectionArchiveCreate extends PluginService {

    public static final String SERVICE_NAME = "daris.collection.archive.create";

    public static final long GiB = 1073741824L;

    public static final int DEFAULT_COMPRESSION_LEVEL = 0;

    public static enum ArchiveFormat {
        AAR("application/arc-archive", "aar", Long.MAX_VALUE), ZIP("application/zip", "zip",
                Long.MAX_VALUE), TGZ("application/x-gzip", "tgz", 8 * GiB - 1);
        private String _mimeType;
        private String _ext;
        private long _maxSize;

        ArchiveFormat(String mimeType, String ext, long maxSize) {
            _mimeType = mimeType;
            _ext = ext;
            _maxSize = maxSize;
        }

        public static ArchiveFormat fromString(String s, ArchiveFormat defaultValue) {
            if (s != null) {
                ArchiveFormat[] vs = values();
                for (ArchiveFormat v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return defaultValue;
        }

        public String mimeType() {
            return _mimeType;
        }

        public long maxSize() {
            return _maxSize;
        }

        public String fileExtension() {
            return _ext;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static enum Parts {
        meta, content, all;
        public static Parts fromString(String s, Parts defaultValue) {
            if (s != null) {
                if (s.equalsIgnoreCase(meta.name())) {
                    return meta;
                } else if (s.equalsIgnoreCase(content.name())) {
                    return content;
                } else if (s.equalsIgnoreCase(all.name())) {
                    return all;
                }
            }
            return defaultValue;
        }
    }

    private Interface _defn;

    public SvcCollectionArchiveCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "the citeable id of the root/parent object.", 1,
                1));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "the query to filter/find the objects to be included in the archive.", 0, 1));
        _defn.add(new Interface.Element("format", new EnumType(ArchiveFormat.values()),
                "the archive format. Defaults to aar.", 0, 1));
        _defn.add(new Interface.Element("clevel", new IntegerType(0, 9),
                "the compression level (0 ~ 9). Set to 0 for fastest/no compression; 9 for slowest/best compression. Defaults to "
                        + DEFAULT_COMPRESSION_LEVEL + ".",
                0, 1));
        _defn.add(new Interface.Element("parts", new EnumType(Parts.values()),
                "Specifies which parts of the assets to archive. Defaults to 'all'.", 0, 1));
        _defn.add(new Interface.Element("decompress", BooleanType.DEFAULT,
                "Specifies whether or not decompress the content before adding to the archive. Defaults to true.", 0,
                1));
        Interface.Element transcode = new Interface.Element("transcode", XmlDocType.DEFAULT,
                "Transcodes to apply to the matching objects.", 0, Integer.MAX_VALUE);
        transcode.add(
                new Interface.Element("from", StringType.DEFAULT, "The mime type of the input asset/object.", 1, 1));
        transcode.add(
                new Interface.Element("to", StringType.DEFAULT, "The mime type of the output asset/object.", 1, 1));
        _defn.add(transcode);
        _defn.add(new Interface.Element("include-attachments", BooleanType.DEFAULT,
                "Include attachment assets. Defaults to true.", 0, 1));

        Interface.Element layoutPattern = new Interface.Element("layout-pattern", StringType.DEFAULT,
                "The pattern that defines the directory structure of the output archive. For each object type, only one pattern can be specified.",
                0, 5);
        layoutPattern.add(new Interface.Attribute("type",
                new EnumType(new String[] { "project", "subject", "ex-method", "study", "dataset" }),
                "The type of the objects the pattern applies to. If not specified, applies to all objects. For each object type, only one pattern can be specified.",
                0));
        _defn.add(layoutPattern);
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
        return "Creates an archive for the specific collection.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        String where = args.value("where");
        final boolean includeAttachments = args.booleanValue("include-attachments", true);
        final ArchiveFormat format = ArchiveFormat.fromString(args.value("format"), ArchiveFormat.ZIP);
        final int clevel = args.intValue("clevel", DEFAULT_COMPRESSION_LEVEL);
        final Parts parts = Parts.fromString(args.value("parts"), Parts.all);
        final boolean decompress = args.booleanValue("decompress", true);
        final SortedMap<String, String> transcodes = parseTranscodes(args);
        final Map<String, String> layoutPatterns = parseLayoutPatterns(args);
        StringBuilder sb = new StringBuilder();
        if (parts == Parts.content) {
            sb.append("((cid='" + cid + "' or cid starts with '" + cid + "') and asset has content)");
        } else {
            sb.append("(cid='" + cid + "' or cid starts with '" + cid + "')");
        }
        if (where != null) {
            sb.append(" and (");
            sb.append(where);
            sb.append(")");
        }
        String query = sb.toString();
        /*
         * search for objects.
         */
        PluginTask.setCurrentThreadActivity("Looking for objects");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", query);
        dm.add("size", "infinity");
        dm.add("action", "get-cid");
        final List<XmlDoc.Element> cides = executor().execute("asset.query", dm.root()).elements("cid");
        if (cides == null || cides.isEmpty()) {
            throw new Exception("No object found.");
        }

        PluginTask.clearCurrentThreadActivity();
        PluginTask.checkIfThreadTaskAborted();

        /*
         * check if the total content size is greater than the maximum size that
         * the specified format can support.
         */
        PluginTask.setCurrentThreadActivity("Checking the total content size");
        long totalContentSize = getTotalContentSize(executor(), query);
        if (totalContentSize > format.maxSize()) {
            throw new Exception("The total content size " + totalContentSize + " is greater than the maximum size that "
                    + format.name() + " format archive can handle.");
        }
        PluginTask.clearCurrentThreadActivity();
        PluginTask.checkIfThreadTaskAborted();

        /*
         * initialize service output
         */
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
            @Override
            public void run() {
                try {
                    int totalObjects = cides.size();
                    String mimeType = format == ArchiveFormat.TGZ ? "application/x-tar" : format.mimeType();
                    OutputStream os = format == ArchiveFormat.TGZ ? new GZIPOutputStream(pos) : pos;
                    try {
                        PluginTask.threadTaskBeginSetOf(totalObjects);
                        ArchiveOutput ao = ArchiveRegistry.createOutput(os, mimeType, clevel, null);
                        try {
                            for (XmlDoc.Element cide : cides) {
                                PluginTask.checkIfThreadTaskAborted();
                                PluginTask.setCurrentThreadActivity("Processing object " + cide.value());
                                addToArchive(executor(), cide, parts, decompress, transcodes, includeAttachments, ao,
                                        layoutPatterns);
                                PluginTask.clearCurrentThreadActivity();
                                PluginTask.threadTaskCompletedOneOf(totalObjects);
                            }
                        } finally {
                            ao.close();
                        }
                        PluginTask.threadTaskCompleted();
                    } finally {
                        if (os != pos) {
                            os.close();
                        }
                        pos.close();
                        // NOTE: No need to close pis (If do that, it will cause
                        // problem: corrupt archive).
                    }
                } catch (Throwable e) {
                    e.printStackTrace(System.out);
                }
            }

        });
        outputs.output(0).setData(pis, -1, format.mimeType());
    }

    private static SortedMap<String, String> parseTranscodes(Element args) throws Throwable {
        if (args == null || !args.elementExists("transcode")) {
            return null;
        }
        List<XmlDoc.Element> tes = args.elements("transcode");
        SortedMap<String, String> map = new TreeMap<String, String>();
        for (XmlDoc.Element te : tes) {
            String from = te.value("from");
            String to = te.value("to");
            if (map.containsKey(from)) {
                throw new IllegalArgumentException("Multiple inconsistent transcodes for input type: " + from);
            }
            map.put(from, to);
        }
        return map;
    }

    private static Map<String, String> parseLayoutPatterns(Element args) throws Throwable {
        if (args == null || !args.elementExists("layout-pattern")) {
            return null;
        }

        List<XmlDoc.Element> pes = args.elements("layout-pattern");
        Map<String, String> layoutPatterns = new LinkedHashMap<String, String>();
        for (XmlDoc.Element pe : pes) {
            String type = pe.value("@type");
            String pattern = pe.value();
            if (layoutPatterns.containsKey(type)) {
                throw new IllegalArgumentException(type == null ? "Multiple layout-patterns specified."
                        : "Multiple layout-patterns specified for object type: " + type);
            }
            if (type == null) {
                String[] ts = { "project", "subject", "ex-method", "study", "dataset" };
                for (String t : ts) {
                    if (layoutPatterns.containsKey(t)) {
                        throw new IllegalArgumentException("Multiple layout-patterns specified for object type: " + t);
                    }
                    layoutPatterns.put(t, pattern);
                }
            } else {
                layoutPatterns.put(type, pattern);
            }
        }
        if (!layoutPatterns.isEmpty()) {
            return layoutPatterns;
        } else {
            return null;
        }
    }

    private static void addToArchive(ServiceExecutor executor, Element cide, Parts parts, boolean decompress,
            SortedMap<String, String> transcodes, boolean includeAttachments, ArchiveOutput ao,
            Map<String, String> layoutPatterns) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get", "<args><id>" + cide.value("@id") + "</id></args>", null, null)
                .element("asset");
        String cid = ae.value("cid");
        String type = ae.value("type");
        String objectType = ae.value("meta/daris:pssd-object/type");
        String transcodeToType = (type != null && transcodes != null && transcodes.containsKey(type))
                ? transcodes.get(type) : null;
        String layoutPattern = layoutPatterns == null ? null : layoutPatterns.get(objectType);
        String dirPath = directoryPathFor(executor, ae, decompress, transcodeToType, layoutPattern);
        if (includeAttachments) {
            Collection<String> attachments = ae.values("related[@type='attachment']/to");
            if (attachments != null && !attachments.isEmpty()) {
                for (String attachment : attachments) {
                    PluginTask.setCurrentThreadActivity("Adding attachment " + attachment + " of object " + cid);
                    addAttachmentToArchive(executor, ae, attachment, ao, dirPath);
                }
            }
        }
        if (parts != Parts.content) {
            PluginTask.setCurrentThreadActivity("Adding metadata of object " + cid);
            addMetaToArchive(executor, ae, ao, dirPath);
        }
        if (parts == Parts.meta || !ae.elementExists("content")) {
            return;
        }

        if (transcodeToType != null) {
            transcodeContentToArchive(executor, ae, transcodeToType, decompress, ao, dirPath);
        } else {
            String ctype = ae.value("content/type");
            if (ArchiveRegistry.isAnArchive(ctype) && decompress) {
                PluginTask.setCurrentThreadActivity("Extracting content archive of object " + cid);
                extractContentToArchive(executor, ae, ao, dirPath);
            } else {
                PluginTask.setCurrentThreadActivity("Adding content of object " + cid);
                addContentToArchive(executor, ae, ao, dirPath);
            }
        }
    }

    private static void addAttachmentToArchive(ServiceExecutor executor, XmlDoc.Element ae, String attachmentAssetId,
            ArchiveOutput ao, String dirPath) throws Throwable {
        String cid = ae.value("cid");
        StringBuilder path = new StringBuilder(dirPath);
        path.append("/");
        path.append(cid);
        path.append(".attachments");
        XmlDoc.Element aae = executor
                .execute("asset.get", "<args><id>" + attachmentAssetId + "</id></args>", null, null).element("asset");
        if (!aae.elementExists("content")) {
            return;
        }
        String fileName = aae.value("name");
        if (fileName == null) {
            fileName = aae.value("@id");
            String ext = aae.value("content/type/@ext");
            if (ext != null) {
                fileName = fileName + "." + ext;
            }
        }
        /*
         * the archive entry name
         */
        String entryName = path.toString() + "/" + fileName;
        /*
         * 
         */
        String assetId = aae.value("@id");
        String ctype = aae.value("content/type");
        long csize = aae.longValue("content/size");
        addAssetContentToArchive(executor, assetId, ctype, csize, entryName, ao);
    }

    private static void transcodeContentToArchive(ServiceExecutor executor, XmlDoc.Element ae, String toType,
            boolean decompress, ArchiveOutput ao, String dirPath) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", ae.value("@id"));
        dm.add("atype", "aar");
        dm.push("transcode");
        dm.add("from", ae.value("type"));
        dm.add("to", toType);
        dm.pop();
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.transcode", dm.root(), null, outputs);
        PluginService.Output output = outputs.output(0);
        if (decompress) {
            extractServiceOutputToArchive(output, dirPath, ao);
        } else {
            /*
             * add to the archive
             */
            StringBuilder entryName = new StringBuilder(dirPath);
            entryName.append("/");
            String cid = ae.value("cid");
            String assetId = ae.value("@id");
            entryName.append(cid != null ? cid : assetId);
            entryName.append("-");
            entryName.append(toType.replace('/', '_'));
            String ext = getTypeExtension(executor, output.mimeType());
            if (ext != null) {
                entryName.append(".");
                entryName.append(ext);
            }
            try {
                ao.add(output.mimeType(), entryName.toString(), output.stream(), output.length());
            } finally {
                output.stream().close();
                output.close();
            }
        }
    }

    private static void addContentToArchive(ServiceExecutor executor, Element ae, ArchiveOutput ao, String dirPath)
            throws Throwable {
        String ctype = ae.value("content/type");
        String ext = ae.value("content/type/@ext");
        long csize = ae.longValue("content/size");
        /*
         * generate parent directory path
         */
        String fileName = ae.value("meta/daris:pssd-filename/original");
        /*
         * generate file name
         */
        if (fileName == null) {
            fileName = ae.value("cid");
            if (ext != null) {
                fileName = fileName + "." + ext;
            }
        }
        String assetId = ae.value("@id");
        /*
         * the archive entry name
         */
        String entryName = dirPath + "/" + fileName;
        /*
         * 
         */
        addAssetContentToArchive(executor, assetId, ctype, csize, entryName, ao);
    }

    private static void addAssetContentToArchive(ServiceExecutor executor, String assetId, String ctype, long csize,
            String entryName, ArchiveOutput ao) throws Throwable {
        /*
         * retrieve the content (InputStream)
         */
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.content.get", "<args><id>" + assetId + "</id></args>", null, outputs);
        PluginService.Output output = outputs.output(0);
        /*
         * add to the archive
         */
        try {
            ao.add(ctype, entryName, output.stream(), csize);
        } finally {
            output.stream().close();
            output.close();
        }
    }

    private static String getTypeExtension(ServiceExecutor executor, String type) throws Throwable {
        return executor.execute("type.describe", "<args><type>" + type + "</type></args>", null, null)
                .value("type/extension");
    }

    private static String directoryPathFor(ServiceExecutor executor, XmlDoc.Element ae, boolean decompress,
            String transcodeToType, String layoutPattern) throws Throwable {
        if (layoutPattern == null) {
            String cid = ae.value("cid");
            StringBuilder sb = new StringBuilder();
            sb.append(CiteableIdUtil.getProjectId(cid));
            if (CiteableIdUtil.isProjectId(cid)) {
                return sb.toString();
            }
            sb.append("/");
            sb.append(CiteableIdUtil.getSubjectId(cid));
            if (CiteableIdUtil.isSubjectId(cid) || CiteableIdUtil.isExMethodId(cid)) {
                return sb.toString();
            }
            sb.append("/");
            sb.append(CiteableIdUtil.getStudyId(cid));
            if (CiteableIdUtil.isStudyId(cid)) {
                return sb.toString();
            }
            sb.append("/");
            sb.append(cid);
            String type = ae.value("type");
            String ctype = ae.value("content/type");
            if (ArchiveRegistry.isAnArchive(ctype) && decompress && transcodeToType == null) {
                sb.append("/").append(type.replace('/', '_'));
            }
            if (transcodeToType != null) {
                sb.append("/").append(transcodeToType.replace('/', '_'));
            }
            return sb.toString();
        } else {
            return SvcAssetPathGenerate.generatePath(executor, ae, layoutPattern);
        }
    }

    private static void extractContentToArchive(ServiceExecutor executor, Element ae, ArchiveOutput ao, String dirPath)
            throws Throwable {
        /*
         * retrieve the content (InputStream)
         */
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.content.get", "<args><id>" + ae.value("@id") + "</id></args>", null, outputs);
        PluginService.Output output = outputs.output(0);
        /*
         * extract the content archive
         */
        extractServiceOutputToArchive(output, dirPath, ao);
    }

    private static void extractServiceOutputToArchive(PluginService.Output output, String dirPath, ArchiveOutput ao)
            throws Throwable {
        try {
            ArchiveInput ai = null;
            try {
                ai = ArchiveRegistry.createInput(new SizedInputStream(output.stream(), output.length()),
                        new NamedMimeType(output.mimeType()));
                ArchiveInput.Entry entry = null;
                while ((entry = ai.next()) != null) {
                    try {
                        if (!entry.isDirectory()) {
                            ao.add(entry.mimeType(), dirPath + "/" + entry.name(), entry.stream());
                        }
                    } finally {
                        ai.closeEntry();
                        // DEBUG
                        if (entry.stream() != null) {
                            try {
                                entry.stream().close();
                            } catch (Throwable e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                }
            } finally {
                if (ai != null) {
                    ai.close();
                }
            }
        } finally {
            output.stream().close();
            output.close();
        }
    }

    private static void addMetaToArchive(ServiceExecutor executor, Element ae, ArchiveOutput ao, String dirPath)
            throws Throwable {
        String cid = ae.value("cid");
        String entryName = dirPath + "/" + cid + ".meta.xml";
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", cid);
        XmlDoc.Element oe = executor.execute("om.pssd.object.describe", dm.root()).element("object");
        byte[] bytes = oe.toString().getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            ao.add("text/xml", entryName, is, bytes.length);
        } finally {
            is.close();
        }
    }

    private static long getTotalContentSize(ServiceExecutor executor, String where) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        dm.add("size", "infinity");
        dm.add("action", "sum");
        dm.add("xpath", "content/size");
        return executor.execute("asset.query", dm.root()).longValue("value");
    }

    @Override
    public boolean canBeAborted() {
        return true;
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
    public String name() {
        return SERVICE_NAME;
    }

}
