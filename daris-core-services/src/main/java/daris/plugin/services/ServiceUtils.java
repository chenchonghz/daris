package daris.plugin.services;

import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class ServiceUtils {

    static SimpleEntry<String, String> getObjectIdentifiers(ServiceExecutor executor, String id, String cid)
            throws Throwable {
        if (id == null && cid == null) {
            throw new IllegalArgumentException("Missing argument cid or id.");
        }
        if (id != null && cid != null) {
            throw new IllegalArgumentException("Expect argument id or cid, but not both.");
        }
        if (id != null) {
            cid = executor.execute("asset.identifier.get", "<args><id>" + id + "</id></args>", null, null)
                    .value("id/@cid");
            if (cid == null) {
                // NOTE: every daris/pssd object should have a cid (except for
                // attachments...)
                throw new Exception("Asset " + id + " does not have cid. Not a valid daris pssd-object.");
            }
        }
        if (cid != null) {
            id = executor.execute("asset.identifier.get", "<args><cid>" + cid + "</cid></args>", null, null)
                    .value("id");
        }
        return new SimpleEntry<String, String>(id, cid);
    }

    static SimpleEntry<String, String> getObjectIdentifiers(ServiceExecutor executor, XmlDoc.Element args)
            throws Throwable {
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new IllegalArgumentException("Missing argument cid or id.");
        }
        if (id != null && cid != null) {
            throw new IllegalArgumentException("Expects argument cid or id, but not both.");
        }
        return getObjectIdentifiers(executor, id, cid);
    }

    static XmlDoc.Element getAssetMeta(ServiceExecutor executor, String id, String cid) throws Throwable {
        if (id != null && cid != null) {
            throw new IllegalArgumentException("Expects cid or id, but not both.");
        }
        if (id != null) {
            return executor.execute("asset.get", "<args><id>" + id + "</id></args>", null, null).element("asset");
        } else {
            return executor.execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null).element("asset");
        }
    }

    static boolean assetExists(ServiceExecutor executor, String id) throws Throwable {
        return executor.execute("asset.exists", "<args><id>" + id + "</id></args>", null, null).booleanValue("exists");
    }

}
