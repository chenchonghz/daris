package daris.transcode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.transcode.TranscoderImpl;
import arc.mime.MimeType;

public abstract class DarisTranscodeImpl implements TranscoderImpl {

    public static final String ERROR_FILE_NAME = "error.txt";
    public static final int COMPRESS_LEVEL = 6;

    public static class MimeTypes {
        public static final String ZIP = "application/zip";
        public static final String XZIP = "application/x-zip";
        public static final String TAR = "application/x-tar";
        public static final String AAR = "application/arc-archive";
        public static final String GZIP = "application/x-gzip";
        public static final String BZIP2 = "application/x-bzip2";

        public static boolean isZIP(String mimeType) {
            return ZIP.equalsIgnoreCase(mimeType)
                    || XZIP.equalsIgnoreCase(mimeType);
        }

        public static boolean isAAR(String mimeType) {
            return AAR.equalsIgnoreCase(mimeType);
        }

        public static boolean isTAR(String mimeType) {
            return TAR.equalsIgnoreCase(mimeType);
        }

        public static boolean isGZIP(String mimeType) {
            return GZIP.equalsIgnoreCase(mimeType);
        }

        public static boolean isBZIP2(String mimeType) {
            return BZIP2.equalsIgnoreCase(mimeType);
        }

        public static boolean isArchiveType(String mimeType) {
            return isAAR(mimeType) || isZIP(mimeType) || isTAR(mimeType);
        }

        public static boolean isCompressType(String mimeType) {
            return isGZIP(mimeType) || isBZIP2(mimeType);
        }
    }

    public static class ResourceManager {

        private ResourceManager() {
        }

        private static long _limit = 2000000000L;
        private static long _used = 0L;

        static synchronized void setLimit(int limit) {
            _limit = limit;
        }

        public static synchronized void start(long size) throws Throwable {
            while (_used + size > _limit) {
                if (size > _limit) {
                    throw new Exception("Reached resource limit(" + _limit
                            + " bytes).");
                } else {
                    ResourceManager.class.wait();
                }
            }
            _used += size;
        }

        public static synchronized void stop(long size) {
            _used -= size;
            ResourceManager.class.notifyAll();
        }
    }

    @Override
    public final String transcode(File inputFile, MimeType fromType,
            MimeType fromContentType, MimeType toType, File out,
            Map<String, String> params) throws Throwable {
        File tmpDir = PluginService.createTemporaryDirectory();
        try {
            extract(inputFile, fromContentType, tmpDir);
            final File[] inputFiles = tmpDir.listFiles();
            final long inputSize = FileUtils.sizeOf(tmpDir);
            boolean resourceManagerStarted = false;
            try {
                if (resourceManaged()) {
                    ResourceManager.start(inputSize);
                    resourceManagerStarted = true;
                }
                transcode(tmpDir, fromType, toType, params);
            } finally {
                if (resourceManaged() && resourceManagerStarted) {
                    ResourceManager.stop(inputSize);
                }
            }
            forceDelete(inputFiles);
            File[] outputFiles = tmpDir.listFiles();
            if (outputFiles == null || outputFiles.length == 0) {
                appendToErrorFile(tmpDir, "No output found.");
            }
            String outputType = outputType(fromType, toType);
            if (!MimeTypes.isArchiveType(outputType)) {
                // Defaults to AAR
                outputType = MimeTypes.AAR;
            }
            archive(tmpDir, out, outputType);
            return outputType;
        } finally {
            forceDelete(tmpDir);
        }
    }

    @Override
    public String outputType(MimeType fromType, MimeType toType) {
        // Defaults to AAR
        return MimeTypes.AAR;
    }

    @Override
    public String description() {
        return from() + " to " + to() + " transcoder.";
    }

    protected abstract void transcode(File dir, MimeType fromType,
            MimeType toType, Map<String, String> params) throws Throwable;

    protected abstract boolean resourceManaged();

    public abstract String from();

    public abstract String to();

    public abstract DarisTranscodeProvider provider();

    private static void extract(File inputFile, MimeType type, File outputDir)
            throws Throwable {
        String typeName = type.name();
        if (MimeTypes.isAAR(typeName)) {
            ArchiveInput ai = ArchiveRegistry.createInput(inputFile, type);
            try {
                ArchiveExtractor.extract(ai, outputDir, false, true, true);
            } finally {
                ai.close();
            }
        } else if (MimeTypes.isZIP(typeName) || MimeTypes.isTAR(typeName)) {
            List<File> outputFiles = unarchive(inputFile, outputDir);
            if (outputFiles == null) {
                throw new Exception("Failed to extract archive: "
                        + inputFile.getAbsolutePath() + ". MIME type: "
                        + typeName + ".");
            }
        } else if (MimeTypes.isGZIP(typeName) || MimeTypes.isBZIP2(typeName)) {
            File outputFile = decompress(inputFile, outputDir);
            if (outputFile == null) {
                throw new Exception("Failed to decompress archive: "
                        + inputFile.getAbsolutePath() + ". MIME type: "
                        + typeName + ".");
            }
            List<File> outputFiles = unarchive(inputFile, outputDir);
            if (outputFiles != null) {
                // the input must be a tar.gz or tar.bz2 etc. So delete the
                // archive file because it has been extracted.
                FileUtils.forceDelete(outputFile);
            }
        } else {
            // not an archive, simply copy the file to the directory
            FileUtils.copyFileToDirectory(inputFile, outputDir);
        }
    }

    private static File decompress(File inputFile, File outputDir)
            throws Throwable {
        String outputFileName = inputFile.getName();
        if (outputFileName.endsWith(".gz")) {
            outputFileName = outputFileName.substring(0,
                    outputFileName.length() - 3);
        } else if (outputFileName.endsWith(".bz2")) {
            outputFileName = outputFileName.substring(0,
                    outputFileName.length() - 4);
        }
        File of = new File(outputDir, outputFileName);
        InputStream is = new BufferedInputStream(new FileInputStream(inputFile));
        OutputStream os = new BufferedOutputStream(new FileOutputStream(of));
        CompressorInputStream cis = null;
        boolean decompressed = false;
        try {
            cis = new CompressorStreamFactory().createCompressorInputStream(is);
            IOUtils.copy(cis, os);
            decompressed = true;
        } catch (Throwable e) {
        } finally {
            is.close();
            if (cis != null) {
                cis.close();
            }
            os.close();
        }
        if (decompressed) {
            return of;
        } else {
            return null;
        }
    }

    private static void archive(File inputDir, File outputFile,
            String outputType) throws Throwable {
        ArchiveOutput ao = ArchiveRegistry.createOutput(outputFile, outputType,
                COMPRESS_LEVEL, null);
        try {
            File[] files = inputDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    ao.add(null, file.getName(), file);
                }
            }
        } finally {
            ao.close();
        }
    }

    private static List<File> unarchive(File inputFile, File outputDir)
            throws Throwable {
        InputStream is = new BufferedInputStream(new FileInputStream(inputFile));
        ArchiveInputStream ais = null;
        List<File> ofs = new ArrayList<File>();
        try {
            ais = new ArchiveStreamFactory().createArchiveInputStream(is);
            ArchiveEntry ae = ais.getNextEntry();
            while (ae != null) {
                if (!ae.isDirectory()) {
                    File of = new File(outputDir, ae.getName());
                    OutputStream os = new FileOutputStream(of);
                    try {
                        IOUtils.copy(ais, os);
                    } finally {
                        os.close();
                    }
                    ofs.add(of);
                }
                ae = ais.getNextEntry();
            }
        } catch (Throwable e) {
            // failed to extract
        } finally {
            is.close();
            if (ais != null) {
                ais.close();
            }
        }
        if (ofs.isEmpty()) {
            return null;
        } else {
            return ofs;
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
            System.out.println("Failed to delete "
                    + (file.isDirectory() ? "directory" : "file") + ": "
                    + file.getAbsolutePath()
                    + ". It will be deleted when jvm exits.");
            FileUtils.forceDeleteOnExit(file);
        }
    }

    private static void forceDelete(File[] files) throws Throwable {
        for (File file : files) {
            forceDelete(file);
        }
    }

    private static void appendToErrorFile(File dir, String errorText)
            throws Throwable {
        File file = new File(dir, ERROR_FILE_NAME);
        PrintWriter w = null;
        try {
            w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), "UTF-8")));
            w.println(errorText);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }
}
