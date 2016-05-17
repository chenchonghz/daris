package daris.client.pssd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.mf.client.ServerClient.Input;
import arc.mf.client.archive.Archive;
import arc.streams.StreamCopy;
import arc.streams.StreamCopy.AbortCheck;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;
import daris.client.util.ProgressMonitor;

public class DatasetUtils {

    public static String uploadDataset(ServerClient.Connection cxn, String pid,
            String studyName, Collection<String> inputCids, boolean derived,
            String datasetName, String datasetDescription, File f, boolean gzip,
            ArchiveType archiveType, String type, ProgressMonitor pm)
                    throws Throwable {

        String datasetCid = findDataset(cxn, pid, studyName, datasetName, f);
        if (datasetCid != null) {
            if (pm != null) {
                pm.update(1, 1, "dataset " + datasetCid + " from \""
                        + f.getAbsolutePath() + "\" already exists.");
            }
            return datasetCid;
        }
        if (pid == null) {
            throw new IllegalArgumentException(
                    "No study parent cid or study cid is specified. Cannot find study.");
        }
        String studyCid = StudyUtils.findStudy(cxn, pid, studyName);
        if (studyCid == null) {
            throw new IllegalArgumentException("Cannot find study. (pid='" + pid
                    + "',  study name='" + studyName + "')");
        }
        if (!derived) {
            if (inputCids != null) {
                if (!inputCids.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Unexpected input cids when creating primary dataset.");
                }
            }
        }
        if (f.isDirectory() && archiveType == null) {
            archiveType = ArchiveType.AAR;
        }
        datasetCid = createDataset(cxn, studyCid, inputCids, derived,
                datasetName, datasetDescription, f, gzip, archiveType, type,
                pm);
        return datasetCid;
    }

    static String createDataset(Connection cxn, String studyCid,
            Collection<String> inputCids, boolean derived, String datasetName,
            String datasetDescription, File f, boolean gzip,
            ArchiveType archiveType, String type, ProgressMonitor pm)
                    throws Throwable {
        if (derived) {
            return createDerivedDataset(cxn, studyCid, datasetName,
                    datasetDescription, inputCids, f, archiveType, gzip, type,
                    pm);
        } else {
            return createPrimaryDataset(cxn, studyCid, datasetName,
                    datasetDescription, f, archiveType, gzip, type, pm);
        }
    }

    private static String createPrimaryDataset(Connection cxn, String studyCid,
            String datasetName, String datasetDescription, File f,
            ArchiveType archiveType, boolean gzip, String type,
            ProgressMonitor pm) throws Throwable {

        XmlStringWriter w = new XmlStringWriter();
        addDatasetMeta(cxn, studyCid, datasetName, datasetDescription, f, type,
                archiveType, gzip, w);
        w.push("subject");
        w.add("id", CiteableIdUtils.getParentCID(studyCid, 2));
        w.pop();
        ServerClient.Input input = null;
        if (f.isDirectory()) {
            input = createInputFromDirectory(f, archiveType, pm);
        } else {
            input = createInputFromFile(f, gzip, pm);
        }
        return cxn.execute("om.pssd.dataset.primary.create", w.document(),
                input, null).value("id");
    }

    private static String createDerivedDataset(Connection cxn, String studyCid,
            String datasetName, String datasetDescription,
            Collection<String> inputCids, File f, ArchiveType archiveType,
            boolean gzip, String type, ProgressMonitor pm) throws Throwable {

        XmlStringWriter w = new XmlStringWriter();
        addDatasetMeta(cxn, studyCid, datasetName, datasetDescription, f, type,
                archiveType, gzip, w);
        if (inputCids != null && !inputCids.isEmpty()) {
            for (String inputCid : inputCids) {
                addInputDataset(cxn, inputCid, w);
            }
        }
        ServerClient.Input input = null;
        if (f.isDirectory()) {
            input = createInputFromDirectory(f, archiveType, pm);
        } else {
            input = createInputFromFile(f, gzip, pm);
            if (!gzip && pm != null) {
                pm.update(0, 1,
                        "Uploading \"" + f.getAbsolutePath() + "\"...");
            }
        }
        String datasetCid = cxn.execute("om.pssd.dataset.derivation.create",
                w.document(), input, null).value("id");
        if (!f.isDirectory() && !gzip && pm != null) {
            pm.update(1, 1, "Uploaded \"" + f.getAbsolutePath() + "\".");
        }
        return datasetCid;
    }

    private static void addInputDataset(ServerClient.Connection cxn,
            String inputCid, XmlStringWriter w) throws Throwable {
        XmlDoc.Element ae = ObjectUtils.getAssetMeta(cxn, inputCid);
        w.add("input", new String[] { "vid", ae.value("@vid") }, inputCid);
    }

    private static void addDatasetMeta(ServerClient.Connection cxn,
            String studyCid, String datasetName, String datasetDescription,
            File f, String type, ArchiveType archiveType, boolean gzip,
            XmlStringWriter w) throws Throwable {

        w.add("pid", studyCid);
        if (datasetName != null) {
            w.add("name", datasetName);
        } else {
            w.add("name", f.getName());
        }
        if (datasetDescription != null) {
            w.add("description", datasetDescription);
        }
        if (type != null) {
            w.add("type", type);
        }
        w.add("fillin", true);
        if (f.isDirectory()) {
            w.add("filename", f.getName() + "." + archiveType);
        } else {
            if (gzip) {
                w.add("filename", f.getName() + ".gz");
            } else {
                w.add("filename", f.getName());
            }
        }
        XmlDoc.Element studyAE = ObjectUtils.getAssetMeta(cxn, studyCid);
        w.push("method");
        w.add("id", studyAE.value("meta/daris:pssd-study/method"));
        w.add("step", studyAE.value("meta/daris:pssd-study/method/@step"));
        w.pop();
        w.push("meta");
        w.push("mf-note");
        w.add("note", "source: " + f.getAbsolutePath());
        w.pop();
        w.pop();
    }

    private static Input createInputFromFile(final File f, boolean gzip,
            final ProgressMonitor pm) throws Throwable {
        ServerClient.Input input = null;
        if (gzip) {
            input = new ServerClient.GeneratedInput("application/x-gzip", "gz",
                    f.getAbsolutePath(), -1, null) {
                @Override
                protected void copyTo(OutputStream out, AbortCheck ac)
                        throws Throwable {
                    InputStream in = new BufferedInputStream(
                            new FileInputStream(f));
                    GZIPOutputStream gout = new GZIPOutputStream(out);
                    try {
                        if (pm != null) {
                            pm.update(0, 1, "Uploading \""
                                    + f.getAbsolutePath() + "\"...");
                        }
                        StreamCopy.copy(in, gout, ac);
                        if (pm != null) {
                            pm.update(1, 1, "Uploaded \""
                                    + f.getAbsolutePath() + "\".");
                        }
                    } finally {
                        in.close();
                        gout.close();
                    }
                }
            };
        } else {
            input = new ServerClient.FileInput(f);
        }
        return input;
    }

    private static Input createInputFromDirectory(final File dir,
            final ArchiveType archiveType, final ProgressMonitor pm)
                    throws Throwable {

        return new ServerClient.GeneratedInput(archiveType.mimeType(),
                archiveType.fileExtension(), dir.getAbsolutePath(), -1, null) {
            @Override
            protected void copyTo(OutputStream out, AbortCheck ac)
                    throws Throwable {
                Archive.declareSupportForAllTypes();
                ArchiveOutput ao = ArchiveRegistry.createOutput(out,
                        archiveType.mimeType(), 6, null);
                try {
                    ArchiveUtils.addToArchive(dir, ao, pm, ac);
                } finally {
                    ao.close();
                }
            }
        };
    }

    static String findDataset(ServerClient.Connection cxn, String pid,
            String studyName, String datasetName, File f) throws Throwable {
        StringBuilder query = new StringBuilder();
        query.append(
                "(model='om.pssd.dataset' and xpath(mf-note/note) ends with '"
                        + f.getAbsolutePath() + "')");
        if (pid != null) {
            query.append(" and (cid starts with '" + pid + "' or cid='" + pid
                    + "')");
        }
        if (studyName != null) {
            query.append(
                    " and (cid contained by(xpath(daris:pssd-object/name)='"
                            + studyName + "'))");
        }
        if (datasetName != null) {
            query.append(" and (xpath(daris:pssd-object/name)='" + datasetName
                    + "')");
        }
        XmlStringWriter w = new XmlStringWriter();
        w.add("where", query.toString());
        w.add("size", 1);
        w.add("action", "get-cid");
        return cxn.execute("asset.query", w.document()).value("cid");
    }

}
