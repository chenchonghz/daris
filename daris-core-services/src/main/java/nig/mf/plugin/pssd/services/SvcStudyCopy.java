package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;

import nig.mf.pssd.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyCopy extends PluginService {

    public static final String SERVICE_NAME = "daris.study.copy";
    public static final String SERVICE_DESCRIPTION = "Copy a study to another parent subject within the same or a different project. To run this service successfully, the user must have MODIFY access to the destination project.";

    private Interface _defn;

    public SvcStudyCopy() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of study.", 1, 1));

        Interface.Element to = new Interface.Element("to",
                CiteableIdType.DEFAULT,
                "The citeable id of the destination subject or ex-method.", 1,
                1);
        to.add(new Interface.Attribute("step", CiteableIdType.DEFAULT,
                "The ex-method step of the destination ex-method.", 0));
        _defn.add(to);
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
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
            final XmlWriter w) throws Throwable {
        final String srcStudyCid = args.value("cid");
        if (!CiteableIdUtil.isStudyId(srcStudyCid)) {
            throw new Exception(srcStudyCid
                    + " is not a valid citeable id for study.");
        }
        final XmlDoc.Element srcStudyAE = executor().execute("asset.get",
                "<args><cid>" + srcStudyCid + "</cid></args>", null, null)
                .element("asset");
        String srcMethodCid = srcStudyAE.value("meta/daris:pssd-study/method");
        String srcExMethodStep = srcStudyAE
                .value("meta/daris:pssd-study/method/@id");
        String to = args.value("to");
        String dstSubjectCid = null;
        String dstExMethodCid = null;
        String dstExMethodStep = args.value("to/@step");
        if (CiteableIdUtil.isSubjectId(to)) {
            dstSubjectCid = to;
            dstExMethodCid = findDstExMethod(executor(), dstSubjectCid,
                    srcMethodCid);
            if (dstExMethodCid == null) {
                throw new Exception("No ex-method found in subject "
                        + dstSubjectCid);
            }
        } else if (CiteableIdUtil.isExMethodId(to)) {
            dstSubjectCid = CiteableIdUtil.getParentId(to);
            dstExMethodCid = to;
        } else {
            throw new Exception("Invalid destination citeable id: " + to
                    + ". Must be the id of a subject or ex-method.");
        }
        if (srcStudyCid.startsWith(dstSubjectCid + ".")) {
            throw new Exception("Cannot send to the same subject: "
                    + dstSubjectCid);
        }
        String dstMethodCid = executor().execute("asset.get",
                "<args><cid>" + dstExMethodCid + "</cid></args>", null, null)
                .value("asset/meta/daris:pssd-ex-method/method/id");
        boolean methodMatch = srcMethodCid.equals(dstMethodCid);
        if (dstExMethodStep == null && methodMatch) {
            dstExMethodStep = findDstExMethodStep(executor(), dstExMethodCid,
                    srcExMethodStep);
        }
        final String dstExMethod = dstExMethodCid;
        final String dstStep = dstExMethodStep;
        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                String dstStudyCid = createDstStudy(executor, dstExMethod,
                        dstStep, srcStudyAE);
                w.push("study", new String[] { "cid", dstStudyCid });
                copyAttachments(executor, srcStudyAE.value("cid"), dstStudyCid,
                        w);
                Collection<String> srcDataSetCids = executor
                        .execute(
                                "asset.query",
                                "<args><where>cid in '"
                                        + srcStudyCid
                                        + "' and model='om.pssd.dataset'</where><size>infinity</size><action>get-cid</action></args>",
                                null, null).values("cid");
                if (srcDataSetCids != null) {
                    for (String srcDataSetCid : srcDataSetCids) {
                        String dstDataSetCid = createDstDataSet(executor,
                                dstStudyCid, srcDataSetCid);
                        w.push("dataset", new String[] { "cid", dstDataSetCid });
                        copyAttachments(executor, srcDataSetCid, dstDataSetCid,
                                w);
                        w.pop();
                    }
                }
                w.pop();
                return false;
            }
        }).execute(executor());
    }

    private static String createDstDataSet(ServiceExecutor executor,
            String dstStudyCid, String srcDataSetCid) throws Throwable {
        XmlDoc.Element srcDataSetAE = executor.execute("asset.get",
                "<args><cid>" + srcDataSetCid + "</cid></args>", null, null)
                .element("asset");
        String type = srcDataSetAE.stringValue("meta/daris:pssd-dataset/type",
                "derivation");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("fillin", true);
        dm.add("pid", dstStudyCid);
        if (srcDataSetAE.elementExists("type")) {
            dm.add("type", srcDataSetAE.value("type"));
        }
        if (srcDataSetAE.elementExists("content/type")) {
            dm.add("ctype", srcDataSetAE.value("content/type"));
        }
        if (srcDataSetAE.elementExists("content/ltype")) {
            dm.add("lctype", srcDataSetAE.value("content/ltype"));
        }
        if (srcDataSetAE.elementExists("meta/daris:pssd-object/name")) {
            dm.add("name", srcDataSetAE.value("meta/daris:pssd-object/name"));
        }
        if (srcDataSetAE.elementExists("meta/daris:pssd-object/description")) {
            dm.add("description",
                    srcDataSetAE.value("meta/daris:pssd-object/description"));
        }
        if (srcDataSetAE.elementExists("meta/daris:pssd-filename/original")) {
            dm.add("filename",
                    new String[] {
                            "private",
                            srcDataSetAE
                                    .value("meta/daris:pssd-filename/original/@private") },
                    srcDataSetAE.value("meta/daris:pssd-filename/original"));
        }
        boolean derived = type.equals("derivation");
        if (derived) {
            if (srcDataSetAE
                    .elementExists("meta/daris:pssd-derivation/processed")) {
                dm.add("processed", srcDataSetAE
                        .value("meta/daris:pssd-derivation/processed"));
            }
            if (srcDataSetAE.elementExists("meta/daris:pssd-derivation/input")) {
                List<XmlDoc.Element> ies = srcDataSetAE
                        .elements("meta/daris:pssd-derivation/input");
                dm.addAll(ies);
            }
        }
        List<XmlDoc.Element> mes = srcDataSetAE.element("meta").elements();
        dm.push("meta");
        if (mes != null && !mes.isEmpty()) {
            for (XmlDoc.Element me : mes) {
                String tag = me.value("@tag");
                if ("pssd.meta".equals(tag)) {
                    me.remove(me.attribute("tag"));
                    dm.add(me);
                }
            }
        }
        dm.push("mf-note");
        dm.add("note", "Copied from dataset " + srcDataSetAE.value("cid")
                + "(asset_id=" + srcDataSetAE.value("@id") + ")");
        dm.pop();
        dm.pop();
        dm.add("url", srcDataSetAE.value("content/url"));
        return executor.execute(
                derived ? "om.pssd.dataset.derivation.create"
                        : "om.pssd.dataset.primary.create", dm.root()).value(
                "id");
    }

    private static String createDstStudy(ServiceExecutor executor,
            String dstExMethodCid, String dstExMethodStep,
            XmlDoc.Element srcStudyAE) throws Throwable {
        String srcStudyType = srcStudyAE.value("meta/daris:pssd-study/type");
        boolean srcStudyProcessed = srcStudyAE.booleanValue(
                "meta/daris:pssd-study/processed", false);
        Collection<String> dstStudyTypes = executor.execute(
                "om.pssd.ex-method.study.type.list",
                "<args><id>" + dstExMethodCid + "</id></args>", null, null)
                .values("type");
        if (dstStudyTypes == null || !dstStudyTypes.contains(srcStudyType)) {
            throw new Exception("The destination ex-method " + dstExMethodCid
                    + " does not support study type: ");
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("fillin", true);
        dm.add("pid", dstExMethodCid);
        dm.add("type", srcStudyType);
        if (srcStudyProcessed) {
            dm.add("processed", true);
        }
        if (dstExMethodStep != null) {
            // methods match
            dm.add("step", dstExMethodStep);
        }
        if (srcStudyAE.elementExists("meta/daris:pssd-object/name")) {
            dm.add("name", srcStudyAE.value("meta/daris:pssd-object/name"));
        }
        if (srcStudyAE.elementExists("meta/daris:pssd-object/description")) {
            dm.add("description",
                    srcStudyAE.value("meta/daris:pssd-object/description"));
        }
        List<XmlDoc.Element> mes = srcStudyAE.element("meta").elements();
        dm.push("meta");
        if (mes != null && !mes.isEmpty()) {
            for (XmlDoc.Element me : mes) {
                String tag = me.value("@tag");
                if ("pssd.meta".equals(tag)) {
                    me.remove(me.attribute("tag"));
                    me.remove(me.attribute("id"));
                    dm.add(me);
                }
            }
        }
        dm.push("mf-note");
        dm.add("note", "Copied from study " + srcStudyAE.value("cid")
                + "(asset_id=" + srcStudyAE.value("@id") + ")");
        dm.pop();
        dm.pop();
        return executor.execute("om.pssd.study.create", dm.root()).value("id");
    }

    private static String findDstExMethodStep(ServiceExecutor executor,
            String dstExMethodCid, String srcExMethodStep) throws Throwable {
        Collection<String> steps = executor.execute(
                "om.pssd.ex-method.study.step.find",
                "<args><id>" + dstExMethodCid + "</id></args>", null, null)
                .values("ex-method/step");
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        if (steps.contains(srcExMethodStep)) {
            return srcExMethodStep;
        } else {
            return null;
        }
    }

    private static String findDstExMethod(ServiceExecutor executor,
            String dstSubjectCid, String srcMethodCid) throws Throwable {
        String query1 = "cid in '" + dstSubjectCid
                + "' and model='om.pssd.ex-method'";
        String query2 = srcMethodCid == null ? query1
                : (query1 + " and xpath(daris:pssd-ex-method/method/id)='"
                        + srcMethodCid + "'");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", srcMethodCid == null ? query1 : query2);
        dm.add("size", 1);
        dm.add("action", "get-cid");
        String dstExMethodCid = executor.execute("asset.query", dm.root())
                .value("cid");
        if (srcMethodCid == null) {
            return dstExMethodCid;
        }
        if (dstExMethodCid == null) {
            // try return a ex-method without matching method
            dm = new XmlDocMaker("args");
            dm.add("where", query1);
            dm.add("size", 1);
            dm.add("action", "get-cid");
            dstExMethodCid = executor.execute("asset.query", dm.root()).value(
                    "cid");
        }
        return dstExMethodCid;
    }

    private static void copyAttachments(ServiceExecutor executor,
            String srcObjCid, String dstObjCid, XmlWriter w) throws Throwable {
        Collection<String> srcAttachmentAssetIds = executor.execute(
                "asset.get", "<args><cid>" + srcObjCid + "</cid></args>", null,
                null).values("asset/related[@type='attachment']/to");
        if (srcAttachmentAssetIds == null || srcAttachmentAssetIds.isEmpty()) {
            return;
        }
        XmlDoc.Element dstObjAE = executor.execute("asset.get",
                "<args><cid>" + dstObjCid + "</cid></args>", null, null)
                .element("asset");
        String dstObjId = dstObjAE.value("@id");
        for (String srcAttachmentAssetId : srcAttachmentAssetIds) {
            XmlDoc.Element ae = executor.execute("asset.get",
                    "<args><id>" + srcAttachmentAssetId + "</id></args>", null,
                    null).element("asset");
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("name", ae.value("name"));
            dm.add("namespace", dstObjAE.value("namespace"));
            if (ae.elementExists("description")) {
                dm.add("description", ae.value("description"));
            }
            if (ae.elementExists("type")) {
                dm.add("type", ae.value("type"));
            }
            if (ae.elementExists("content/type")) {
                dm.add("ctype", ae.value("content/type"));
            }
            if (ae.elementExists("content/ltype")) {
                dm.add("lctype", ae.value("content/ltype"));
            }
            dm.add("url", ae.value("content/url"));
            dm.push("related");
            dm.add("from", new String[] { "relationship", "attachment" },
                    dstObjId);
            dm.pop();
            List<XmlDoc.Element> acls = dstObjAE.elements("acl");
            if (acls != null) {
                for (XmlDoc.Element acl : acls) {
                    while (acl.elementExists("asset")) {
                        acl.remove(acl.element("asset"));
                    }
                    XmlDoc.Element actor = acl.element("actor");
                    actor.remove(actor.attribute("id"));
                    dm.add(acl);
                    System.out.println(acl);
                }
            }
            System.out.println(dm.root());
            String dstAttachmentAssetId = executor.execute("asset.create",
                    dm.root()).value("id");
            w.add("attachment", new String[] { "id", dstAttachmentAssetId });
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
