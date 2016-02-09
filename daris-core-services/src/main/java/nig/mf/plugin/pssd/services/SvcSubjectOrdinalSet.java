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

public class SvcSubjectOrdinalSet extends PluginService {

    public static final String SERVICE_NAME = "daris.subject.ordinal.set";

    private Interface _defn;

    public SvcSubjectOrdinalSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The current citeable id of the subject.", 1, 1));
        _defn.add(new Interface.Element("ordinal", IntegerType.POSITIVE_ONE,
                "The new ordinal number for the subject.", 1, 1));
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
        return "Set the ordinal number part of the subject cid. It will also change the citeable ids of the descendants contained by the subject.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        final String cid = args.value("cid");
        final int ordinal = args.intValue("ordinal");
        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor exec) throws Throwable {
                // find descendants
                Collection<String> descendantCids = executor()
                        .execute("asset.query",
                                "<args><where>cid starts with '" + cid
                                        + "'</where><size>infinity</size><action>get-cid</action></args>",
                                null, null)
                        .values("cid");
                String newCid = replaceSubjectOrdinal(cid, ordinal);
                // set subject cid
                changeAssetCid(exec, cid, newCid);

                if (descendantCids == null || descendantCids.isEmpty()) {
                    return false;
                }
                // process descendants
                for (String descendantCid : descendantCids) {
                    String newDescendantCid = replaceSubjectOrdinal(
                            descendantCid, ordinal);
                    changeAssetCid(exec, descendantCid, newDescendantCid);
                }
                return false;
            }

        }).execute(executor());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    private static void changeAssetCid(ServiceExecutor executor, String cid,
            String newCid) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><cid>" + cid + "</cid></args>", null, null)
                .element("asset");
        String assetId = ae.value("@id");
        String assetName = ae.value("name");
        String assetNamespace = ae.value("namespace");
        String objectType = ae.value("meta/daris:pssd-object/type");

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("cid", cid);
        executor.execute("asset.cid.set", dm.root());

        // set subject asset name
        if (assetName != null && assetName.endsWith(cid)) {
            setAssetName(executor, assetId, assetName.replace(cid, newCid));
        }
        // rename subject namespace
        if (assetNamespace != null && assetNamespace.endsWith("/" + cid)) {
            renameAssetNamespace(executor, assetNamespace, newCid);
        }

        if ("study".equals(objectType)) {
            /*
             * update pssd-study/method
             */
            String exMethodStep = ae
                    .value("meta/daris:pssd-study/method/@step");
            String oldExMethodCid = ae.value("meta/daris:pssd-study/method");
            String newExMethodCid = CiteableIdUtil.getParentId(newCid);
            if (!oldExMethodCid.equals(newExMethodCid)) {
                updateStudyExMethod(executor, assetId, exMethodStep,
                        newExMethodCid);
                /*
                 * update template ns
                 */
                String oldTplNs = oldExMethodCid + "_" + exMethodStep;
                String newTplNs = newExMethodCid + "_" + exMethodStep;
                SvcAssetTemplateNamespaceReplace.replaceAssetTemplateNamespace(
                        executor, ae, oldTplNs, newTplNs);
            }
        }
        if ("dataset".equals(objectType)) {
            String oldSubjectCid = CiteableIdUtil.getSubjectId(cid);
            String newSubjectCid = CiteableIdUtil.getSubjectId(newCid);
            String oldExMethodCid = CiteableIdUtil.getExMethodId(cid);
            String newExMethodCid = CiteableIdUtil.getExMethodId(newCid);
            if (!oldSubjectCid.equals(newSubjectCid)
                    || !oldExMethodCid.equals(newExMethodCid)) {
                updateDatasetMeta(executor, ae, newCid);
            }
        }
    }

    private static void updateDatasetMeta(ServiceExecutor executor, Element ae,
            String newCid) throws Throwable {
        String assetId = ae.value("@id");
        String cid = ae.value("cid");
        String oldSubjectCid = CiteableIdUtil.getSubjectId(cid);
        String newSubjectCid = CiteableIdUtil.getSubjectId(newCid);
        String oldExMethodCid = CiteableIdUtil.getExMethodId(cid);
        String newExMethodCid = CiteableIdUtil.getExMethodId(newCid);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.push("meta");
        if (ae.elementExists("meta/daris:pssd-acquisition")
                && (!newSubjectCid.equals(oldSubjectCid)
                        || !newExMethodCid.equals(oldExMethodCid))) {
            dm.push("daris:pssd-acquisition");
            if (!newSubjectCid.equals(oldSubjectCid) && oldSubjectCid
                    .equals(ae.value("meta/daris:pssd-acquisition/subject"))) {
                dm.add("subject",
                        new String[] { "state",
                                ae.value(
                                        "meta/daris:pssd-acquisition/subject/@state") },
                        newSubjectCid);
            }
            if (!newExMethodCid.equals(oldExMethodCid)
                    && ae.elementExists("meta/daris:pssd-acquisition/method")) {
                dm.add("method",
                        new String[] { "step",
                                ae.value(
                                        "meta/daris:pssd-acquisition/method/@step") },
                        newExMethodCid);
            }
            dm.pop();
        }
        if (ae.elementExists("meta/daris:pssd-derivation")
                && !newExMethodCid.equals(oldExMethodCid)) {
            dm.push("daris:pssd-derivation");
            if (!newExMethodCid.equals(oldExMethodCid)
                    && ae.elementExists("meta/daris:pssd-derivation/method")) {
                dm.add("method",
                        new String[] { "step",
                                ae.value(
                                        "meta/daris:pssd-derivation/method/@step") },
                        newExMethodCid);
            }
            dm.pop();
        }
        dm.pop();
    }

    private static void updateStudyExMethod(ServiceExecutor executor,
            String studyAssetId, String exMethodStep, String exMethodCid)
                    throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", studyAssetId);
        dm.push("meta");
        dm.push("daris:pssd-study");
        dm.add("method", new String[] { "step", exMethodStep }, exMethodCid);
        dm.pop();
        dm.pop();
        executor.execute("asset.set", dm.root());
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

    private static String replaceSubjectOrdinal(String cid, int ordinal) {
        if (ordinal <= 0) {
            throw new AssertionError(
                    "Expects ordinal number greater than zero. Found "
                            + ordinal);
        }
        if (!(CiteableIdUtil.isDataSetId(cid) || CiteableIdUtil.isStudyId(cid)
                || CiteableIdUtil.isExMethodId(cid)
                || CiteableIdUtil.isSubjectId(cid))) {
            throw new AssertionError(
                    "Expects cid of subject, ex-method, study or dataset. Found "
                            + cid);
        }
        if (CiteableIdUtil.isSubjectId(cid)) {
            return CiteableIdUtil.getParentId(cid) + "." + ordinal;
        }
        if (CiteableIdUtil.isExMethodId(cid)) {
            return replaceSubjectOrdinal(CiteableIdUtil.getParentId(cid),
                    ordinal) + "." + CiteableIdUtil.getLastSection(cid);
        }
        if (CiteableIdUtil.isStudyId(cid)) {
            return replaceSubjectOrdinal(CiteableIdUtil.getParentId(cid),
                    ordinal) + "." + CiteableIdUtil.getLastSection(cid);
        }
        if (CiteableIdUtil.isDataSetId(cid)) {
            return replaceSubjectOrdinal(CiteableIdUtil.getParentId(cid),
                    ordinal) + "." + CiteableIdUtil.getLastSection(cid);
        }
        throw new AssertionError(
                "Expects cid of subject, ex-method, study or dataset. Found "
                        + cid);
    }

    public static void main(String[] args) {
        System.out.println(replaceSubjectOrdinal("1.5.1.1", 6));
        System.out.println(replaceSubjectOrdinal("1.5.1.1.1", 6));
        System.out.println(replaceSubjectOrdinal("1.5.1.1.1.2", 6));
        System.out.println(replaceSubjectOrdinal("1.5.1.1.1.2.3", 6));
    }

}
