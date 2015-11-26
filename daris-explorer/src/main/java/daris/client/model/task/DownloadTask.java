package daris.client.model.task;

import java.io.File;
import java.util.List;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.ServerClient;
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
import daris.client.model.object.DObjectRef;

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

    public void start() {
        DownloadTaskManager.get().addTask(this);
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
                    String mimeType = ae.value("type");
                    long csize = ae.longValue("content/size");
                    if (ae.elementExists("content")) {
                        if (_options.hasTranscodeFor(mimeType)) {
                            transcodeContent(cxn, ae);
                        } else {
                            downloadContent(cxn, ae);
                        }
                        _progress.incProcessedSize(csize);
                        _progress.incProcessedObjects();
                    }
                } else {
                    int idx = 1;
                    int size = 100;
                    int remaining = Integer.MAX_VALUE;
                    XmlDoc.Element re = null;
                    while (remaining > 0) {
                        re = cxn.execute("asset.query",
                                "<where>cid starts with '" + cid
                                        + "' and asset has content</where><count>true</count><idx>"
                                        + idx + "</idx><size>" + size
                                        + "</size><action>get-meta</action>",
                                null, null);
                        remaining = re.intValue("cursor/remaining", 0);
                        List<XmlDoc.Element> caes = re.elements("asset");
                        if (caes != null) {
                            for (XmlDoc.Element cae : caes) {
                                String mimeType = cae.value("type");
                                long csize = cae.longValue("content/size");
                                checkIfAborted();
                                if (_options.hasTranscodeFor(mimeType)) {
                                    transcodeContent(cxn, cae);
                                } else {
                                    downloadContent(cxn, cae);
                                }
                                _progress.incProcessedSize(csize);
                                _progress.incProcessedObjects();
                            }
                        }
                        idx += size;
                    }
                }
                _progress.setProgress(1.0f);
            } finally {
                cxn.execute("asset.unlock", "<id>" + assetId + "</id>", null,
                        null);
            }
        } finally {
            cxn.close();
        }
    }

    private void downloadContent(ServerClient.Connection cxn, XmlDoc.Element ae)
            throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        _progress.setMessage(
                "transcoding asset " + (cid == null ? assetId : cid));
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
                if (ArchiveRegistry.isAnArchive(ctype)
                        && _options.decompress()) {
                    File outputDirectory = createOutputDirectoryFor(ae, null);
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
                    File outputFile = createOutputFileFor(ae, null, null);
                    StreamCopy.copy(pis, outputFile);
                }
            }
        };
        cxn.execute("asset.content.get", "<id>" + assetId + "</id>", null,
                output);
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
                    File outputDirectory = createOutputDirectoryFor(ae, toType);
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
                    File outputFile = createOutputFileFor(ae, "application/zip",
                            "zip");
                    StreamCopy.copy(pis, outputFile);
                }
            }
        };
        cxn.execute("asset.transcode", w.document(), null, output);
    }

    private File createOutputFileFor(XmlDoc.Element ae, String mimeType,
            String fileExt) throws Throwable {
        StringBuilder sb = new StringBuilder(_options.directory());
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        mimeType = mimeType == null ? ae.value("type") : mimeType;
        fileExt = fileExt == null ? ae.value("content/type/@ext") : fileExt;
        String fileName = ae.value("meta/daris:pssd-filename/original");
        fileName = fileName == null ? (cid == null ? assetId : cid) : fileName;
        fileName = fileName.endsWith("." + fileExt) ? fileName
                : (fileName + "." + fileExt);
        if (cid != null) {
            sb.append(File.separatorChar);
            sb.append(cid);
        }
        sb.append(File.separatorChar);
        sb.append(fileName);
        File file = new File(sb.toString());
        file.getParentFile().mkdirs();
        return file;
    }

    private File createOutputDirectoryFor(XmlDoc.Element ae, String mimeType)
            throws Throwable {
        StringBuilder sb = new StringBuilder(_options.directory());
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        mimeType = mimeType == null ? ae.value("type") : mimeType;
        sb.append(File.separatorChar);
        sb.append(cid == null ? assetId : cid);
        if (mimeType != null) {
            sb.append(File.separatorChar);
            sb.append(mimeType.replace('/', '_'));
        }
        File dir = new File(sb.toString());
        dir.mkdirs();
        return dir;
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
            return cxn.execute("asset.query",
                    "<where>(cid='" + cid + "' or cid starts with '" + cid
                            + "') and asset has content</where><size>infinity</size><action>count</action>",
                    null, null).intValue("value");
        } else {
            return 1;
        }
    }

}
