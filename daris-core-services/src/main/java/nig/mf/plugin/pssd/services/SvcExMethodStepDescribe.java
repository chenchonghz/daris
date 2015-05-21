package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.method.ActionStep;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.ExMethodStepStatus;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcExMethodStepDescribe extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.ex-method.step.describe";

    private Interface _defn;

    public SvcExMethodStepDescribe() {
        _defn = new Interface();
        Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the executing ExMethod.", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);

        _defn.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The step within the method to describe.", 1, 1));
    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return "Describes a step in the identified method execution, including identity of subjects, data sets, and associated metadata. Can only be used to describe either a subject or study step.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        // Set distributed ExMethod asset. Might be primary or replica, we don't
        // care.
        // It could be anywhere in the federation.
        DistributedAsset dID = new DistributedAsset(args.element("id"));

        // Reconstruct ExMethod from the asset
        ExMethod em = ExMethod.lookup(executor(), dID);

        // Retrieve the Method
        Method m = em.method();
        if (m != null) {

            w.push("ex-method", new String[] { "proute", dID.getServerRoute(), "id", dID.getCiteableID() });

            String sid = args.value("step");

            ActionStep as = m.actionStepByPath(sid);

            w.push("step", new String[] { "id", sid });
            w.add("name", as.name());

            if (as.description() != null) {
                w.add("description", as.description());
            }

            ExMethodStepStatus mss = em.statusForStep(sid);
            if (mss != null) {
                w.push("status");
                w.add("state", mss.status());
                if (mss.notes() != null) {
                    w.add("notes", mss.notes());
                }
                w.pop();
            }

            describeActionStep(w, dID, sid, as);

            w.pop();

            w.pop();
        }

    }

    /**
     * Describe the given action step.
     * 
     * @param w
     * @param dID
     *            The identity of the ExMethod
     * @param sid
     *            The step identifier
     * @param as
     *            The action step
     * @throws Throwable
     */
    private void describeActionStep(XmlWriter w, DistributedAsset dID, String sid, ActionStep as) throws Throwable {

        // Method namespace, for metadata on the assets.
        // Of the form <ex method id>_<step>
        // It may, on a document by document basis, get an extra _<group> added
        // on
        // for Study action steps
        String mns = ExMethod.metaNamespace(dID.getCiteableID(), sid);

        // Subject actions..
        // FOr now, we do not handle document 'group' attributes.
        describeSubjectActions(w, dID, mns, as.subjectActions());

        // Study actions..
        // We do handle the document 'group' attribute indicating a document
        // grouping that e.g. the portal can use
        describeStudyActions(w, dID, sid, mns, as.studyActions());

        describeTransformActions(w, dID, sid, mns, as.transformActions());
    }

    private void describeTransformActions(XmlWriter w, DistributedAsset dEID, String sid, String mns, List<Element> sas)
            throws Throwable {
        if (sas == null) {
            return;
        }
        for (XmlDoc.Element se : sas) {
            w.add(se);
        }

    }

    /**
     * 
     * @param w
     * @param dEID
     *            The identity of the ExMethod
     * @param mns
     * @param sas
     * @throws Throwable
     */
    private void describeSubjectActions(XmlWriter w, DistributedAsset dEID, String mns, List<XmlDoc.Element> sas)
            throws Throwable {
        if (sas == null) {
            return;
        }

        // Identity of parent subject.
        // This service is sometimes called in the context of finding the
        // meta-data needed to
        // create a Study so use read/write context
        Boolean readOnly = false;
        DistributedAsset dSID = dEID.getParentSubject(readOnly);

        Vector<XmlDoc.Element> psTemplates = null;
        Vector<XmlDoc.Element> rsTemplates = null;

        for (XmlDoc.Element se : sas) {
            Collection<XmlDoc.Element> mes = se.elements("metadata");
            if (mes != null) {
                XmlDoc.Element te = new XmlDoc.Element("template");
                te.add(new XmlDoc.Attribute("ns", mns));
                for (XmlDoc.Element me : mes) {
                    te.add(me);
                }

                String part = se.value("@part");

                // Part applies to either the project subject, or the r-subject.
                if (part.equals("p")) {
                    if (psTemplates == null) {
                        psTemplates = new Vector<XmlDoc.Element>();
                    }

                    psTemplates.add(te);
                } else {
                    if (rsTemplates == null) {
                        rsTemplates = new Vector<XmlDoc.Element>();
                    }

                    rsTemplates.add(te);
                }
            }
        }

        if (psTemplates != null) {
            w.push("subject", new String[] { "type", "p", "id", dSID.getCiteableID() });
            describeAssetMetadata(w, dSID, mns, psTemplates);
            w.pop();
        }

        if (rsTemplates != null) {

            // Describe the Subject to find the RSubject if any
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("cid", dSID.getCiteableID());
            dm.add("pdist", 0); // Force local on whatever server it's executed
            XmlDoc.Element r = executor().execute(dSID.getServerRouteObject(), "asset.get", dm.root());
            String rsid = r.value("meta/daris:pssd-subject/r-subject");

            if (rsid != null) {
                // Where do we look for the RSubject ? We don't really care
                // whether it's a replica or primary for this Process; we assume
                // the
                // meta-data is the same regardless
                DistributedAsset dRSID = DistributedAssetUtil.findObject(executor(), rsid, null);
                if (dRSID == null) {
                    throw new Exception("No asset RSubject with the citeable ID " + rsid + " can be found");
                }

                w.push("subject", new String[] { "type", "r", "id", dSID.getCiteableID() });
                describeAssetMetadata(w, dRSID, mns, rsTemplates);
                w.pop();
            }
        }

    }

    private void describeAssetMetadata(XmlWriter w, DistributedAsset dID, String mns, List<XmlDoc.Element> templates)
            throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", dID.getCiteableID());
        dm.add("format", "template");
        dm.add("template-if-namespace", mns);

        // Each template may have multiple document types
        // This is because each Subject action step may define
        // meta-data from multiple document types
        for (XmlDoc.Element template : templates) {

            // TBD: If we were to add the 'group' capability (like for Study
            // action
            // steps, we'd need to iterate over all meta-data documents
            // and remove the group attribute. The code below just
            // assumes one document. Further, because the template-if-namespace
            // filter is fixed (exmethod_step), the asset.get process below
            // is not general enough because each document (with a group)
            // needs a namespace like exmethod_step_group
            // This coujld be worked around by calling asset.get multiple
            // times and rejoining the results

            /*
             * XmlDoc.Element def = template.element("metadata/definition");
             * XmlDoc.Attribute att = def.attribute("group"); if (att!=null) {
             * def.remove(att); }
             */
            dm.add(template);
        }
        dm.add("pdist", 0); // Force local on whatever server it's executed
        XmlDoc.Element r = executor().execute(dID.getServerRouteObject(), "asset.get", dm.root());

        Collection<XmlDoc.Element> mes = r.elements("asset/meta/metadata");
        if (mes != null) {
            for (XmlDoc.Element me : mes) {
                w.add(me);
            }
        }
    }

    /**
     * 
     * @param w
     * @param dID
     *            The identity of the ExMethod
     * @param stepPath
     * @param mns
     * @param sas
     * @throws Throwable
     */
    private void describeStudyActions(XmlWriter w, DistributedAsset dID, String stepPath, String mns,
            List<XmlDoc.Element> sas) throws Throwable {
        if (sas == null) {
            return;
        }

        for (XmlDoc.Element se : sas) {
            Vector<XmlDoc.Element> templates = null;

            Collection<XmlDoc.Element> mes = se.elements("metadata");
            if (mes != null) {
                XmlDoc.Element te = new XmlDoc.Element("template");
                te.add(new XmlDoc.Attribute("ns", mns));
                for (XmlDoc.Element me : mes) {
                    te.add(me);
                }

                if (templates == null) {
                    templates = new Vector<XmlDoc.Element>(sas.size());
                }

                templates.add(te);
            }

            // templates is essentially a copy of mes with extra root "template"
            // and the namespace added to that
            w.push("study");
            w.add("type", se.value("type"));
            XmlDoc.Element dicom = se.element("dicom");
            if (dicom != null)
                w.add(dicom);

            if (templates != null) {

                // This function always returns null as it's not implemented.
                // Not really sure
                // what Jason intended and what he means by 'templates'
                DistributedAsset studyId = findStudyForMethodStep(dID, stepPath);

                if (studyId == null) {

                    // This process then throws away the extra 'templates -ns'
                    // root
                    // and we end back with what was essentially mes again
                    for (int i = 0; i < templates.size(); i++) {
                        XmlDoc.Element te = templates.get(i);
                        mes = te.elements("metadata");
                        for (XmlDoc.Element me : mes) {
                            describeDocTypeAsTemplate(w, mns, me);
                        }
                    }
                } else {
                    // We never get in here as studyID is always null
                    // This function is not 'group' aware
                    describeAssetMetadata(w, studyId, mns, templates);
                }
            }

            w.pop();
        }
    }

    /**
     * I wonder what this is for....
     * 
     * @param mid
     * @param stepPath
     * @return
     * @throws Throwable
     */
    private DistributedAsset findStudyForMethodStep(DistributedAsset mid, String stepPath) throws Throwable {
        // String where = "cid in '" + mid + "' and xpath(daris:pssd-study[@step='" +
        // stepPath + "']/method)='" + mid + "'";

        // TODO --

        return null;
    }

    private void describeDocTypeAsTemplate(XmlWriter w, String mns, XmlDoc.Element te) throws Throwable {
        XmlDoc.Element ve = te.element("value");
        String req = te.value("definition/@requirement");
        String dtype = te.value("definition");
        String group = te.value("definition/@group"); // Allows Doc Types to be
                                                      // grouped, say in the
                                                      // portal
        String ns = ExMethod.addGroupToMetaNamespace(mns, group);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", dtype);
        dm.push("template");
        dm.add(ve);
        dm.pop();

        // We are just going to get the document type locally and assume they
        // are the
        // everywhere in the federation (brave assumption)
        // The group attribute migrates into the added namespace of form
        // exmethod_step_group
        XmlDoc.Element r = executor().execute("asset.doc.type.describe", dm.root());
        w.push("metadata", new String[] { "type", dtype, "ns", ns, "requirement", req }); // Handles
                                                                                          // nulls
        String label = r.value("type/label");
        String desc = r.value("type/description");
        boolean editable = r.booleanValue("type/can-publish", false);

        XmlDoc.Element de = r.element("type/definition");

        w.add("label", label);
        if (desc != null) {
            w.add("description", desc);
        }

        w.add(de);

        w.add("editable", editable);

        w.pop();
    }
}
