package daris.client.dicom;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pixelmed.dicom.DicomFileUtilities;

import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.mf.client.archive.Archive;
import arc.streams.StreamCopy.AbortCheck;
import arc.xml.XmlStringWriter;

public class DicomIngest {

    public static final int COMPRESSION_LEVEL = 6;
    public static final String MIME_TYPE_AAR = "application/arc-archive";

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
        String cid = null;
        boolean anonymize = false;
        String metaSetService = null;
        List<File> dcmFiles = new ArrayList<File>();
        try {
            for (int i = 0; i < args.length;) {
                if (args[i].equals("--help") || args[i].equals("-h")) {
                    showHelp();
                    System.exit(0);
                } else if (args[i].equals("--mf.host")) {
                    if (mfHost != null) {
                        throw new Exception(
                                "--mf.host has already been specified.");
                    }
                    mfHost = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.port")) {
                    if (mfPort > 0) {
                        throw new Exception(
                                "--mf.port has already been specified.");
                    }
                    try {
                        mfPort = Integer.parseInt(args[i + 1]);
                    } catch (Throwable e) {
                        throw new Exception("Invalid mf.port: " + args[i + 1],
                                e);
                    }
                    if (mfPort <= 0 || mfPort > 65535) {
                        throw new Exception("Invalid mf.port: " + args[i + 1]);
                    }
                    i += 2;
                } else if (args[i].equals("--mf.transport")) {
                    if (mfTransport != null) {
                        throw new Exception(
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
                        throw new Exception(
                                "Invalid mf.transport: " + mfTransport
                                        + ". Expects http, https or tcp/ip.");
                    }
                } else if (args[i].equals("--mf.auth")) {
                    if (mfAuth != null) {
                        throw new Exception(
                                "--mf.auth has already been specified.");
                    }
                    if (mfSid != null || mfToken != null) {
                        throw new Exception(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfAuth = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.token")) {
                    if (mfToken != null) {
                        throw new Exception(
                                "--mf.token has already been specified.");
                    }
                    if (mfSid != null || mfAuth != null) {
                        throw new Exception(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfToken = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.sid")) {
                    if (mfSid != null) {
                        throw new Exception(
                                "--mf.sid has already been specified.");
                    }
                    if (mfToken != null || mfAuth != null) {
                        throw new Exception(
                                "You can only specify one of mf.auth, mf.token or mf.sid. Found more than one.");
                    }
                    mfSid = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--anonymize")) {
                    anonymize = true;
                    i++;
                } else if (args[i].equals("--meta-set-service")) {
                    if (metaSetService != null) {
                        throw new Exception(
                                "--meta-set-service has already been specified.");
                    }
                    metaSetService = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--cid")) {
                    if (cid != null) {
                        throw new Exception(
                                "--cid has already been specified.");
                    }
                    cid = args[i + 1];
                    i += 2;
                } else {
                    File f = new File(args[i]);
                    if (!f.exists()) {
                        throw new FileNotFoundException(
                                "File " + args[i] + " is not found.");
                    }
                    if (f.isFile()) {
                        if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
                            dcmFiles.add(f);
                        } else {
                            throw new Exception("File " + args[i]
                                    + " is not a valid DICOM file.");
                        }
                    } else if (f.isDirectory()) {
                        addDicomDirectory(dcmFiles, f);
                    }
                    i++;
                }
            }
            if (cid == null) {
                throw new Exception("--cid is not specified.");
            }
            if (dcmFiles.isEmpty()) {
                throw new Exception("No dicom file/directory is specified.");
            }
            if (mfHost == null) {
                throw new Exception("--mf.host is not specified.");
            }
            if (mfPort <= 0) {
                throw new Exception("--mf.port is not specified.");
            }
            if (mfTransport == null) {
                throw new Exception("--mf.transport is not specified.");
            }
            if (mfAuth == null && mfSid == null && mfToken == null) {
                throw new Exception(
                        "You need to specify one of mf.auth, mf.token or mf.sid. Found none.");
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
                        throw new Exception("Invalid mf.auth: " + mfAuth
                                + ". Expects a string in the form of 'domain,user,password'");
                    }
                    cxn.connect(parts[0], parts[1], parts[2]);
                } else {
                    cxn.reconnect(mfSid);
                }
                ingestDicomFiles(cxn, cid, anonymize, metaSetService, dcmFiles);
            } finally {
                cxn.closeAndDiscard();
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            showHelp();
        }
    }

    private static void addDicomDirectory(final List<File> dcmFiles, File dir) {
        dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    addDicomDirectory(dcmFiles, f);
                } else if (f.isFile()) {
                    if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
                        dcmFiles.add(f);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static void showHelp() {
        System.out.println(
                "Usage: dicom-ingest [--help] --mf.host <host> --mf.port <port> --mf.transport <transport> [--mf.sid <sid>|--mf.token <token>|--mf.auth <domain,user,password>]  [--anonymize] [--meta-set-service <service>] --cid <cid> <dicom-files/dicom-directories>");
        System.out.println("Description:");
        System.out.println(
                "    --mf.host <host>                 The Mediaflux server host.");
        System.out.println(
                "    --mf.port <port>                 The Mediaflux server port.");
        System.out.println(
                "    --mf.transport <transport>       The Mediaflux server transport, can be http, https or tcp/ip.");
        System.out.println(
                "    --mf.auth <domain,user,password> The Mediaflux user authentication deatils.");
        System.out.println(
                "    --mf.token <token>               The Mediaflux secure identity token.");
        System.out.println(
                "    --mf.sid <sid>                   The Mediaflux session id.");
        System.out.println(
                "    --cid <cid>                      The DaRIS object cid. It can be the cid of the destination project/subject/study.");
        System.out.println(
                "    --anonymize                      Anonymize PatientName element(0010,0010) if set.");
        System.out.println(
                "    --meta-set-service <service>     The service to map dicom header elements to domain specific meta-data. This argument is optional.");
        System.out.println(
                "    --help                           Display help information.");
    }

    private static void ingestDicomFiles(ServerClient.Connection cxn,
            String cid, boolean anonymize, String metaSetService,
            final List<File> dcmFiles) throws Throwable {

        // I think you can't supply the id and prefix for the default PSS
        // engine, only our nig.dicom engine. So you can probably only upload
        // without specifying the ID for PSS engine. So these arguments
        // would probably be ignored.
        XmlStringWriter w = new XmlStringWriter();
        w.add("engine", "nig.dicom");
        w.add("anonymize", anonymize);
        w.add("arg", new String[] { "name", "nig.dicom.id.ignore-non-digits" },
                "true");
        w.add("arg", new String[] { "name", "nig.dicom.subject.create" },
                "true");
        w.add("arg", new String[] { "name", "nig.dicom.id.citable" }, cid);
        // The CID prefix is not used
        if (metaSetService != null) {
            w.add("arg",
                    new String[] { "name",
                            "nig.dicom.subject.meta.set-service" },
                    metaSetService);
        }
        w.add("arg",
                new String[] { "name", "nig.dicom.write.mf-dicom-patient" },
                true);
        w.add("wait", true);
        w.add("type", MIME_TYPE_AAR);
        Archive.declareSupportForAllTypes();
        Collections.sort(dcmFiles);
        ServerClient.Input sci = new ServerClient.GeneratedInput(MIME_TYPE_AAR,
                "aar", dcmFiles.get(0).getParentFile().getAbsolutePath(), -1,
                null) {

            @Override
            protected void copyTo(OutputStream os, AbortCheck ac)
                    throws Throwable {
                ArchiveOutput ao = ArchiveRegistry.createOutput(os,
                        MIME_TYPE_AAR, 6, null);
                try {
                    for (int i = 0; i < dcmFiles.size(); i++) {
                        System.out.println("Sending: "
                                + dcmFiles.get(i).getAbsolutePath());
                        ao.add("application/dicom",
                                String.format("%08d.dcm", i + 1),
                                dcmFiles.get(i));
                    }
                } finally {
                    ao.close();
                }

            }
        };
        cxn.execute("dicom.ingest", w.document(), sci);
    }
}
