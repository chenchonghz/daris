package nig.mf.plugin.pssd.services;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

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
import arc.mf.plugin.dtype.StringType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

public class SvcCollectionArchiveCreate extends PluginService {

    public static final String SERVICE_NAME = "daris.collection.archive.create";

    public static final long GIGABYTE = 1073741824L;

    public static enum ArchiveFormat {
        aar, zip, tar;
        public static ArchiveFormat fromString(String s,
                ArchiveFormat defaultValue) {
            if (s != null) {
                if (s.equalsIgnoreCase(aar.name())) {
                    return aar;
                } else if (s.equalsIgnoreCase(zip.name())) {
                    return zip;
                } else if (s.equalsIgnoreCase(tar.name())) {
                    return tar;
                }
            }
            return defaultValue;
        }

        public String mimeType() {
            if (this == aar) {
                return "application/arc-archive";
            } else if (this == zip) {
                return "application/zip";
            } else {
                return "application/x-tar";
            }
        }

        public long maxSize() {
            if (this == zip) {
                return GIGABYTE * 4 - 1;
            } else if (this == tar) {
                return GIGABYTE * 8 - 1;
            } else {
                return Long.MAX_VALUE;
            }
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
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "the citeable id of the root/parent object.", 1, 1));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "the query to filter/find the objects to be included in the archive.",
                0, 1));
        _defn.add(new Interface.Element("format",
                new EnumType(ArchiveFormat.values()),
                "the archive format. Defaults to aar.", 0, 1));
        _defn.add(new Interface.Element("parts", new EnumType(Parts.values()),
                "Specifies which parts of the assets to archive. Defaults to 'all'.",
                0, 1));
        _defn.add(new Interface.Element("decompress", BooleanType.DEFAULT,
                "Specifies whether or not decompress the content before adding to the archive. Defaults to true.",
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
        return "Creates an archive for the specific collection.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        String where = args.value("where");
        final ArchiveFormat format = ArchiveFormat
                .fromString(args.value("format"), ArchiveFormat.zip);
        final Parts parts = Parts.fromString(args.value("parts"), Parts.all);
        final boolean decompress = args.booleanValue("decompress", true);
        StringBuilder sb = new StringBuilder();
        if (parts == Parts.content) {
            sb.append("((cid='" + cid + "' or cid starts with '" + cid
                    + "') and asset has content)");
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
        final List<XmlDoc.Element> cides = executor()
                .execute("asset.query", dm.root()).elements("cid");
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
            throw new Exception("The total content size " + totalContentSize
                    + " is greater than the maximum size that " + format.name()
                    + " format archive can handle.");
        }
        PluginTask.clearCurrentThreadActivity();
        PluginTask.checkIfThreadTaskAborted();

        /*
         * initialize service output
         */
        PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
            @Override
            public void run() {
                try {
                    int totalObjects = cides.size();
                    try {
                        PluginTask.threadTaskBeginSetOf(totalObjects);
                        ArchiveOutput ao = ArchiveRegistry.createOutput(pos,
                                format.mimeType(), 6, null);
                        try {
                            for (XmlDoc.Element cide : cides) {
                                PluginTask.checkIfThreadTaskAborted();
                                PluginTask.setCurrentThreadActivity(
                                        "Processing object " + cide.value());
                                addToArchive(executor(), cide, parts,
                                        decompress, ao);
                                PluginTask.clearCurrentThreadActivity();
                                PluginTask
                                        .threadTaskCompletedOneOf(totalObjects);
                            }
                        } finally {
                            ao.close();
                        }
                        PluginTask.threadTaskCompleted();
                    } finally {
                        pos.close();
                    }
                } catch (Throwable e) {
                    e.printStackTrace(System.out);
                }
            }

        });
        outputs.output(0).setData(pis, -1, format.mimeType());
    }

    private static void addToArchive(ServiceExecutor executor, Element cide,
            Parts parts, boolean decompress, ArchiveOutput ao)
                    throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + cide.value("@id") + "</id></args>", null, null)
                .element("asset");
        String cid = ae.value("cid");
        if (parts != Parts.content) {
            PluginTask.setCurrentThreadActivity(
                    "Adding metadata of object " + cid);
            addMetaToArchive(executor, ae, ao);
        }
        if (parts == Parts.meta || !ae.elementExists("content")) {
            return;
        }
        String ctype = ae.value("content/type");
        if (ArchiveRegistry.isAnArchive(ctype) && decompress) {
            PluginTask.setCurrentThreadActivity(
                    "Extracting content archive of object " + cid);
            extractContentToArchive(executor, ae, ao);
        } else {
            PluginTask.setCurrentThreadActivity(
                    "Adding content of object " + cid);
            addContentToArchive(executor, ae, ao);
        }
    }

    private static void addContentToArchive(ServiceExecutor executor,
            Element ae, ArchiveOutput ao) throws Throwable {
        String ctype = ae.value("content/type");
        String ext = ae.value("content/type/@ext");
        long csize = ae.longValue("content/size");
        /*
         * generate parent directory path
         */
        String dirPath = directoryPathFor(ae, false);
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
        /*
         * the archive entry name
         */
        String entryName = dirPath + "/" + fileName;
        /*
         * retrieve the content (InputStream)
         */
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.content.get",
                "<args><id>" + ae.value("@id") + "</id></args>", null, outputs);
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

    private static String directoryPathFor(XmlDoc.Element ae,
            boolean decompress) throws Throwable {
        String cid = ae.value("cid");
        StringBuilder sb = new StringBuilder();
        sb.append(CiteableIdUtil.getProjectId(cid));
        if (CiteableIdUtil.isProjectId(cid)) {
            return sb.toString();
        }
        sb.append("/");
        sb.append(CiteableIdUtil.getSubjectId(cid));
        if (CiteableIdUtil.isSubjectId(cid)
                || CiteableIdUtil.isExMethodId(cid)) {
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
        if (!decompress || ctype == null
                || !ArchiveRegistry.isAnArchive(ctype)) {
            return sb.toString();
        }
        sb.append("/");
        sb.append(type.replace('/', '_'));
        return sb.toString();
    }

    private static void extractContentToArchive(ServiceExecutor executor,
            Element ae, ArchiveOutput ao) throws Throwable {
        String dirPath = directoryPathFor(ae, true);
        String ctype = ae.value("content/type");
        long csize = ae.longValue("content/size");
        /*
         * retrieve the content (InputStream)
         */
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.content.get",
                "<args><id>" + ae.value("@id") + "</id></args>", null, outputs);
        PluginService.Output output = outputs.output(0);
        /*
         * extract the content archive
         */
        try {
            ArchiveInput ai = ArchiveRegistry.createInput(
                    new SizedInputStream(output.stream(), csize),
                    new NamedMimeType(ctype));
            try {
                ArchiveInput.Entry entry = null;
                while ((entry = ai.next()) != null) {
                    try {
                        if (!entry.isDirectory()) {
                            ao.add(entry.mimeType(),
                                    dirPath + "/" + entry.name(),
                                    entry.stream());
                        }
                    } finally {
                        ai.closeEntry();
                    }
                }
            } finally {
                ai.close();
            }
        } finally {
            output.stream().close();
            output.close();
        }
    }

    private static void addMetaToArchive(ServiceExecutor executor, Element ae,
            ArchiveOutput ao) throws Throwable {
        String cid = ae.value("cid");
        String entryName = directoryPathFor(ae, false) + "/" + cid
                + ".meta.xml";
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", cid);
        XmlDoc.Element oe = executor
                .execute("om.pssd.object.describe", dm.root())
                .element("object");
        byte[] bytes = oe.toString().getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            ao.add("text/xml", entryName, is, bytes.length);
        } finally {
            is.close();
        }
    }

    private static long getTotalContentSize(ServiceExecutor executor,
            String where) throws Throwable {

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
