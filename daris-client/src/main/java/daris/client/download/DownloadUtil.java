package daris.client.download;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.ServerClient;
import arc.mf.client.archive.Archive;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.NonCloseInputStream;
import arc.streams.ProgressMonitoredInputStream;
import arc.streams.StreamCopy;
import arc.utils.ProgressMonitor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import daris.client.download.DownloadOptions.Parts;
import daris.client.util.CiteableIdUtils;
import daris.client.util.XmlUtils;

public class DownloadUtil {

    public static void download(ServerClient.Connection cxn, Logger logger,
            Set<String> cids, DownloadOptions options) throws Throwable {
        for (String cid : cids) {
            download(cxn, logger, cid, options);
        }
    }

    public static void download(ServerClient.Connection cxn, Logger logger,
            String cid, DownloadOptions options) throws Throwable {
        XmlDoc.Element ae = cxn.execute("asset.get",
                "<cid>" + cid + "</cid><lock>true</lock>", null, null)
                .element("asset");
        String assetId = ae.value("@id");
        try {
            if (!options.recursive()) {
                downloadObject(cxn, logger, ae, options);
            } else {
                StringBuilder sb = new StringBuilder("(cid='").append(cid)
                        .append("' or cid starts with '").append(cid)
                        .append("')");
                if (options.filter() != null) {
                    sb.append("and (").append(options.filter()).append(")");
                }
                if (options.datasetOnly()) {
                    sb.append("and (model='om.pssd.dataset')");
                }
                int idx = 1;
                int size = 100;
                int remaining = Integer.MAX_VALUE;
                XmlDoc.Element re = null;
                while (remaining > 0) {
                    re = cxn.execute("asset.query",
                            "<where>" + sb.toString()
                                    + "</where><count>true</count><idx>" + idx
                                    + "</idx><size>" + size
                                    + "</size><action>get-meta</action>",
                            null, null);
                    remaining = re.intValue("cursor/remaining", 0);
                    List<XmlDoc.Element> caes = re.elements("asset");
                    if (caes != null) {
                        for (XmlDoc.Element cae : caes) {
                            downloadObject(cxn, logger, cae, options);
                        }
                    }
                    idx += size;
                }
            }
        } finally {
            cxn.execute("asset.unlock", "<id>" + assetId + "</id>", null, null);
        }
    }

    private static void downloadObject(ServerClient.Connection cxn,
            Logger logger, XmlDoc.Element ae, DownloadOptions options)
                    throws Throwable {
        File objectDir = createObjectDirectory(options.outputDir(), ae);
        if (options.includeAttachments()) {
            Collection<String> attachments = ae
                    .values("related[@type='attachment']/to");
            if (attachments != null && !attachments.isEmpty()) {
                for (String attachment : attachments) {
                    downloadAttachment(cxn, logger, ae, attachment, objectDir,
                            options);
                }
            }
        }
        if (options.parts() == Parts.META || options.parts() == Parts.ALL) {
            downloadMeta(cxn, logger, ae, objectDir, options);
        }
        if (options.parts() == Parts.CONTENT || options.parts() == Parts.ALL) {
            String mimeType = ae.value("type");
            if (ae.elementExists("content")) {
                if (options.hasTranscode(mimeType)) {
                    transcodeContent(cxn, logger, ae, objectDir, options);
                } else {
                    downloadContent(cxn, logger, ae, objectDir, options);
                }
            }
        }
    }

    private static String titleFor(XmlDoc.Element ae) throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        if (cid != null) {
            String objectType = ae.value("meta/daris:pssd-object/type");
            if (objectType != null) {
                return objectType + " " + cid;
            }
        }
        return "asset " + assetId;
    }

    private static File createObjectDirectory(File outputDir, XmlDoc.Element ae)
            throws Throwable {
        StringBuilder sb = new StringBuilder();
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        if (cid == null) {
            sb.append(File.separator).append(assetId).toString();
        } else {
            String projectCID = CiteableIdUtils.getProjectCID(cid);
            sb.append(File.separator);
            sb.append(projectCID);
            if (!CiteableIdUtils.isProjectCID(cid)) {
                String subjectCID = CiteableIdUtils.getSubjectCID(cid);
                sb.append(File.separator);
                sb.append(subjectCID);
                if (!(CiteableIdUtils.isSubjectCID(cid)
                        || CiteableIdUtils.isExMethodCID(cid))) {

                    String studyCID = CiteableIdUtils.getStudyCID(cid);
                    sb.append(File.separator);
                    sb.append(studyCID);
                    if (!CiteableIdUtils.isStudyCID(cid)) {
                        sb.append(File.separator);
                        sb.append(cid);
                    }
                }
            }
        }
        File objectDir = new File(outputDir, sb.toString());
        if (!objectDir.exists()) {
            objectDir.mkdirs();
        }
        return objectDir;
    }

    private static File createMetaFile(File objectDir, XmlDoc.Element ae)
            throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        String fileName = new StringBuilder(cid == null ? assetId : cid)
                .append(".meta.xml").toString();
        File file = new File(objectDir, fileName);
        return file;
    }

    private static File createAttachmentDirectory(File objectDir,
            XmlDoc.Element aae) throws Throwable {
        String attachmentDirName = "attachment-" + aae.value("@id");
        File attachmentDir = new File(objectDir, attachmentDirName);
        if (!attachmentDir.exists()) {
            attachmentDir.mkdirs();
        }
        return attachmentDir;
    }

    private static File createContentDirectory(File objectDir,
            XmlDoc.Element ae) throws Throwable {
        String type = ae.value("type");
        if (type != null) {
            File contentDir = new File(objectDir, type.replace('/', '_'));
            if (!contentDir.exists()) {
                contentDir.mkdirs();
            }
            return contentDir;
        } else {
            return objectDir;
        }
    }

    private static File createContentFile(File contentDir, XmlDoc.Element ae)
            throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        String ext = ae.value("content/type/@ext");
        String contentFileName = ae.value("meta/daris:pssd-filename/original");
        if (contentFileName == null) {
            contentFileName = cid == null ? assetId : cid;
            if (ext != null) {
                contentFileName = contentFileName + "." + ext;
            }
        }
        File contentFile = new File(contentDir, contentFileName);
        return contentFile;
    }

    private static File createTranscodedDirectory(File objectDir, String type)
            throws Throwable {
        File transcodedDir = new File(objectDir, type.replace('/', '_'));
        transcodedDir.mkdirs();
        return transcodedDir;
    }

    private static File createTranscodedFile(File transcodedDir,
            XmlDoc.Element ae, String toType, String toExt) throws Throwable {
        String cid = ae.value("cid");
        String assetId = ae.value("@id");
        String ext = ae.value("content/type/@ext");
        String fileName = ae.value("meta/daris:pssd-filename/original");

        if (fileName == null) {
            fileName = cid == null ? assetId : cid;
        }
        if (ext != null && fileName.endsWith("." + ext)) {
            fileName = fileName.substring(0,
                    fileName.length() - ext.length() - 1);
        }
        if (!fileName.endsWith("." + toExt)) {
            fileName += "." + toExt;
        }

        File file = new File(transcodedDir, fileName);
        return file;
    }

    private static void downloadMeta(ServerClient.Connection cxn, Logger logger,
            XmlDoc.Element ae, File objectDir, DownloadOptions options)
                    throws Throwable {
        File file = createMetaFile(objectDir, ae);
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
        logger.info("writing file: " + file.getAbsolutePath());
        XmlUtils.saveToFile(e, file);
    }

    private static void downloadAttachment(ServerClient.Connection cxn,
            final Logger logger, XmlDoc.Element ae,
            final String attachmentAssetId, File objectDir,
            DownloadOptions options) throws Throwable {

        /*
         * attachment asset meta
         */
        XmlDoc.Element aae = cxn.execute("asset.get",
                "<id>" + attachmentAssetId + "</id>", null, null)
                .element("asset");

        /*
         * attachment file
         */
        String attachmentFileName = aae.value("name");
        if (attachmentFileName == null) {
            attachmentFileName = attachmentAssetId;
            String ext = aae.value("content/type/@ext");
            if (ext != null) {
                attachmentFileName = attachmentFileName + "." + ext;
            }
        }
        File attachmentDir = createAttachmentDirectory(objectDir, aae);
        final File attachmentFile = new File(attachmentDir, attachmentFileName);
        if (attachmentFile.exists() && !options.overwrite()) {
            logger.info("skipping attachment file: "
                    + attachmentFile.getAbsolutePath());
            return;
        }

        /*
         * progress monitor
         */
        final ProgressMonitor pm = new ProgressMonitor() {
            private long _downloaded = 0;

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
                _downloaded += itemProgress;
                logger.fine("downloading attachment file: "
                        + attachmentFile.getAbsolutePath() + "... "
                        + _downloaded + "bytes downloaded.");
            }
        };

        /*
         * output handler
         */
        ServerClient.OutputConsumer output = new ServerClient.OutputConsumer() {
            @Override
            protected void consume(Element re, LongInputStream is)
                    throws Throwable {
                ProgressMonitoredInputStream pis = new ProgressMonitoredInputStream(
                        pm, is, true);
                logger.info(
                        "writing file: " + attachmentFile.getAbsolutePath());
                StreamCopy.copy(pis, attachmentFile);
            }
        };
        logger.info("downloading attachment file: "
                + attachmentFile.getAbsolutePath());
        cxn.execute("asset.content.get", "<id>" + attachmentAssetId + "</id>",
                null, output);
    }

    private static void downloadContent(ServerClient.Connection cxn,
            final Logger logger, final XmlDoc.Element ae, final File objectDir,
            final DownloadOptions options) throws Throwable {
        String assetId = ae.value("@id");
        final String title = titleFor(ae);
        final String ctype = ae.value("content/type");
        final File contentDir = createContentDirectory(objectDir, ae);
        final ProgressMonitor pm = new ProgressMonitor() {

            private long _downloaded = 0;

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
                _downloaded += itemProgress;
                logger.fine("downloading content of " + title + "... "
                        + _downloaded + " bytes downloaded.");
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
                        && options.decompress()) {
                    ArchiveInput ai = ArchiveRegistry.createInput(
                            new NonCloseInputStream(pis),
                            new NamedMimeType(ctype));
                    logger.info("extracting content archive of " + title);
                    ArchiveExtractor.extract(ai, contentDir, true,
                            options.overwrite(), false,
                            new ArchiveExtractor.Terminator() {
                        private long _totalDecompressed = 0;

                        @Override
                        public void checkIfTerminatedProcessed(
                                long bytesDecompressed) throws Throwable {
                            _totalDecompressed += bytesDecompressed;
                            logger.fine("extracting content archive of " + title
                                    + "... " + _totalDecompressed
                                    + " bytes extracted.");

                        }

                        @Override
                        public void checkIfTerminatedAfterEntry()
                                throws Throwable {
                            // nbFielsDecompressed++;
                        }
                    });
                    ArchiveInput.discardToEndOfStream(pis);
                } else {
                    File contentFile = createContentFile(contentDir, ae);
                    if (!contentFile.exists() || options.overwrite()) {
                        logger.info("writing file: "
                                + contentFile.getAbsolutePath());
                        StreamCopy.copy(pis, contentFile);
                    } else {
                        logger.info("skipping file: "
                                + contentFile.getAbsolutePath());
                        // TODO: check if pis.discard() can be used.
                        StreamCopy.copy(pis, new OutputStream() {
                            @Override
                            public void write(int b) {
                            }
                        });
                    }

                }
            }
        };
        logger.info("downloading content of " + title);
        cxn.execute("asset.content.get", "<id>" + assetId + "</id>", null,
                output);
    }

    private static void transcodeContent(ServerClient.Connection cxn,
            final Logger logger, final XmlDoc.Element ae, File objectDir,
            final DownloadOptions options) throws Throwable {
        String assetId = ae.value("@id");
        String type = ae.value("type");
        final String toType = options.transcode(type);
        final String atype = options.decompress() ? "aar" : "zip";
        final String title = titleFor(ae);
        final File transcodedDir = createTranscodedDirectory(objectDir, toType);
        logger.fine("created directory: " + transcodedDir);
        XmlStringWriter w = new XmlStringWriter();
        w.add("id", assetId);
        w.add("atype", atype);
        w.push("transcode");
        w.add("from", type);
        w.add("to", toType);
        w.pop();

        final ProgressMonitor pm = new ProgressMonitor() {
            private long _downloaded;

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
                _downloaded += itemProgress;
                logger.fine("downloading transcoded content of " + title
                        + "... " + _downloaded + " bytes downloaded.");
            }
        };

        ServerClient.OutputConsumer output = new ServerClient.OutputConsumer() {
            @Override
            protected void consume(Element re, LongInputStream is)
                    throws Throwable {
                ProgressMonitoredInputStream pis = new ProgressMonitoredInputStream(
                        pm, is, true);
                Archive.declareSupportForAllTypes();
                if (options.decompress()) {
                    ArchiveInput ai = ArchiveRegistry.createInput(
                            new NonCloseInputStream(pis),
                            new NamedMimeType("application/arc-archive"));
                    logger.info("extracting transcoded content of " + title);
                    ArchiveExtractor.extract(ai, transcodedDir, true,
                            options.overwrite(), false,
                            new ArchiveExtractor.Terminator() {
                        private long _totalDecompressed = 0;

                        @Override
                        public void checkIfTerminatedProcessed(
                                long bytesDecompressed) throws Throwable {
                            _totalDecompressed += bytesDecompressed;
                            logger.fine("extracting transcoded content of "
                                    + title + "... " + _totalDecompressed
                                    + " bytes extracted.");
                        }

                        @Override
                        public void checkIfTerminatedAfterEntry()
                                throws Throwable {
                            // nbFielsDecompressed++;
                        }
                    });
                    ArchiveInput.discardToEndOfStream(pis);
                } else {

                    File transcodedFile = createTranscodedFile(transcodedDir,
                            ae, toType, atype);
                    if (!transcodedFile.exists() || options.overwrite()) {
                        logger.info("writing file: "
                                + transcodedFile.getAbsolutePath());
                        StreamCopy.copy(pis, transcodedFile);
                    } else {
                        logger.info("skipping file: "
                                + transcodedFile.getAbsolutePath());
                        // TODO: check if pis.discard() can be used.
                        StreamCopy.copy(pis, new OutputStream() {
                            @Override
                            public void write(int b) {
                            }
                        });
                    }
                }
            }
        };
        logger.info("transcoding " + title + " from " + type + " to " + toType);
        cxn.execute("asset.transcode", w.document(), null, output);
    }

}
