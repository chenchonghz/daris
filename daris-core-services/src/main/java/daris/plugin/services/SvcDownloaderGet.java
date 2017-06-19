package daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
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
        final String downloaderJarPath = SvcDownloaderPut.getDownloaderJarPath(executor());

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
            @Override
            public void run() {
                try {
                    ArchiveInput ai = ArchiveRegistry.createInput(new File(downloaderJarPath),
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
                        ao.add("text/xml", "download.manifest.xml",
                                new SizedInputStream(new ByteArrayInputStream(manifestBytes), manifestBytes.length));
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
