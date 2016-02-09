package nig.mf.plugin.pssd.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

public class SvcStudyDataSetOrdinalReset extends PluginService {

    public static final String SERVICE_NAME = "daris.study.dataset.ordinal.reset";

    private Interface _defn;

    public SvcStudyDataSetOrdinalReset() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the study"));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Re-organize dataset ordinal numbers within the specified study.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2,
            final XmlWriter w)
            throws Throwable {
        String studyCid = args.value("cid");
        XmlDoc.Element ae = executor().execute("asset.get",
                "<args><cid>" + studyCid + "</cid></args>", null, null);
        if (!"study".equals(ae.value("asset/meta/daris:pssd-object/type"))) {
            throw new IllegalArgumentException(
                    "Asset " + studyCid + " is not a daris pssd-study.");
        }
        final List<XmlDoc.Element> cides = executor().execute("asset.query",
                "<args><where>cid in '" + studyCid
                        + "' and model='om.pssd.dataset'</where><action>get-cid</action><size>infinity</size></args>",
                null, null).elements("cid");
        if (cides != null) {
        	Collections.sort(cides,new Comparator<XmlDoc.Element>() {
                @Override
                public int compare(Element cide1, Element cide2) {
                    String cid1 = cide1.value();
                    String cid2 = cide2.value();
                    int ordinal1 = Integer
                            .parseInt(CiteableIdUtil.getLastSection(cid1));
                    int ordinal2 = Integer
                            .parseInt(CiteableIdUtil.getLastSection(cid2));
                    if (ordinal1 > ordinal2) {
                        return 1;
                    } else if (ordinal1 < ordinal2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            new AtomicTransaction(new AtomicOperation() {

                @Override
                public boolean execute(ServiceExecutor exec) throws Throwable {
                    for (int i = 0; i < cides.size(); i++) {
                        XmlDoc.Element cide = cides.get(i);
                        String cid = cide.value();
                        int ordinal = i + 1;
                        if (Integer.parseInt(
                                CiteableIdUtil.getLastSection(cid)) > ordinal) {
                            String assetId = cide.value("@id");
                            String newCid = CiteableIdUtil.getParentId(cid)
                                    + "." + ordinal;
                            exec.execute("asset.cid.set",
                                    "<args><id>" + assetId + "</id><cid>"
                                            + newCid + "</cid></args>",
                                    null, null);
                            w.add("cid", new String[]{"id", assetId, "old-cid", cid}, newCid);
                        }

                    }
                    return false;
                }
            }).execute(executor());

        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
