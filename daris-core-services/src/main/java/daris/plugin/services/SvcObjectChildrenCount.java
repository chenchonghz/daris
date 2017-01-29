package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectChildrenCount extends PluginService {

    public static final String SERVICE_NAME = "daris.object.children.count";

    private Interface _defn;

    public SvcObjectChildrenCount() {

        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the parent object.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the parent object.", 0, 1));

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
        return "Count the number of children of the specified object. If no object is specified, count the number of projects.";
    }

    @Override
    public void execute(XmlDoc.Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        String id = args.value("id");
        if (cid == null && id != null) {
            cid = ServiceUtils.getObjectIdentifiers(executor(), id, null).getValue();
        }

        long count = countChildren(executor(), cid);
        w.add("nbc", new String[] { "cid", cid }, count);
    }

    public static int countChildren(ServiceExecutor executor, String cid) throws Throwable {
        StringBuilder sb = new StringBuilder();
        if (cid == null) {
            sb.append("model='om.pssd.project'");
        } else {
            sb.append("cid in '").append(cid).append("'");
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", sb.toString());
        dm.add("action", "count");
        return executor.execute("asset.query", dm.root()).intValue("value");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
