package daris.plugin.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import nig.mf.MimeTypes;

public class SvcObjectAttachmentGet extends PluginService {

    public static final String SERVICE_NAME = "daris.object.attachment.get";

    private Interface _defn;

    public static final int BUFFER_SIZE = 2048;

    public SvcObjectAttachmentGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object to attach to.", 0,
                1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the object to attach to.", 0, 1));
        _defn.add(new Interface.Element("aid", AssetType.DEFAULT,
                "The identity of the attachment asset. if not specified, all the attachments to the object will be packaged into a zip archive.",
                0, Integer.MAX_VALUE));
    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return "Get the attachments of the object.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public int minNumberOfOutputs() {
        return 1;
    }

    public int maxNumberOfOutputs() {
        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs outputs, XmlWriter w) throws Throwable {

        SimpleEntry<String, String> ids = ServiceUtils.getObjectIdentifiers(executor(), args);
        String id = ids.getKey();
        String cid = ids.getValue();
        Collection<String> allAids = ServiceUtils.getAssetMeta(executor(), id, null)
                .values("related[@type='" + SvcObjectAttach.RELATIONSHIP_TYPE + "']/to");
        if (allAids == null || allAids.isEmpty()) {
            throw new Exception("Object " + cid + " does not have attachment.");
        }
        Collection<String> aids = args.values("aid");
        if (aids != null) {
            for (String aid : aids) {
                if (!allAids.contains(aid)) {
                    throw new IllegalArgumentException(
                            "Object " + cid + " does not have attachment(asset_id=" + aid + ").");
                }
            }
        }
        if (aids != null && !aids.isEmpty()) {
            getAttachments(executor(), aids, aids.size() > 1, outputs);
        } else {
            getAttachments(executor(), allAids, true, outputs);
        }

    }

    public static void getAttachments(ServiceExecutor executor, Collection<String> aids, boolean zip,
            PluginService.Outputs outputs) throws Throwable {

        if (aids.size() == 1 && !zip) {
            executor.execute("asset.content.get", "<args><id>" + aids.iterator().next() + "</id></args>", null,
                    outputs);
        } else {
            File of = PluginTask.createTemporaryFile();
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(of)));
            try {
                for (String aid : aids) {
                    PluginService.Outputs os = new PluginService.Outputs(1);
                    XmlDoc.Element r = executor.execute("asset.get", "<args><id>" + aid + "</id></args>", null, os);
                    String name = r.value("asset/name");
                    ZipEntry entry = new ZipEntry(name);
                    zos.putNextEntry(entry);
                    BufferedInputStream is = new BufferedInputStream(os.output(0).stream());
                    IOUtils.copy(is, zos, true, false);
                }
            } finally {
                zos.close();
            }
            outputs.output(0).setData(PluginService.deleteOnCloseInputStream(of), of.length(), MimeTypes.ZIP);
        }

    }

}
