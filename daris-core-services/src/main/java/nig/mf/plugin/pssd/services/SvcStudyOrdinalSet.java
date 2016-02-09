package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.pssd.plugin.util.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcStudyOrdinalSet extends PluginService {

    public static final String SERVICE_NAME = "daris.study.ordinal.set";

    public static final String SERVICE_DESCRIPTION = "Set the ordinal number part of the study cid. It will also change the citeable ids of the datasets contained by the study.";

    private Interface _defn;

    public SvcStudyOrdinalSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The current citeable id of the study.", 1, 1));
        _defn.add(new Interface.Element("ordinal", IntegerType.POSITIVE_ONE,
                "The new ordinal number for the study.", 1, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String studyCid = args.value("cid");
        int ordinal = args.intValue("ordinal");
        setStudyOrdinal(executor(), studyCid, ordinal, w);
    }

    private static void setStudyOrdinal(ServiceExecutor executor,
            final String studyCid, final int ordinal, final XmlWriter w)
                    throws Throwable {
        final XmlDoc.Element ae = executor
                .execute("asset.get",
                        "<args><cid>" + studyCid + "</cid></args>", null, null)
                .element("asset");
        final String newStudyCid = CiteableIdUtil.getParentId(studyCid) + "."
                + ordinal;
        final List<XmlDoc.Element> des = executor
                .execute("asset.query",
                        "<args><action>get-cid</action><size>infinity</size><where>cid in '"
                                + studyCid + "'</where></args>",
                        null, null)
                .elements("cid");
        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                String studyAssetId = ae.value("@id");
                String studyAssetName = ae.value("name");
                String studyAssetNamespace = ae.value("namespace");
                setCid(executor, studyAssetId, newStudyCid);
                if (des != null) {
                    for (XmlDoc.Element de : des) {
                        String datasetAssetId = de.value("@id");
                        String datasetCid = de.value();
                        String newDatasetCid = newStudyCid + "."
                                + CiteableIdUtil.getLastSection(datasetCid);
                        setCid(executor, datasetAssetId, newDatasetCid);
                    }
                }
                // set subject asset name
                if (studyAssetName != null && studyAssetName.endsWith(studyCid)) {
                    setAssetName(executor, studyAssetId,
                            studyAssetName.replace(studyCid, newStudyCid));
                }
                // rename subject namespace
                if (studyAssetNamespace != null
                        && studyAssetNamespace.endsWith("/" + studyCid)) {
                    renameAssetNamespace(executor, studyAssetNamespace, newStudyCid);
                }
                w.add("cid", new String[] { "id", studyAssetId, "old-cid",
                        studyCid }, newStudyCid);
                return false;
            }
        }).execute(executor);
    }

    private static void setCid(ServiceExecutor executor, String assetId,
            String cid) throws Throwable {
        if (executor.execute("asset.exists",
                "<args><cid>" + cid + "</cid></args>", null, null)
                .booleanValue("exists")) {
            throw new Exception("Asset " + cid + " already exists.");
        }
        if (!executor
                .execute("citeable.id.exists",
                        "<args><cid>" + cid + "</cid></args>", null, null)
                .booleanValue("exists")) {
            executor.execute("citeable.id.import",
                    "<args><root-depth>0</root-depth><cid>" + cid
                            + "</cid></args>",
                    null, null);
        }
        executor.execute("asset.cid.set",
                "<args><id>" + assetId + "</id><cid>" + cid + "</cid></args>",
                null, null);
    }

    private static void setAssetName(ServiceExecutor executor, String assetId,
            String assetName) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("name", assetName);
        executor.execute("asset.set", dm.root());
    }

    private static void renameAssetNamespace(ServiceExecutor executor,
            String namespace, String newCid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", namespace);
        dm.add("name", newCid);
        executor.execute("asset.namespace.rename", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
