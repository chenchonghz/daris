package daris.client.cli;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import daris.client.download.DownloadOptions;
import daris.client.download.DownloadOptions.Parts;
import daris.client.download.DownloadUtil;
import daris.client.util.BooleanUtil;
import daris.client.util.CiteableIdUtils;

public class DownloadCLI {

    public static final String LOG_DIR = System.getProperty("user.home")
            + File.pathSeparator + ".daris";

    public static void main(String[] args) throws Throwable {

        if (args == null || args.length == 0) {
            showHelp();
            System.exit(1);
        }
        String mfHost = null;
        int mfPort = -1;
        String mfTransport = null;
        boolean useHttp = true;
        boolean encrypt = true;
        String mfAuth = null;
        String mfToken = null;
        String mfSid = null;

        Set<String> cids = new LinkedHashSet<String>();
        DownloadOptions options = new DownloadOptions();

        try {
            for (int i = 0; i < args.length;) {
                if (args[i].equals("--help") || args[i].equals("-h")) {
                    showHelp();
                    System.exit(0);
                } else if (args[i].equals("--mf.host")) {
                    if (mfHost != null) {
                        throw new IllegalArgumentException(
                                "--mf.host has already been specified.");
                    }
                    mfHost = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.port")) {
                    if (mfPort > 0) {
                        throw new IllegalArgumentException(
                                "--mf.port has already been specified.");
                    }
                    try {
                        mfPort = Integer.parseInt(args[i + 1]);
                    } catch (Throwable e) {
                        throw new IllegalArgumentException(
                                "Invalid mf.port: " + args[i + 1], e);
                    }
                    if (mfPort <= 0 || mfPort > 65535) {
                        throw new IllegalArgumentException(
                                "Invalid mf.port: " + args[i + 1]);
                    }
                    i += 2;
                } else if (args[i].equals("--mf.transport")) {
                    if (mfTransport != null) {
                        throw new IllegalArgumentException(
                                "--mf.transport has already been specified.");
                    }
                    mfTransport = args[i + 1];
                    i += 2;
                    if ("http".equalsIgnoreCase(mfTransport)) {
                        useHttp = true;
                        encrypt = false;
                    } else if ("https".equalsIgnoreCase(mfTransport)) {
                        useHttp = true;
                        encrypt = true;
                    } else if ("tcp/ip".equalsIgnoreCase(mfTransport)) {
                        useHttp = false;
                        encrypt = false;
                    } else {
                        throw new IllegalArgumentException(
                                "Invalid mf.transport: " + mfTransport
                                        + ". Expects http, https or tcp/ip.");
                    }
                } else if (args[i].equals("--mf.auth")) {
                    if (mfAuth != null) {
                        throw new IllegalArgumentException(
                                "--mf.auth has already been specified.");
                    }
                    if (mfSid != null || mfToken != null) {
                        throw new IllegalArgumentException(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfAuth = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.token")) {
                    if (mfToken != null) {
                        throw new IllegalArgumentException(
                                "--mf.token has already been specified.");
                    }
                    if (mfSid != null || mfAuth != null) {
                        throw new IllegalArgumentException(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfToken = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.sid")) {
                    if (mfSid != null) {
                        throw new IllegalArgumentException(
                                "--mf.sid has already been specified.");
                    }
                    if (mfToken != null || mfAuth != null) {
                        throw new IllegalArgumentException(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfSid = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--recursive")) {
                    boolean recursive = BooleanUtil.parseBoolean(args[i + 1],
                            true);
                    options.setRecursive(recursive);
                    i += 2;
                } else if (args[i].equals("--parts")) {
                    options.setParts(DownloadOptions.Parts
                            .fromString(args[i + 1], Parts.CONTENT));
                    i += 2;

                } else if (args[i].equals("--include-attachments")) {
                    boolean includeAttachments = BooleanUtil
                            .parseBoolean(args[i + 1], false);
                    options.setIncludeAttachments(includeAttachments);
                    i += 2;
                } else if (args[i].equals("--decompress")) {
                    boolean decompress = BooleanUtil.parseBoolean(args[i + 1],
                            true);
                    options.setDecompress(decompress);
                    i += 2;
                } else if (args[i].equals("--filter")) {
                    if (options.filter() != null) {
                        throw new IllegalArgumentException(
                                "--filter has already been specified.");
                    }
                    String filter = args[i + 1];
                    options.setFilter(filter);
                    i += 2;
                } else if (args[i].equals("--dataset-only")) {
                    boolean datasetOnly = BooleanUtil.parseBoolean(args[i + 1],
                            false);
                    options.setDatasetOnly(datasetOnly);
                    i += 2;
                } else if (args[i].equals("--output-dir")) {
                    File outputDir = new File(args[i + 1]);
                    if (!outputDir.exists()) {
                        throw new IllegalArgumentException("output directory: "
                                + outputDir.getAbsolutePath()
                                + " does not exist.");
                    }
                    options.setOutputDir(outputDir);
                    i += 2;
                } else if (args[i].equals("--transcodes")) {
                    if (options.hasTranscodes()) {
                        throw new IllegalArgumentException(
                                "--transcodes has already been specified.");
                    }
                    String transcodes = args[i + 1];
                    parseTranscodes(options, transcodes);
                    i += 2;
                } else if (args[i].equals("--overwrite")) {
                    boolean overwrite = BooleanUtil.parseBoolean(args[i + 1],
                            true);
                    options.setOverwrite(overwrite);
                    i += 2;
                } else {
                    String cid = args[i];
                    if (!CiteableIdUtils.isCID(cid)) {
                        throw new IllegalArgumentException(
                                cid + " is not a valid citeable id.");
                    }
                    cids.add(cid);
                    i++;
                }
            }

            if (mfHost == null) {
                throw new IllegalArgumentException(
                        "--mf.host is not specified.");
            }
            if (mfPort <= 0) {
                throw new IllegalArgumentException(
                        "--mf.port is not specified.");
            }
            if (mfTransport == null) {
                throw new IllegalArgumentException(
                        "--mf.transport is not specified.");
            }
            if (mfAuth == null && mfSid == null && mfToken == null) {
                throw new IllegalArgumentException(
                        "No --mf.auth, --mf.token or --mf.sid is specified.");
            }
            if (cids.isEmpty()) {
                throw new IllegalArgumentException(
                        "No object cid is specified.");
            }

            RemoteServer server = new RemoteServer(mfHost, mfPort, useHttp,
                    encrypt);
            ServerClient.Connection cxn = server.open();
            try {
                if (mfToken != null) {
                    cxn.connectWithToken(mfToken);
                } else if (mfAuth != null) {
                    String[] parts = mfAuth.split(",");
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("Invalid mf.auth: "
                                + mfAuth
                                + ". Expects a string in the form of 'domain,user,password'");
                    }
                    cxn.connect(parts[0], parts[1], parts[2]);
                } else {
                    cxn.reconnect(mfSid);
                }
                DownloadUtil.download(cxn, createLogger(), cids, options);
            } finally {
                cxn.closeAndDiscard();
                System.exit(0);
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            showHelp();
            System.exit(1);
        }

    }

    private static Logger createLogger() throws Throwable {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Logger logger = Logger.getLogger("daris-download");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        /*
         * file handler
         */
        FileHandler fileHandler = new FileHandler(
                "%h/.daris/daris-download.%g.log", 5000000, 2);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new Formatter() {

            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                sb.append(new Date(record.getMillis())).append(" ");
                sb.append("[thread: ").append(record.getThreadID())
                        .append("] ");
                sb.append(record.getLevel().getName()).append(" ");
                sb.append(record.getMessage());
                sb.append("\n");
                return sb.toString();
            }
        });
        logger.addHandler(fileHandler);
        /*
         * console handler
         */
        StreamHandler consoleHandler = new StreamHandler(System.out,
                new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return record.getMessage() + "\n";
                    }
                });
        consoleHandler.setLevel(Level.INFO);
        logger.addHandler(consoleHandler);
        return logger;
    }

    private static void parseTranscodes(DownloadOptions options,
            String transcodes) {
        if (transcodes == null || transcodes.trim().isEmpty()) {
            return;
        }
        String[] pairs = transcodes.split(";");
        for (String pair : pairs) {
            String[] types = pair.split(":");
            options.addTranscode(types[0], types[1]);
        }
    }

    private static void showHelp() {
        System.out.println("Usage: daris-download <options> <cid>");
        System.out.println("Options:");
        System.out.println(
                "    --mf.host <host>                    The Mediaflux server host.");
        System.out.println(
                "    --mf.port <port>                    The Mediaflux server port.");
        System.out.println(
                "    --mf.transport <transport>          The Mediaflux server transport, can be http, https or tcp/ip.");
        System.out.println(
                "    --mf.auth <domain,user,password>    The Mediaflux user authentication deatils.");
        System.out.println(
                "    --mf.token <token>                  The Mediaflux secure identity token.");
        System.out.println(
                "    --mf.sid <sid>                      The Mediaflux session id.");
        System.out.println(
                "    --output-dir <dir>                  The output directory. If not specified, defatuls to current working directory.");
        System.out.println(
                "    --overwrite <true|false>            Overwrite files if exists. Defaults to true.");
        System.out.println(
                "    --recursive <true|false>            Download recursively. Defaults to true.");
        System.out.println(
                "    --parts <content|meta|all>          Parts to download. Defaults to content.");
        System.out.println(
                "    --include-attachments <true|false>  Whether or not include object attachments. Defaults to false.");
        System.out.println(
                "    --decompress <true|false>           Decompress/Extract if the object content is an archive. Defaults to true.");
        System.out.println(
                "    --filter <query>                    A filter query to find the objects to download.");
        System.out.println(
                "    --dataset-only <true|false>         Downloads only datasets. Defaults to false.");
        System.out.println(
                "    --transcodes <from:to;from:to>      Apply transcodes before downloading. e.g. --transcodes dicom/series:nifti/series");
        System.out.println(
                "    --help                              Display help information.");
    }

}
