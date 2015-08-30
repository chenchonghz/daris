package nig.mf.plugin.pssd;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import nig.iio.metadata.XMLUtil;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.method.ActionStep;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.services.SvcCollectionMemberCount;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class Study {
    public static final PSSDObject.Type TYPE = PSSDObject.Type.study;

    public static final int DEPTH = 3;

    public static final String MODEL = "om.pssd.study";

    public static final String TYPE_DICTIONARY = "daris:pssd.study.types";

    private String _type;
    private String _mid; // ExMethod cid
    private String _mname;
    private String _step;
    private String _sname;

    /**
     * Constructor
     * 
     * @param type
     *            type of Study object
     * @param mid
     *            ExMethod CID
     * @param mname
     *            Name of Method
     * @param step
     *            STep in Method
     * @param sname
     *            Name of STep in ExMethod
     */
    public Study(String type, String mid, String mname, String step,
            String sname) {
        _type = type;
        _mid = mid;
        _mname = mname;
        _step = step;
        _sname = sname;
    }

    public String type() {
        return _type;
    }

    public String methodId() {
        return _mid;
    }

    public String methodName() {
        return _mname;
    }

    public String step() {
        return _step;
    }

    public String stepName() {
        return _sname;
    }

    /**
     * Create a Study on the local server
     * 
     * @param executor
     * @param dExMethod
     *            Distributed citeable asset of the parent ExMethod
     * @param studyNumber
     * @param type
     * @param name
     * @param description
     * @param step
     * @param meta
     * @param dProject
     * @param fillIn
     * @return
     * @throws Throwable
     */
    public static String create(ServiceExecutor executor,
            DistributedAsset dExMethod, long studyNumber, String type,
            String name, String description, Boolean processed, String step,
            boolean allowIncompleteMeta, XmlDoc.Element meta,
            DistributedAsset dProject, boolean fillIn) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");

        // Generate CID, filling in allocator space if desired.
        // The parent could be anywhere in the federation so allow Mediaflux to
        // find the parent CID and allocate the child CID by setting the
        // distation to infinity
        // Ignores servers it can't reach.
        String pdist = "infinity";
        String cid = nig.mf.pssd.plugin.util.CiteableIdUtil
                .generateCiteableID(executor, dExMethod.getCiteableID(), pdist,
                        studyNumber, fillIn);
        dm.add("cid", cid);
        dm.add("name", "study " + cid);
        String studyNS = PSSDUtils.namespace(executor, dExMethod) + "/" + cid;
        dm.add("namespace", new String[]{"create", "true"}, studyNS);
        dm.add("model", Study.MODEL);

        if (dExMethod != null && step != null) {
            String etype = addMethodStudyTemplates(executor, dm, dExMethod,
                    step);
            if (type == null) {
                type = etype;
            }
        }
        dm.add("allow-incomplete-meta", allowIncompleteMeta);
        dm.push("meta");
        PSSDUtils.setObjectMeta(dm, Study.TYPE, name, description, true);

        // Set Method and other meta-data namespaces and tags
        setMethodAndOptionalMeta(meta, dExMethod, step, dm);

        dm.push("daris:pssd-study");
        dm.add("type", type);
        // TBD: nebk: Should method and step should be mandatory? Is a Study
        // always
        // created from a Method and step.
        if (dExMethod != null && step != null) {
            dm.add("method", new String[] { "step", step },
                    dExMethod.getCiteableID());
        }
        if (processed != null) {
            dm.add("processed", processed);
        }

        dm.pop();

        dm.pop();

        PSSDUtils.addStudyACLs(dm, dProject.getCiteableID());

        XmlDoc.Element r = executor.execute("asset.create", dm.root());
        if (cid == null) {
            return r.value("cid");
        }

        // Generate system event
        SystemEventChannel.generate(new PSSDObjectEvent(Action.CREATE, cid, 0));

        return cid;
    }

    private static void setMethodAndOptionalMeta(XmlDoc.Element meta,
            DistributedAsset dExMethod, String step, XmlDocMaker dm)
            throws Throwable {
        if (meta == null)
            return;

        List<XmlDoc.Element> mes = meta.elements();
        if (mes != null) {
            // Filter out method specific metadata.
            String mns = ExMethod
                    .metaNamespace(dExMethod.getCiteableID(), step);

            XmlDoc.Element om = new XmlDoc.Element("meta");
            for (XmlDoc.Element me : mes) {

                String ns = me.value("@ns");
                String ns2 = null;
                if (ns != null)
                    ns2 = ExMethod.partialMetaNameSpace(ns); // Pull off
                                                             // optional group
                                                             // component of
                                                             // supplied meta
                                                             // namespace
                if (ns2 == null || !ns2.equalsIgnoreCase(mns)) {
                    // Non-method meta-data gets the tag pssd.meta and whatever
                    // namespace is set
                    om.add(me);
                } else {

                    // Method meta-data has the namespace -ns <ex
                    // method>_<step>_<group> where <group> is optional
                    dm.add(me);
                }
            }

            // If there is anything left, then that is optional metadata.
            PSSDUtils.setObjectOptionalMeta(dm, om, "om.pssd.study");
        }

    }

    /**
     * Returns true if the CID is for a Study object
     */
    public static boolean isObjectStudy(ServiceExecutor executor, String cid)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", cid);
        XmlDoc.Element r = executor.execute("om.pssd.object.type", dm.root());
        PSSDObject.Type type = PSSDObject.Type.parse(r.value("type"));
        if (type == TYPE)
            return true;
        return false;
    }

    /**
     * Adds templates for a study. This will specify meta-data for Study Action
     * steps that require meta-data attached to a Study. Returns the expected
     * type of study.
     * 
     * @param executor
     * @param dm
     * @param dEID
     *            Distributed exMethod CID (checked to be compliant with
     *            federation policy)
     * @param stepPath
     * @return
     * @throws Throwable
     */
    public static String addMethodStudyTemplates(ServiceExecutor executor,
            XmlDocMaker dm, DistributedAsset dEID, String stepPath)
            throws Throwable {

        // Reinstantiate ExMethod from its asset
        ExMethod em = ExMethod.lookup(executor, dEID);

        // Find the action step for this path
        ActionStep as = em.method().actionStepByPath(stepPath);

        // See if there are any Study actions (could also be Subject actions)
        // The actual implementation of om.pssd.method.create only allows
        // one study action per step. So this code is more general.
        List<XmlDoc.Element> sas = as.studyActions();
        if (sas == null) {
            throw new Exception("Not a study step: method="
                    + dEID.getCiteableID() + ", step=" + stepPath);
        }

        String mns = ExMethod.metaNamespace(dEID.getCiteableID(), stepPath);

        String type = null;

        // Make a hash map to track template pushes which may now be group
        // dependent
        HashMap<String, Integer> pushed = new HashMap<String, Integer>();

        // We need a doc maker for each template namespace
        // At the end we'll add them all in to the master
        Vector<XmlDocMaker> dms = new Vector<XmlDocMaker>();
        int idx = 0;

        // Iterate over the Study actions
        for (int i = 0; i < sas.size(); i++) {
            XmlDoc.Element se = sas.get(i);

            if (type == null) {
                type = se.value("type");
            }

            // If the Study action prescribes that some meta-data are required
            // then include that in the template
            List<XmlDoc.Element> mes = se.elements("metadata");
            if (mes != null) {
                for (XmlDoc.Element me : mes) {
                    // Get the document grouping if any
                    XmlDoc.Element def = me.element("definition");
                    String group = def.value("@group");
                    String ns = ExMethod.addGroupToMetaNamespace(mns, group);

                    if (group != null) {

                        // Remove the group attribute from the definition as it
                        // 1) isn't part of
                        // the expected syntax and 2) it is now manifested in
                        // the namespace
                        XMLUtil.removeAttribute(me, "group");
                    } else {

                        // No group so we make a 'pseudo' group for the purposes
                        // of
                        // creating a template element
                        group = "_______NO_GROUP_GROUP________";
                    }

                    // Create and push a new template element as needed
                    XmlDocMaker dm2 = null;
                    if (pushed.containsKey(group)) {
                        // Fetch the right DocMaker
                        int idx2 = pushed.get(group);
                        dm2 = dms.get(idx2);
                    } else {
                        // Make a new DocMaker and push the template element
                        dms.add(new XmlDocMaker("args"));
                        dm2 = dms.get(idx);
                        dm2.push("template", new String[] { "ns", ns });
                        pushed.put(group, idx);
                        //
                        idx++;
                    }

                    // Add element
                    dm2.add(me);
                }
            }
        }

        // Pop all DMs and add to master DM
        for (int i = 0; i < dms.size(); i++) {
            XmlDocMaker t = dms.get(i);
            t.pop();
            dm.add(t.root().element("template"));
        }

        return type;
    }

    /**
     * Update local Study object
     * 
     * @param executor
     * @param id
     *            Citeable ID of Study object
     * @param name
     * @param description
     * @param exMethod
     *            is the CID of the ExMethod that we want to update the Study
     *            with. We don't need to fetch its meta-data so we don't care
     *            where it is located in a federation. It may be null. It's only
     *            passed in if we want to change it.
     * @param step
     * @param meta
     * @return
     * @throws Throwable
     */
    public static String update(ServiceExecutor executor, String id,
            String type, String name, String description, Boolean processed,
            String exMethod, String step, boolean allowIncompleteMeta,
            XmlDoc.Element meta) throws Throwable {

        // See if we can safely update the exmethod/step (only if no template
        // information). The template info has name space
        // <ExMethod>_<Step>_<Group> where
        // <Group> is optional. This means that you can't change the Step or
        // ExMethod information for this Study if
        // it has template meta-data
        // TBD: there are now MF services to allow modification of :template so
        // this could be relaxed
        if (!PSSDUtils.checkStudyTemplateForEdit(executor, exMethod, step, id)) {
            throw new Exception(
                    "Cannot update Study ExMethod/Step because this Study has template information that cannot currently be updated");
        }

        // Get existing meta from local Study object
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        dm.add("pdist", 0); // Force local
        XmlDoc.Element oldAsset = executor.execute("asset.get", dm.root());

        // Build new meta
        dm = new XmlDocMaker("args");
        dm.add("cid", id);
        dm.add("allow-incomplete-meta", allowIncompleteMeta);
        dm.push("meta", new String[] { "action", "merge" });
        PSSDUtils.setObjectMeta(dm, Study.TYPE, name, description, false);
        dm.pop();

        // Get the existing daris:pssd-study meta
        XmlDoc.Element oldEl = oldAsset.element("asset/meta/daris:pssd-study");

        // AFter we have edited (or not) the daris:pssd-study-method/method (the
        // ex-method) these
        // 'new' variables will hold the actual ex method and step of this Study
        String oldStep = oldEl.value("method/@step");
        String oldExMethod = oldEl.value("method");
        //
        String newExMethod = null;
        String newStep = null;

        // Handle modification of the method/step. If we relax the restriction
        // that if there is :template information you can update ExMethod/STep
        // have to think hard about namespaces (-ns) of documents. Probably
        // if we change method/step we should insist on the :meta being replaced
        // with correct namespaces refelcting the new ex method/step
        if (exMethod != null || step != null || type != null
                || processed != null) {

            // Remove old (only way to update the step)
            dm.push("meta", new String[] { "action", "remove" });
            dm.push("daris:pssd-study");
            dm.pop();
            dm.pop();

            // Now add new
            dm.push("meta", new String[] { "action", "merge" });
            dm.push("daris:pssd-study");
            if (type == null) {
                dm.add("type", oldEl.value("type"));
            } else {
                dm.add("type", type);
            }

            if (processed == null) {
                if (oldEl.value("processed") != null)
                    dm.add("processed", oldEl.value("processed"));
            } else {
                dm.add("processed", processed);
            }

            // Set new Method/step. SOme or all of old/current may be null.
            newStep = step;
            if (newStep == null)
                newStep = oldStep;
            //
            newExMethod = exMethod;
            if (newExMethod == null)
                newExMethod = oldExMethod;

            // MF requires attributes so can't have naked Method.
            if (newStep != null && newExMethod != null) {
                dm.add("method", new String[] { "step", newStep }, newExMethod);
            }
            dm.pop();
            dm.pop();
        }
        if (newExMethod == null)
            newExMethod = oldExMethod;
        if (newStep == null)
            newStep = oldStep;

        if (meta != null) {
            // Extract the action for handling the meta; defaults to replace
            XmlDoc.Attribute actionAttr = meta.attribute("action");
            String action = "replace";
            if (actionAttr != null)
                action = actionAttr.value();
            dm.push("meta", new String[] { "action", action });

            // Handle namespaces on meta
            DistributedAsset dExMethod = new DistributedAsset(null, newExMethod);
            setMethodAndOptionalMeta(meta, dExMethod, step, dm);
            //
            dm.pop();
        }

        // Do it
        executor.execute("asset.set", dm.root());

        // Generate system event
        SystemEventChannel.generate(new PSSDObjectEvent(Action.MODIFY, id,
                SvcCollectionMemberCount.countMembers(executor, id)));
        return id;

    }

}
