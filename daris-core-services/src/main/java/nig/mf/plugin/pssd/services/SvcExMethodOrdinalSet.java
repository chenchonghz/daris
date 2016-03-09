package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

public class SvcExMethodOrdinalSet extends PluginService {

    public static final String SERVICE_NAME = "daris.ex-method.ordinal.set";

    private Interface _defn;

    public SvcExMethodOrdinalSet() {
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
        return "Set the ordinal number part of the ex-method cid. It will also change the citeable ids of the descendants of the ex-method. Use with caution.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String exMethodCid = args.value("cid");
        int ordinal = args.intValue("ordinal");
        setExMethodOrdinal(executor(), exMethodCid, ordinal, w);
    }

    private static void setExMethodOrdinal(ServiceExecutor executor,
            final String exMethodCid, final int ordinal, final XmlWriter w)
                    throws Throwable {
        final XmlDoc.Element ae = executor.execute("asset.get",
                "<args><cid>" + exMethodCid + "</cid></args>", null, null)
                .element("asset");
        final String newExMethodCid = CiteableIdUtil.getParentId(exMethodCid)
                + "." + ordinal;
        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                String exMethodAssetId = ae.value("@id");
                String exMethodAssetName = ae.value("name");
                Collection<String> studyCids = executor.execute("asset.query",
                        "<args><action>get-cid</action><size>infinity</size><where>cid in '"
                                + exMethodCid + "'</where></args>",
                        null, null).values("cid");
                if (studyCids != null) {
                    for (String studyCid : studyCids) {
                        Collection<String> datasetCids = executor
                                .execute("asset.query",
                                        "<args><action>get-cid</action><size>infinity</size><where>cid in '"
                                                + studyCid + "'</where></args>",
                                        null, null)
                                .values("cid");
                        if (datasetCids != null) {
                            for (String datasetCid : datasetCids) {
                                updateDataset(executor, datasetCid,
                                        newExMethodCid);
                            }
                        }
                        // update study cid, name, meta ...
                        updateStudy(executor, studyCid, newExMethodCid);
                    }
                }
                // set ex-method cid
                setCid(executor, exMethodAssetId, newExMethodCid);
                // set ex-method asset name
                if (exMethodAssetName != null
                        && exMethodAssetName.endsWith(exMethodCid)) {
                    setAssetName(executor, exMethodAssetId, exMethodAssetName
                            .replace(exMethodCid, newExMethodCid));
                }
                w.add("cid", new String[] { "id", exMethodAssetId, "old-cid",
                        exMethodCid }, newExMethodCid);
                return false;
            }
        }).execute(executor);
    }

    private static void updateStudy(ServiceExecutor executor, String studyCid,
            String newExMethodCid) throws Throwable {
        XmlDoc.Element studyAE = executor
                .execute("asset.get",
                        "<args><cid>" + studyCid + "</cid></args>", null, null)
                .element("asset");
        String studyAssetId = studyAE.value("@id");
        String studyAssetName = studyAE.value("name");
        String studyAssetNamespace = studyAE.value("namespace");
        String newStudyCid = newExMethodCid + "."
                + CiteableIdUtil.getLastSection(studyCid);
        // set study cid
        setCid(executor, studyAssetId, newStudyCid);

        // set study meta
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", studyAssetId);
        if (studyAssetName != null && studyAssetName.endsWith(studyCid)) {
            dm.add("name", studyAssetName.replace(studyCid, newStudyCid));
        }
        dm.push("meta");
        dm.push("daris:pssd-study");
        dm.add("type", studyAE.value("meta/daris:pssd-study/type"));
        dm.add("method",
                new String[] { "step",
                        studyAE.value("meta/daris:pssd-study/method/@step") },
                newExMethodCid);
        dm.pop();
        dm.pop();
        executor.execute("asset.set", dm.root());

        // rename study namespace
        if (studyAssetNamespace != null
                && studyAssetNamespace.endsWith("/" + studyCid)) {
            renameAssetNamespace(executor, studyAssetNamespace, newStudyCid);
        }
    }

    private static void updateDataset(ServiceExecutor executor,
            String datasetCid, String newExMethodCid) throws Throwable {
        XmlDoc.Element datasetAE = executor.execute("asset.get",
                "<args><cid>" + datasetCid + "</cid></args>", null, null)
                .element("asset");
        String datasetAssetId = datasetAE.value("@id");
        String studyCid = CiteableIdUtil.getParentId(datasetCid);
        String newStudyCid = newExMethodCid + "."
                + CiteableIdUtil.getLastSection(studyCid);
        String newDatasetCid = newStudyCid + "."
                + CiteableIdUtil.getLastSection(datasetCid);

        // set dataset cid
        setCid(executor, datasetAssetId, newDatasetCid);

        // set dataset meta
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", datasetAssetId);
        dm.push("meta");
        if (datasetAE.elementExists("meta/daris:pssd-acquisition")) {
            dm.push("daris:pssd-acquisition");
            dm.add(datasetAE.element("meta/daris:pssd-acquisition/subject"),
                    true);
            dm.add("method",
                    new String[] { "step",
                            datasetAE
                                    .value("meta/daris:pssd-acquisition/method/@step") },
                    newExMethodCid);
            dm.pop();
        }
        if (datasetAE.elementExists("meta/daris:pssd-derivation")) {
            dm.push("daris:pssd-derivation");
            dm.add("method",
                    new String[] { "step",
                            datasetAE
                                    .value("meta/daris:pssd-derivation/method/@step") },
                    newExMethodCid);
            dm.pop();
        }
        dm.pop();
        executor.execute("asset.set", dm.root());
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
                    "<args><root-depth>1</root-depth><cid>" + cid
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
