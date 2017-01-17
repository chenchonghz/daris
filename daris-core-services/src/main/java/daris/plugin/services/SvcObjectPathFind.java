package daris.plugin.services;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.CiteableIdUtil;

public class SvcObjectPathFind extends PluginService {

    public static final String SERVICE_NAME = "daris.object.path.find";

    private Interface _defn;

    public SvcObjectPathFind() {
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
        return "Describes the path to the object in the project hierarchy, also includes the first child of the object if any.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String cid = ServiceUtils.getObjectIdentifiers(executor(), args).getValue();

        List<String> parentCids = getParentCids(cid);
        if (parentCids != null && !parentCids.isEmpty()) {
            int depth = parentCids.size();
            for (String parentCid : parentCids) {
                addParent(executor(), parentCid, depth, w);
                depth--;
            }
        }

        addObject(executor(), cid, w);

        addFirstChild(executor(), cid, w);

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    static List<String> getParentCids(String cid) {
        if (cid == null || !CiteableIdUtil.isCiteableId(cid)) {
            return null;
        }
        int depth = CiteableIdUtil.getIdDepth(cid);
        if (depth <= CiteableIdUtil.projectDepth() || depth > CiteableIdUtil.dataSetDepth()) {
            return null;
        }
        List<String> pcids = new ArrayList<String>();
        String projectCid = CiteableIdUtil.getProjectId(cid);
        pcids.add(projectCid);
        if (CiteableIdUtil.isSubjectId(cid)) {
            return pcids;
        }
        String subjectCid = CiteableIdUtil.getSubjectId(cid);
        pcids.add(subjectCid);
        if (CiteableIdUtil.isExMethodId(cid)) {
            return pcids;
        }
        String exMethodCid = CiteableIdUtil.getExMethodId(cid);
        pcids.add(exMethodCid);
        if (CiteableIdUtil.isStudyId(cid)) {
            return pcids;
        }
        String studyCid = CiteableIdUtil.getStudyId(cid);
        pcids.add(studyCid);
        if (CiteableIdUtil.isDataSetId(cid)) {
            return pcids;
        }
        return null;
    }

    private void addParent(ServiceExecutor executor, String parentCid, int depth, XmlWriter w) throws Throwable {
        XmlDoc.Element ae = ServiceUtils.getAssetMeta(executor, null, parentCid);
        String assetId = ae.value("@id");
        String type = ae.value("meta/daris:pssd-object/type");
        String name = ae.value("meta/daris:pssd-object/name");
        long nbc = SvcObjectChildrenCount.countChildren(executor, parentCid);
        w.add("parent", new String[] { "cid", parentCid, "id", assetId, "depth", Integer.toString(depth), "type", type,
                "nbc", Long.toString(nbc), "name", name });
    }

    private void addObject(ServiceExecutor executor, String cid, XmlWriter w) throws Throwable {
        XmlDoc.Element ae = ServiceUtils.getAssetMeta(executor, null, cid);
        String assetId = ae.value("@id");
        String type = ae.value("meta/daris:pssd-object/type");
        String name = ae.value("meta/daris:pssd-object/name");
        long nbc = SvcObjectChildrenCount.countChildren(executor, cid);
        String datasetType = null;
        String datasetProcessed = null;
        if (CiteableIdUtil.isDataSetId(cid)) {
            datasetType = ae.value("meta/daris:pssd-dataset/type");
            datasetProcessed = ae.value("meta/daris:pssd-derivation/processed");
        }
        w.add("object", new String[] { "cid", cid, "id", assetId, "type", type, "nbc", Long.toString(nbc),
                "dataset-type", datasetType, "dataset-processed", datasetProcessed, "name", name });
    }

    /**
     * 
     * @param executor
     * @param cid
     *            cid of the parent object
     * @param w
     * @throws Throwable
     */
    private void addFirstChild(ServiceExecutor executor, String cid, XmlWriter w) throws Throwable {
        if (CiteableIdUtil.isDataSetId(cid)) {
            return;
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        if (cid == null) {
            dm.add("where", "model='om.pssd.project'");
        } else {
            dm.add("where", "cid in '" + cid + "'");
        }
        dm.add("size", 1);
        dm.push("sort");
        dm.add("key", "cid");
        dm.pop();
        dm.add("action", "get-meta");
        XmlDoc.Element ae = executor.execute("asset.query", dm.root()).element("asset");
        if (ae == null) {
            return;
        }
        String childAssetId = ae.value("@id");
        String childCid = ae.value("cid");
        String childType = ae.value("meta/daris:pssd-object/type");
        String childName = ae.value("meta/daris:pssd-object/name");
        long nbc = SvcObjectChildrenCount.countChildren(executor, childCid);
        String datasetType = null;
        String datasetProcessed = null;
        if (CiteableIdUtil.isDataSetId(childCid)) {
            datasetType = ae.value("meta/daris:pssd-dataset/type");
            datasetProcessed = ae.value("meta/daris:pssd-derivation/processed");
        }
        w.add("child", new String[] { "cid", childCid, "id", childAssetId, "type", childType, "nbc", Long.toString(nbc),
                "dataset-type", datasetType, "dataset-processed", datasetProcessed, "name", childName });

    }

}
