package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCanAccess extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.user.can.access";
    public static final String SERVICE_DESCRIPTION = "Returns whether the user, based on its roles, is allowed to read the specified PSSD object on the local server.";

    private Interface _defn;

    public SvcUserCanAccess() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable identifier of the local object of interest.", 1, 1));
    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return SERVICE_DESCRIPTION;
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public int executeMode() {
        return EXECUTE_DISTRIBUTED_ALL;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String cid = args.value("cid");
        w.add("can", new String[] { "action", "access" }, canAccess(executor(), cid));

    }

    public static boolean canAccess(ServiceExecutor executor, String cid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", cid);
        String assetId = null;
        try {
            assetId = executor.execute("asset.get", dm.root()).value("asset/@id");
        } catch (Throwable e) {
            return false;
        }
        if (assetId == null) {
            return false;
        }
        dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("metadata", "read");
        return executor.execute("asset.acl.have", dm.root()).booleanValue("asset/metadata");
    }
}
