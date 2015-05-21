package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcCollectionMemberCount extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.collection.member.connt";

    private Interface _defn;

    public SvcCollectionMemberCount() {
        _defn = new Interface();
        _defn.add(new Interface.Element("pid", CiteableIdType.DEFAULT,
                "The identity of the (parent) pssd object. If not specified, then counts the root level objects.", 0, 1));
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
        return "Returns the total number of objects of the specific collection.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String pid = args.value("pid");
        w.add("count", countMembers(pid));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static int countMembers(ServiceExecutor executor, String pid) throws Throwable {
        String where = pid == null ? "model='om.pssd.project'" : ("cid in'" + pid + "'");
        return executor.execute("asset.query", "<args><where>" + where + "</where><action>count</action></args>", null,
                null).intValue("value");
    }

    public static int countMembers(String pid) throws Throwable {
        return countMembers(PluginThread.serviceExecutor(), pid);
    }

}
