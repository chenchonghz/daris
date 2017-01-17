package daris.plugin.services;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectDetach extends PluginService {

    public static final String SERVICE_NAME = "daris.object.detach";

    private Interface _defn;

    public SvcObjectDetach() {
        _defn = new Interface();

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object to detach from.",
                0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the object to detach from.", 0, 1));

        _defn.add(new Interface.Element("aid", AssetType.DEFAULT, "The asset id of the attachment to be removed.", 0,
                Integer.MAX_VALUE));

        _defn.add(new Interface.Element("detach-all", BooleanType.DEFAULT,
                "Detach all attachments. Defaults to false. If set to true, aid arguments will be ignored.", 0, 1));
    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return "Removes one or more attachments from the specified object.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        SimpleEntry<String, String> ids = ServiceUtils.getObjectIdentifiers(executor(), args);
        String id = ids.getKey();

        boolean detachAll = args.booleanValue("detach-all", false);
        Collection<String> attachmentAssetIds = null;
        if (detachAll) {
            attachmentAssetIds = ServiceUtils.getAssetMeta(executor(), id, null)
                    .values("related[@type='attachment']/to");
        } else {
            attachmentAssetIds = args.values("aid");
        }
        if (attachmentAssetIds != null && !attachmentAssetIds.isEmpty()) {
            detach(executor(), id, attachmentAssetIds);
        }
    }

    public static void detachAll(ServiceExecutor executor, String id) throws Throwable {
        Collection<String> attachmentAssetIds = ServiceUtils.getAssetMeta(executor, id, null)
                .values("related[@type='attachment']/to");
        detach(executor, id, attachmentAssetIds);
    }

    public static void detach(ServiceExecutor executor, String id, Collection<String> attachmentAssetIds)
            throws Throwable {
        if (attachmentAssetIds != null) {
            for (String attachmentAssetId : attachmentAssetIds) {
                executor.execute("asset.relationship.remove", "<args><id>" + id
                        + "</id><to relationship=\"attachment\">" + attachmentAssetId + "</to></args>", null, null);
                executor.execute("asset.hard.destroy", "<args><id>" + attachmentAssetId + "</id></args>", null, null);
            }
        }
    }

}
