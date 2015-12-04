package daris.client.model.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.ServerClient;
import arc.mf.client.archive.Archive;
import arc.mf.desktop.server.Session;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.NonCloseInputStream;
import arc.streams.ProgressMonitoredInputStream;
import arc.streams.StreamCopy;
import arc.utils.ProgressMonitor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.DownloadOptions.Parts;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class DownloadTask extends ObservableTask {

    public static final String TYPE_NAME = "daris.download";

    private DObjectRef _object;
    private DownloadOptions _options;
    private DownloadTaskProgress _progress;

    public DownloadTask(DObjectRef object, DownloadOptions options) {
        super(null, TYPE_NAME);
        _object = object;
        _options = options == null ? new DownloadOptions() : options;
        _progress = new DownloadTaskProgress();
    }

    public ObjectProperty<Double> progressProperty() {
        return _progress.progressProperty;
    }

    public IntegerProperty totalObjectsProperty() {
        return _progress.totalObjectsProperty;
    }

    public IntegerProperty processedObjectsProperty() {
        return _progress.processedObjectsProperty;
    }

    public StringProperty objectsProgressMessageProperty() {
        return _progress.objectsProgressMessageProperty;
    }

    public LongProperty totalSizeProperty() {
        return _progress.totalSizeProperty;
    }

    public LongProperty processedSizeProperty() {
        return _progress.processedSizeProperty;
    }

    public StringProperty sizeProgressMessageProperty() {
        return _progress.sizeProgressMessageProperty;
    }

    public LongProperty receivedSizeProperty() {
        return _progress.receivedSizeProperty;
    }

    public StringProperty messageProperty() {
        return _progress.messageProperty;
    }

    public StringProperty currentObjectProperty() {
        return _progress.currentObjectProperty;
    }

    public StringProperty currentOutputFileProperty() {
        return _progress.currentOutputFileProperty;
    }

    public void start() {
        DownloadTaskManager.get().addTask(this);
    }

    @Override
    public boolean discard() {
        DownloadTaskManager.get().removeTask(this);
        return super.discard();
    }

    @Override
    protected void doExecute() throws Throwable {
        ServerClient.Connection cxn = Session.connection();
        try {
            checkIfAborted();
            XmlDoc.Element ae = cxn
                    .execute("asset.get",
                            "<cid>" + _object.citeableId()
                                    + "</cid><lock>true</lock>",
                            null, null)
                    .element("asset");

            long totalSize = calcTotalSize(cxn, ae, _options);
            _progress.setTotalSize(totalSize);

            long processedSize = 0;
            _progress.setProcessedSize(processedSize);

            int totalObjects = calcTotalObjects(cxn, ae, _options);
            _progress.setTotalObjects(totalObjects);

            String cid = ae.value("cid");
            String assetId = ae.value("@id");
            try {
                if (cid == null || !_options.recursive()) {
                    checkIfAborted();
                    downloadObject(cxn, ae);
                    _progress.incProcessedObjects();
                } else {
                    int idx = 1;
                    int size = 100;
                    int remaining = Integer.MAX_VALUE;
                    XmlDoc.Element re = null;
                    while (remaining > 0) {
                        re = cxn.execute("asset.query",
                                "<where>cid='" + cid + "' or cid starts with '"
                                        + cid
                                        + "'</where><count>true</count><idx>"
                                        + idx + "</idx><size>" + size
                                        + "</size><action>get-meta</action>",
                                null, null);
                        remaining = re.intValue("cursor/remaining", 0);
                        List<XmlDoc.Element> caes = re.elements("asset");
                        if (caes != null) {
                            for (XmlDoc.Element cae : caes) {
                                checkIfAborted();
                                downloadObject(cxn, cae);
                                _progress.incProcessedObjects();
                            }
                        }
                        idx += size;
                    }
                }
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        _progress.setCompleted();
                    }
                }, 1000L);
            } finally {
                cxn.execute("asset.unlock", "<id>" + assetId + "</id>", null,
                        null);
            }
        } finally {
            cxn.close();
        }
    }

    private void downloadObject(ServerClient.Connection cxn, XmlDoc.Element ae)
            throws Throwable {
        if (_options.parts() != Parts.content) {
            downloadMeta(cxn, ae);
        }
        if (_options.parts() != Parts.meta) {
            String mimeType = ae.value("type");
            if (ae.elementExists("content")) {
                if (_options.hasTranscodeFor(mimeType)) {
                    transcodeContent(cxn, ae);
                } else {
                    downloadContent(cxn, ae);
                }
            }
        }
    }

    private static File createContentDir(String rootDir, XmlDoc.Element ae)
            throws Throwable {
        StringBuilder contentDirPath = new StringBuilder(
                directoryPathFor(rootDir, ae));
        String type = ae.value("type");
        if (type != null) {
            contentDirPath.append("/").append(type.replace('/', '_'));
        }
        File contentDir = new File(contentDirPath.toString());
        contentDir.mkdirs();
        return contentDir;
    }

    private static File createTranscodedDir(String rootDir, XmlDoc.Element ae,
            String type) throws Throwable {
        StringBuilder transcodedDirPath = new StringBuilder(
                directoryPathFor(rootDir, ae));
        if (type != null) {
            transcodedDirPath.append("/").append(type.replace('/', '_'));
        }
        File transcodedDir = new File(transcodedDirPath.toString());
        transcodedDir.mkdirs();
        return transcodedDir;
    }

    private static File createMetaFile(String rootDir, XmlDoc.Element ae)
            throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");

        File dir = new File(directoryPathFor(rootDir, ae));
        dir.mkdirs();

        String fileName = new StringBuilder(cid == null ? assetId : cid)
                .append(".meta.xml").toString();
        File file = new File(dir, fileName);
        return file;
    }

    private static File createContentFile(String rootDir, XmlDoc.Element ae)
            throws Throwable {

        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        String ext = ae.value("content/type/@ext");
        String fileName = ae.value("meta/daris:pssd-filename/original");
        if (fileName == null) {
            fileName = cid == null ? assetId : cid;
            if (ext != null) {
                fileName = fileName + "." + ext;
            }
        }

        File dir = new File(directoryPathFor(rootDir, ae));
        dir.mkdirs();

        File file = new File(dir, fileName);
        return file;
    }

    private static File createTranscodedFile(String rootDir, XmlDoc.Element ae,
            String type, String ext) throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        StringBuilder fileName = new StringBuilder(cid == null ? assetId : cid);
        if (type != null) {
            fileName.append("_").append(type.replace('/', '_'));
        }
        if (ext != null) {
            fileName.append(".").append(ext);
        }

        File dir = new File(directoryPathFor(rootDir, ae));
        dir.mkdirs();

        File file = new File(dir, fileName.toString());
        return file;
    }

    private static String directoryPathFor(String rootDir, XmlDoc.Element ae)
            throws Throwable {
        StringBuilder dirPath = new StringBuilder(rootDir);
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        if (cid == null) {
            return dirPath.append("/").append(assetId).toString();
        }
        String projectCID = CiteableIdUtils.getProjectCID(cid);
        dirPath.append("/");
        dirPath.append(projectCID);
        if (CiteableIdUtils.isProjectCID(cid)) {
            return dirPath.toString();
        }

        String subjectCID = CiteableIdUtils.getSubjectCID(cid);
        dirPath.append("/");
        dirPath.append(subjectCID);
        if (CiteableIdUtils.isSubjectCID(cid)
                || CiteableIdUtils.isExMethodCID(cid)) {
            return dirPath.toString();
        }

        String studyCID = CiteableIdUtils.getStudyCID(cid);
        dirPath.append("/");
        dirPath.append(studyCID);
        if (CiteableIdUtils.isStudyCID(cid)) {
            return dirPath.toString();
        }

        dirPath.append("/");
        dirPath.append(cid);
        return dirPath.toString();
    }

    private void downloadMeta(ServerClient.Connection cxn, XmlDoc.Element ae)
            throws Throwable {
        File file = createMetaFile(_options.directory(), ae);
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        XmlDoc.Element e;
        if (cid == null) {
            e = cxn.execute("asset.get", "<id>" + assetId + "</id>", null, null)
                    .element("asset");
        } else {
            e = cxn.execute("om.pssd.object.describe", "<id>" + cid + "</id>",
                    null, null).element("object");
        }
        writeXmlToFile(e, file);
    }

    private static void writeXmlToFile(XmlDoc.Element xe, File file)
            throws Throwable {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        try {
            os.write(xe.toString().getBytes("UTF-8"));
            os.flush();
        } finally {
            os.close();
        }
    }

    private void downloadContent(ServerClient.Connection cxn, XmlDoc.Element ae)
            throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        _progress.setMessage(
                "downloading asset " + (cid == null ? assetId : cid));
        final String ctype = ae.value("content/type");
        final ProgressMonitor pm = new ProgressMonitor() {
            @Override
            public boolean abort() {
                return false;
            }

            @Override
            public void begin(final int task, final long itemTotal) {
            }

            @Override
            public void beginMultiPart(final int task, final long total) {
            }

            @Override
            public void end(final int task) {
            }

            @Override
            public void endMultiPart(final int task) {
            }

            @Override
            public void update(final long itemProgress) {
                _progress.incProcessedSize(itemProgress);
                if (!ArchiveRegistry.isAnArchive(ctype)
                        || !_options.decompress()) {
                    _progress.incReceivedSize(itemProgress);
                }
            }
        };

        ServerClient.OutputConsumer output = new ServerClient.OutputConsumer() {
            @Override
            protected void consume(Element re, LongInputStream is)
                    throws Throwable {
                ProgressMonitoredInputStream pis = new ProgressMonitoredInputStream(
                        pm, is, true);
                Archive.declareSupportForAllTypes();
                if (ArchiveRegistry.isAnArchive(ctype)
                        && _options.decompress()) {
                    File outputDirectory = createContentDir(
                            _options.directory(), ae);
                    ArchiveInput ai = ArchiveRegistry.createInput(
                            new NonCloseInputStream(pis),
                            new NamedMimeType(ctype));
                    _progress
                            .setMessage("Extracting archive content from asset "
                                    + (cid == null ? assetId : cid));
                    ArchiveExtractor.extract(ai, outputDirectory, true,
                            _options.collisionPolicy() == DownloadCollisionPolicy.OVERWRITE,
                            false, new ArchiveExtractor.Terminator() {

                        @Override
                        public void checkIfTerminatedProcessed(
                                long bytesDecompressed) throws Throwable {
                            _progress.incReceivedSize(bytesDecompressed);
                        }

                        @Override
                        public void checkIfTerminatedAfterEntry()
                                throws Throwable {
                            // nbFielsDecompressed++;
                        }
                    });
                    ArchiveInput.discardToEndOfStream(pis);
                } else {
                    File outputFile = createContentFile(_options.directory(),
                            ae);
                    StreamCopy.copy(pis, outputFile);
                }
            }
        };
        cxn.execute("asset.content.get", "<id>" + assetId + "</id>", null,
                output);
        _progress.setMessage(
                "downloaded asset " + (cid == null ? assetId : cid));
    }

    private void transcodeContent(ServerClient.Connection cxn,
            XmlDoc.Element ae) throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        _progress.setMessage(
                "transcoding asset " + (cid == null ? assetId : cid));
        String type = ae.value("type");
        String toType = _options.transcodeFor(type).toMimeType;
        String atype = _options.decompress() ? "aar" : "zip";
        XmlStringWriter w = new XmlStringWriter();
        w.add("id", assetId);
        w.add("atype", atype);
        w.push("transcode");
        w.add("from", type);
        w.add("to", toType);
        w.pop();

        final ProgressMonitor pm = new ProgressMonitor() {
            @Override
            public boolean abort() {
                return false;
            }

            @Override
            public void begin(final int task, final long itemTotal) {
            }

            @Override
            public void beginMultiPart(final int task, final long total) {
            }

            @Override
            public void end(final int task) {
            }

            @Override
            public void endMultiPart(final int task) {
            }

            @Override
            public void update(final long itemProgress) {
                if (!_options.decompress()) {
                    _progress.incReceivedSize(itemProgress);
                }
            }
        };

        ServerClient.OutputConsumer output = new ServerClient.OutputConsumer() {
            @Override
            protected void consume(Element re, LongInputStream is)
                    throws Throwable {
                ProgressMonitoredInputStream pis = new ProgressMonitoredInputStream(
                        pm, is, true);
                if (_options.decompress()) {
                    File outputDirectory = createTranscodedDir(
                            _options.directory(), ae, toType);
                    ArchiveInput ai = ArchiveRegistry.createInput(
                            new NonCloseInputStream(pis),
                            new NamedMimeType("application/arc-archive"));
                    _progress.setMessage(
                            "Extracting transcoded content from asset "
                                    + (cid == null ? assetId : cid));
                    ArchiveExtractor.extract(ai, outputDirectory, true,
                            _options.collisionPolicy() == DownloadCollisionPolicy.OVERWRITE,
                            false, new ArchiveExtractor.Terminator() {

                        @Override
                        public void checkIfTerminatedProcessed(
                                long bytesDecompressed) throws Throwable {
                            _progress.incReceivedSize(bytesDecompressed);
                        }

                        @Override
                        public void checkIfTerminatedAfterEntry()
                                throws Throwable {
                            // nbFielsDecompressed++;
                        }
                    });
                    ArchiveInput.discardToEndOfStream(pis);
                } else {
                    File outputFile = createTranscodedFile(_options.directory(),
                            ae, toType, "zip");
                    StreamCopy.copy(pis, outputFile);
                }
            }
        };
        cxn.execute("asset.transcode", w.document(), null, output);
        _progress.setMessage(
                "transcoded asset " + (cid == null ? assetId : cid));

    }

    private long calcTotalSize(ServerClient.Connection cxn, XmlDoc.Element ae,
            DownloadOptions options) throws Throwable {
        String cid = ae.value("cid");
        if (options.recursive() && cid != null) {
            return cxn.execute("asset.query",
                    "<where>cid='" + cid + "' or cid starts with '" + cid
                            + "'</where><size>infinity</size><action>sum</action><xpath>content/size</xpath>",
                    null, null).longValue("value");
        } else {
            return ae.longValue("content/size", 0);
        }
    }

    private int calcTotalObjects(ServerClient.Connection cxn, XmlDoc.Element ae,
            DownloadOptions options) throws Throwable {
        String cid = ae.value("cid");
        if (options.recursive() && cid != null) {
            XmlDoc.Element re = cxn.execute("asset.query",
                    "<where>cid='" + cid + "' or cid starts with '" + cid
                            + "'</where><size>infinity</size><action>count</action>",
                    null, null);
            return re.intValue("value");
        } else {
            return 1;
        }
    }

}
