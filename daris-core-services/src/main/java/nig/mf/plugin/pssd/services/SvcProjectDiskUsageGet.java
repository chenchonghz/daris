package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcProjectDiskUsageGet extends PluginService {

    public static final String SERVICE_NAME = "daris.project.disk-usage.get";

    private Interface _defn;

    public SvcProjectDiskUsageGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the project. if not specified, disk usages for all the projects will be listed.",
                0, Integer.MAX_VALUE));
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
        return "Sums up the disk usage of DaRIS project.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        Collection<String> cids = args.values("cid");
        if (cids == null || cids.isEmpty()) {
            cids = executor().execute("asset.query",
                    "<args><where>cid has value and model='om.pssd.project'</where><action>get-cid</action><size>infinity</size></args>",
                    null, null).values("cid");
        }
        long total = 0;
        if (cids != null && !cids.isEmpty()) {
            for (String cid : cids) {
                long du = calcProjectDiskUsage(executor(), cid);
                w.push("project", new String[] { "cid", cid });
                w.add("disk-usage",
                        new String[] { "mb", String.format("%.2f", toMB(du)),
                                "gb", String.format("%.2f", toGB(du)), "tb",
                                String.format("%.2f", toTB(du)) },
                        du);
                w.pop();
                total += du;
            }
        }

        w.add("total",
                new String[] { "mb", String.format("%.2f", toMB(total)), "gb",
                        String.format("%.2f", toGB(total)), "tb",
                        String.format("%.2f", toTB(total)) },
                total);
    }

    public static long calcProjectDiskUsage(ServiceExecutor executor,
            String cid) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><cid>" + cid + "</cid></args>", null, null)
                .element("asset");
        String ns = ae.value("namespace");
        StringBuilder sb = new StringBuilder();
        if (ns.endsWith(cid)) {
            sb.append("namespace>='").append(ns).append("'");
        } else {
            sb.append("(cid='").append(cid).append("' or cid starts with '")
                    .append(cid).append("')");
            String query = sb.toString();
            sb.append(" or (related to{attached-to} ").append(query)
                    .append(")");
        }
        return executor
                .execute("asset.query",
                        "<args><action>sum</action><xpath>content/size</xpath><where>"
                                + sb.toString() + "</where></args>",
                        null, null)
                .longValue("value", 0);
    }

    public static double toMB(long b) {
        return (double) b / 1000000L;
    }

    public static double toGB(long b) {
        return (double) b / 1000000000L;
    }

    public static double toTB(long b) {
        return (double) b / 1000000000000L;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
