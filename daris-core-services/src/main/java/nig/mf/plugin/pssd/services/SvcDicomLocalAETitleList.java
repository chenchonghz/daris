package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcDicomLocalAETitleList extends PluginService {

    public static final String SERVICE_NAME = "daris.dicom.local.ae.title.list";
    public static final String SERVICE_DESCRIPTION = "List all the local dicom ae titles. Including the ae titles for onsending (the users in dicom authentication domain whose names end with 'ONSEND').";

    private Interface _defn;

    public SvcDicomLocalAETitleList() {
        _defn = new Interface();
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        Collection<String> svcTitles = executor().execute("network.describe",
                "<args><type>dicom</type></args>", null, null).values(
                "service/arg[@name='dicom.title']");
        Collection<String> dcmUsers = executor().execute("user.list",
                "<args><domain>dicom</domain></args>", null, null)
                .values("user");
        Set<String> titles = new TreeSet<String>();
        if (svcTitles != null) {
            titles.addAll(svcTitles);
        }
        if (dcmUsers != null) {
            for (String user : dcmUsers) {
                if (user.endsWith("ONSEND") || user.endsWith("onsend")) {
                    titles.add(user);
                }
            }
        }
        if (!titles.isEmpty()) {
            for (String title : titles) {
                w.add("title", title);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
