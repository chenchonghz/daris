package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyMove extends PluginService {

    public static final String SERVICE_NAME = "daris.study.move";
    public static final String SERVICE_DESCRIPTION = "Move a study (including its datasets) to another subject/exmethod within the same project. Note: the destination subject/ex-method need to have the same method.";

    private Interface _defn;

    public SvcStudyMove() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the study to be moved.", 1, 1));
        _defn.add(new Interface.Element(
                "pid",
                CiteableIdType.DEFAULT,
                "The citeable id of the destination Subject/ExMethod that will be the new parent. If a subject, the Study is moved to its matching ExMethod; If an ExMethod, the Study is moved to it.",
                1, 1));
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
        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {

        String srcStudyCid = args.value("cid");
        if (!executor().execute("asset.exists",
                "<args><cid>" + srcStudyCid + "</cid></args>", null, null)
                .booleanValue("exists", false)) {
            throw new Exception("Asset " + srcStudyCid + " does not exist.");
        }
        if (!isStudy(executor(), srcStudyCid)) {
            throw new Exception("Asset " + srcStudyCid + " is not a study.");
        }
        if (!CiteableIdUtil.isStudyId(srcStudyCid)) {
            throw new Exception("cid " + srcStudyCid
                    + " is not a valid study cid.");
        }

        String pid = args.value("pid");
        if (!CiteableIdUtil.isSubjectId(pid)
                && !CiteableIdUtil.isExMethodId(pid)) {
            throw new Exception("pid " + pid
                    + " is not a valid subject cid or ex-method cid.");
        }
        String projectCid = CiteableIdUtil.getProjectId(srcStudyCid);
        if (!projectCid.equals(CiteableIdUtil.getProjectId(pid))) {
            throw new Exception(
                    "The study cid "
                            + srcStudyCid
                            + " and destination parent cid: "
                            + pid
                            + " does not match. You can only move within the same project.");
        }

        String srcExMethodCid = CiteableIdUtil.getParentId(srcStudyCid);

        String dstSubjectCid = null;
        String dstExMethodCid = null;
        if (CiteableIdUtil.isSubjectId(pid)) {
            dstSubjectCid = pid;
            dstExMethodCid = findDstExMethod(executor(), srcExMethodCid,
                    dstSubjectCid);
        } else {
            dstExMethodCid = pid;
            dstSubjectCid = CiteableIdUtil.getParentId(dstExMethodCid);
            if (!methodsMatch(executor(), srcExMethodCid, dstExMethodCid)) {
                throw new Exception("The methods do not match.");
            }
        }

        if (srcExMethodCid.equals(dstExMethodCid)) {
            throw new Exception("Cannot move within the same ex-method: "
                    + dstExMethodCid);
        }

        moveStudy(executor(), srcStudyCid, dstExMethodCid);
    }

    private static String findDstExMethod(ServiceExecutor executor,
            String srcExMethodCid, String dstSubjectCid) throws Throwable {
        String srcMethodId = executor.execute("asset.get",
                "<args><cid>" + srcExMethodCid + "</cid></args>", null, null)
                .value("asset/meta/daris:pssd-ex-method/method/id");
        if (srcMethodId == null) {
            throw new Exception(
                    "Could not find daris:pssd-ex-method/method/id in asset "
                            + srcExMethodCid);
        }
        String dstExMethodCid = executor.execute(
                "asset.query",
                "<args><action>get-cid</action><size>1</size><where>cid in '"
                        + dstSubjectCid
                        + "' and xpath(daris:pssd-ex-method/method/id)='"
                        + srcMethodId + "'</where></args>", null, null).value(
                "cid");
        if (dstExMethodCid == null) {
            throw new Exception("Could not find the ex-method in subject "
                    + dstSubjectCid + ".");
        }
        return dstExMethodCid;
    }

    private static boolean isStudy(ServiceExecutor executor, String studyCid)
            throws Throwable {
        String model = executor.execute("asset.get",
                "<args><cid>" + studyCid + "</cid></args>", null, null).value(
                "asset/model");
        if (model != null && model.equals("om.pssd.study")) {
            return true;
        }
        return false;
    }

    private static boolean methodsMatch(ServiceExecutor executor,
            String srcExMethodCid, String dstExMethodCid) throws Throwable {
        String srcMethodId = executor.execute("asset.get",
                "<args><cid>" + srcExMethodCid + "</cid></args>", null, null)
                .value("asset/meta/daris:pssd-ex-method/method/id");
        String dstMethodId = executor.execute("asset.get",
                "<args><cid>" + dstExMethodCid + "</cid></args>", null, null)
                .value("asset/meta/daris:pssd-ex-method/method/id");
        return ObjectUtil.equals(srcMethodId, dstMethodId);
    }

    private static void moveStudy(ServiceExecutor executor,
            final String srcStudyCid, final String dstExMethodCid)
            throws Throwable {
        XmlDoc.Element srcStudyAE = executor.execute("asset.get",
                "<args><cid>" + srcStudyCid + "</cid></args>", null, null)
                .element("asset");
        final String srcStudyAssetId = srcStudyAE.value("@id");
        final String srcStudyAssetNS = srcStudyAE.value("namespace");
        final String dstExMethodAssetNS = executor.execute("asset.get",
                "<args><cid>" + dstExMethodCid + "</cid></args>", null, null)
                .value("asset/namespace");
        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                /*
                 * create study cid
                 */
                long dstStudyNumber = Long.parseLong(CiteableIdUtil
                        .getLastSection(srcStudyCid));
                String dstStudyCid = nig.mf.pssd.plugin.util.CiteableIdUtil
                        .generateCiteableID(executor, dstExMethodCid,
                                "infinity", dstStudyNumber, true);
                /*
                 * set study cid
                 */
                executor.execute("asset.cid.set", "<args><id>"
                        + srcStudyAssetId + "</id><cid>" + dstStudyCid
                        + "</cid></args>", null, null);
                /*
                 * update study name & metadata (daris:pssd-study)
                 */
                updateStudy(executor, srcStudyAssetId, dstStudyCid);
                Collection<XmlDoc.Element> datasets = executor.execute(
                        "asset.query",
                        "<args><size>infinity</size><action>get-cid</action><where>cid in '"
                                + srcStudyCid + "'</where></args>", null, null)
                        .elements("cid");
                if (datasets != null) {
                    /*
                     * change dataset cids
                     */
                    for (XmlDoc.Element dataset : datasets) {
                        String srcDatasetCid = dataset.value();
                        String datasetAssetId = dataset.value("@id");
                        long dstDatasetNumber = Long.parseLong(CiteableIdUtil
                                .getLastSection(srcDatasetCid));
                        String dstDatasetCid = nig.mf.pssd.plugin.util.CiteableIdUtil
                                .generateCiteableID(executor, dstStudyCid,
                                        "infinity", dstDatasetNumber, true);
                        executor.execute("asset.cid.set", "<args><id>"
                                + datasetAssetId + "</id><cid>" + dstDatasetCid
                                + "</cid></args>", null, null);
                        /*
                         * update dataset metadata
                         */
                        updateDataset(executor, datasetAssetId, dstDatasetCid);
                    }
                }
                if (!srcStudyAssetNS.equals(dstExMethodAssetNS)) {
                    String dstStudyAssetNS = dstExMethodAssetNS + "/"
                            + dstStudyCid;
                    if (!executor.execute(
                            "asset.namespace.exists",
                            "<args><namespace>" + dstStudyAssetNS
                                    + "</namespace></args>", null, null)
                            .booleanValue("exists", false)) {
                        /*
                         * create study namespace
                         */
                        executor.execute("asset.namespace.create",
                                "<args><namespace>" + dstStudyAssetNS
                                        + "</namespace></args>", null, null);
                    }
                    /*
                     * move study to the new namespace
                     */
                    executor.execute("asset.move", "<args><id>"
                            + srcStudyAssetId + "</id><namespace>"
                            + dstStudyAssetNS + "</namespace></args>", null,
                            null);
                    if (datasets != null) {
                        for (XmlDoc.Element dataset : datasets) {
                            /*
                             * move datasets to the new namespace
                             */
                            String assetId = dataset.value("@id");
                            executor.execute("asset.move", "<args><id>"
                                    + assetId + "</id><namespace>"
                                    + dstStudyAssetNS + "</namespace></args>",
                                    null, null);
                        }
                    }
                    if (executor.execute(
                            "asset.query",
                            "<args><action>count</action><where>namespace>='"
                                    + srcStudyAssetNS + "'</where></args>",
                            null, null).intValue("value", 0) == 0) {
                        /*
                         * destroy the old namespace if it is empty.
                         */
                        executor.execute("asset.namespace.destroy",
                                "<args><namespace>" + srcStudyAssetNS
                                        + "</namespace></args>", null, null);
                    }
                }
                return false;
            }
        }).execute(executor);
    }

    private static void updateStudy(ServiceExecutor executor,
            String studyAssetId, String dstStudyCid) throws Throwable {
        String dstExMethodId = CiteableIdUtil.getParentId(dstStudyCid);
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + studyAssetId + "</id></args>", null, null)
                .element("asset");
        XmlDoc.Element se = ae.element("meta/daris:pssd-study");
        se.element("method").setValue(dstExMethodId);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", studyAssetId);
        dm.add("name", "study " + dstStudyCid);
        dm.push("meta", new String[] { "action", "merge" });
        dm.push("daris:pssd-study", new String[] { "id", se.value("@id") });
        dm.add(se, false);
        dm.pop();
        dm.pop();
        executor.execute("asset.set", dm.root());
    }

    private static void updateDataset(ServiceExecutor executor,
            String datasetAssetId, String dstDatasetCid) throws Throwable {
        String dstExMethodId = CiteableIdUtil.getParentId(dstDatasetCid,2);
        String dstSubjectId = CiteableIdUtil.getParentId(dstExMethodId);
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + datasetAssetId + "</id></args>", null, null)
                .element("asset");
        String datasetType = ae.value("meta/daris:pssd-dataset/type");
        if (datasetType == null
                || (!datasetType.equals("primary") && !datasetType
                        .equals("derivation"))) {
            throw new Exception("Invalid daris:pssd-dataset/type: "
                    + datasetType + " in asset " + datasetAssetId);
        }
        XmlDoc.Element de = null;
        if (datasetType.equals("primary")) {
            de = ae.element("meta/daris:pssd-acquisition");
            de.element("subject").setValue(dstSubjectId);
        } else {
            de = ae.element("meta/daris:pssd-derivation");
        }
        de.element("method").setValue(dstExMethodId);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", datasetAssetId);
        dm.push("meta", new String[] { "action", "merge" });
        dm.push(datasetType.equals("primary")?"daris:pssd-acquisition":"daris:pssd-derivation", new String[] { "id", de.value("@id") });
        dm.add(de, false);
        dm.pop();
        dm.pop();
        executor.execute("asset.set", dm.root());
    }

}
