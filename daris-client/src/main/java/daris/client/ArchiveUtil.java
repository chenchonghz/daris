package daris.client;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.archive.Archive;
import arc.mime.NamedMimeType;
import arc.streams.StreamCopy;

public class ArchiveUtil {

    public static void main(String[] args) throws Throwable {
        if (args.length < 2) {
            System.err.println("Error: missing arguments.");
            showUsage();
            System.exit(1);
        }
        /*
         * action
         */
        String action = args[0];
        if (!"extract".equals(action) && !"archive".equals(action)) {
            System.err.println("Error: invalid action: " + action
                    + ". Expects 'extract' or 'archive'.");
            showUsage();
            System.exit(2);
        }
        /*
         * quiet
         */
        boolean quiet = "--quiet".equalsIgnoreCase(args[1]);
        if (quiet && args.length < 3) {
            System.err.println("Error: missing arguments.");
            showUsage();
            System.exit(1);
        }

        /*
         * arc file
         */
        String arcFilePath = quiet ? args[2] : args[1];
        if (!isArchiveFilePath(arcFilePath)) {
            System.err.println("Error: invalid archive file name: "
                    + arcFilePath + ". Expects a .zip or .aar file.");
            System.exit(3);
        }
        File arcFile = new File(arcFilePath);
        if ("extract".equals(action)) {
            if (!arcFile.exists()) {
                System.err.println("Archive file: " + arcFile.getAbsolutePath()
                        + " does not exist.");
                System.exit(4);
            }
            File outputDir = null;
            if (args.length >= 3) {
                outputDir = quiet ? new File(args[3]) : new File(args[2]);
                if (!outputDir.exists()) {
                    System.err.println("Output directory: "
                            + outputDir.getAbsolutePath() + " does not exist.");
                    System.exit(5);
                }
                if (!outputDir.isDirectory()) {
                    System.err.println(
                            "Output directory: " + outputDir.getAbsolutePath()
                                    + " is not a directory.");
                    System.exit(6);
                }
            }
            extract(arcFile, outputDir, quiet);
        } else {
            int start = quiet ? 3 : 2;
            if (args.length == start) {
                System.err.println("No input file/directory is specified.");
                System.exit(7);
            }
            Set<File> inputFiles = new TreeSet<File>();
            for (int i = start; i < args.length; i++) {
                addFiles(inputFiles, new File(args[i]));
            }
            if (inputFiles.isEmpty()) {
                System.err.println(
                        "No input file is found in the specified directories.");
                System.exit(8);
            }
            archive(arcFile, new File(System.getProperty("user.dir")),
                    inputFiles, quiet);
        }
    }

    private static void extract(File arcFile, File outputDir, boolean quiet)
            throws Throwable {
        Archive.declareSupportForAllTypes();
        ArchiveInput ai = ArchiveRegistry.createInput(arcFile,
                new NamedMimeType(getMimeType(arcFile)));
        try {
            ArchiveInput.Entry entry = null;
            while ((entry = ai.next()) != null) {
                String ename = entry.name();
                if (!quiet) {
                    System.out.print("Extracting " + ename + "...");
                }
                if (ename.startsWith("/")) {
                    ename = ename.substring(1);
                }
                File of = outputDir == null ? new File(ename)
                        : new File(outputDir, ename);
                if (entry.isDirectory()) {
                    of.mkdirs();
                } else {
                    of.getParentFile().mkdirs();
                    of.createNewFile();
                    StreamCopy.copy(entry.stream(), of);
                }
                ai.closeEntry();
                if (!quiet) {
                    System.out.println("done");
                }
            }
        } finally {
            ai.close();
        }
    }

    private static void archive(File arcFile, File baseDir,
            Set<File> inputFiles, boolean quiet) throws Throwable {
        Archive.declareSupportForAllTypes();
        ArchiveOutput ao = ArchiveRegistry.createOutput(arcFile,
                getMimeType(arcFile), 6, null);
        try {
            for (File inputFile : inputFiles) {
                String ename = inputFile.getAbsolutePath();
                String baseDirPath = baseDir.getAbsolutePath();
                if (ename.startsWith(baseDirPath + File.separator)
                        || ename.startsWith(baseDirPath + "/")) {
                    ename = ename.substring(baseDirPath.length());
                }
                if (ename.startsWith(File.separator) || ename.startsWith("/")) {
                    ename = ename.substring(1);
                }
                if (!quiet) {
                    System.out.print("Adding " + ename + "...");
                }
                ao.add(null, ename, inputFile);
                if (!quiet) {
                    System.out.println("done");
                }
            }
        } finally {
            ao.close();
        }
    }

    private static void addFiles(final Set<File> files, File f) {
        if (!f.exists()) {
            return;
        }
        if (f.isFile()) {
            files.add(f);
        } else {
            File[] ffs = f.listFiles();
            for (File ff : ffs) {
                if (ff.isFile()) {
                    files.add(ff);
                }
                if (ff.isDirectory()) {
                    addFiles(files, ff);
                }
            }
        }
    }

    private static String getFileExtension(File f) {
        String name = f.getName();
        int idx = name.lastIndexOf('.');
        if (idx == -1) {
            return null;
        }
        return name.substring(idx + 1);
    }

    private static String getMimeType(File arcFile) throws Throwable {
        String ext = getFileExtension(arcFile);
        if ("zip".equalsIgnoreCase(ext)) {
            return "application/zip";
        } else if ("aar".equalsIgnoreCase(ext)) {
            return "application/arc-archive";
        } else {
            throw new Exception(
                    "Unsupported archive type: " + ext.toLowerCase());
        }
    }

    private static boolean isArchiveFilePath(String path) {
        if (path == null) {
            return false;
        }
        return path.endsWith(".zip") || path.endsWith(".aar")
                || path.endsWith(".ZIP") || path.endsWith(".AAR");
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println(
                "    darc extract [--quiet] <archive-file> [output-directory]");
        System.out.println(
                "    darc archive [--quiet] <archive-file> <files/directories>");
        System.out.println("Examples:");
        System.out.println("    darc extract book.zip");
        System.out.println("    darc extract book.aar /home/wilson/Documents");
        System.out.println(
                "    darc archive book.zip /home/wilson/Downloads/book");
        System.out.println(
                "    darc archive book.aar /home/wilson/Downloads/book");

    }
}
