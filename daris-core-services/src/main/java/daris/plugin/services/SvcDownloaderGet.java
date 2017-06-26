package daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcDownloaderGet extends PluginService {

    public static final String SERVICE_NAME = "daris.downloader.get";

    private Interface _defn;

    public SvcDownloaderGet() {
        _defn = new Interface();
        SvcDownloaderManifestGenerate.addToDefinition(_defn);
        SvcDownloaderPut.addToDefn(_defn);
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
        return "Generate downloader.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        final String manifest = SvcDownloaderManifestGenerate.generateManifest(executor(), args);
        String platform = args.stringValue("platform", "java");
        final String downloaderPath = SvcDownloaderPut.getDownloaderPath(executor(), platform);

        if ("java".equalsIgnoreCase(platform)) {
            final PipedInputStream pis = new PipedInputStream();
            final PipedOutputStream pos = new PipedOutputStream(pis);
            PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
                @Override
                public void run() {
                    try {
                        ArchiveInput ai = ArchiveRegistry.createInput(new File(downloaderPath),
                                new NamedMimeType("application/java-archive"));
                        ArchiveOutput ao = ArchiveRegistry.createOutput(pos, "application/java-archive", 6, null);
                        byte[] manifestBytes = manifest.getBytes();
                        try {
                            ArchiveInput.Entry e;
                            while ((e = ai.next()) != null) {
                                if (e.isDirectory()) {
                                    ao.addDirectory(e.name());
                                } else {
                                    if (!"download.manifest.xml".equals(e.name())) {
                                        ao.add(e.mimeType(), e.name(), e.stream());
                                    }
                                }
                            }
                            ao.add("text/xml", "download.manifest.xml", new SizedInputStream(
                                    new ByteArrayInputStream(manifestBytes), manifestBytes.length));
                        } finally {
                            ao.close();
                            ai.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace(System.out);
                    }
                }
            });
            outputs.output(0).setData(pis, -1, "application/java-archive");
        } else {
            File srcJarFile = new File(SvcDownloaderPut.getDownloaderPath(executor(), "java"));
            final File tmpJarFile = PluginTask.createTemporaryFile(".jar");
            addManifestToJar(srcJarFile, manifest, tmpJarFile);
            final PipedInputStream pis = new PipedInputStream();
            final PipedOutputStream pos = new PipedOutputStream(pis);
            PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
                @Override
                public void run() {
                    try {
                        ArchiveInput ai = ArchiveRegistry.createInput(new File(downloaderPath),
                                new NamedMimeType("application/zip"));
                        ArchiveOutput ao = ArchiveRegistry.createOutput(pos, "application/zip", 6, null);
                        String ename = null;
                        try {
                            ArchiveInput.Entry e;
                            while ((e = ai.next()) != null) {
                                if (e.isDirectory()) {
                                    ao.addDirectory(e.name());
                                } else {
                                    if (!e.name().endsWith("daris-downloader.jar")) {
                                        ao.add(e.mimeType(), e.name(), e.stream());
                                    } else {
                                        ename = e.name();
                                    }
                                }
                            }
                            if (ename == null) {
                                throw new Exception("No daris-downloader.jar found in " + downloaderPath);
                            }
                            try {
                                ao.add("application/java-archive", ename, new SizedInputStream(
                                        PluginTask.deleteOnCloseInputStream(tmpJarFile), tmpJarFile.length()));
                            } finally {
                                if (tmpJarFile.exists()) {
                                    Files.delete(tmpJarFile.toPath());
                                }
                            }
                        } finally {
                            ao.close();
                            ai.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace(System.out);
                    }
                }
            });
            outputs.output(0).setData(pis, -1, "application/zip");
        }
    }

    static void addManifestToJar(File srcJarFile, String manifest, File dstJarFile) throws Throwable {
        ArchiveInput ai = ArchiveRegistry.createInput(srcJarFile, new NamedMimeType("application/java-archive"));
        ArchiveOutput ao = ArchiveRegistry.createOutput(dstJarFile, "application/java-archive", 6, null);
        byte[] manifestBytes = manifest.getBytes();
        try {
            ArchiveInput.Entry e;
            while ((e = ai.next()) != null) {
                if (e.isDirectory()) {
                    ao.addDirectory(e.name());
                } else {
                    if (!"download.manifest.xml".equals(e.name())) {
                        ao.add(e.mimeType(), e.name(), e.stream());
                    }
                }
            }
            ao.add("text/xml", "download.manifest.xml",
                    new SizedInputStream(new ByteArrayInputStream(manifestBytes), manifestBytes.length));
        } finally {
            ao.close();
            ai.close();
        }

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

    public int minNumberOfOutputs() {
        return 1;
    }

}
