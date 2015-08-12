package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


/**
 * Specialised service function utilised when the Method structure changed to
 * include DICOM modality and when Study types were changed.
 * 
 * For the given Project CID Finds all Methods registered Updates the ExMethod
 * objects with a new instantiation of the pre-existing Method Updates the Study object with
 * the new Study type, if it has changed.
 * 
 * TBD: handle :template specification changes when available (for Subjects and
 * Studies)
 * 
 * @author nebk
 * 
 */
public class SvcProjectMethodReplace extends PluginService {
    private Interface _defn;

    public SvcProjectMethodReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the Project (managed by the local server). Defaults to all Projects.", 0, 1));
    }

    public String name() {
        return "om.pssd.project.method.replace";
    }

    public String description() {
        return "Specialized management service to refresh the existing Methods registered in a Project and relevant children objects (Subject, ExMethod, Study). \n" +
               "It will replace :template (Subject and Study)  and :meta elements (Subject, ExMethod) as needed.  Assumes: \n " +
               " 1) The Method(s), referred to by the Project's Subjects have been updated with new internals, \n" +
               " 2) The new Methods are compatible with the old (or if not you will remediate separately). For example, \n" +
               "    The meta-data set on the Subject must be consistent with that specified in the new Method. If not you must remediate separately. \n" +
               " 3) If the study types in the Method may have changed there must be a  1 to 1 correspondence between \n" +
               "   the steps in the old and new Methods so the types in existing Studies can be updated.  \n" +
               "   If the Method gets new (as yet unused steps at the end that is ok)";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String id = args.value("id");
        if (id == null) {

            // FInd all Primary projects. Replicas must be fixed where they are
            // primaries !
            Collection<String> projects = PSSDUtils.children(executor(), null, "0",
                    DistributedQuery.ResultAssetType.primary, DistributedQuery.ResultFilterPolicy.none);
            if (projects == null)
                return;
            //
            for (String project : projects) {
                updateProject(executor(), project);
            }
        } else {

            // Set distributed citeable ID for the local Project
            DistributedAsset pID = new DistributedAsset(args.element("id"));

            // Check a few things...
            PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), pID));
            if (type == null) {
                throw new Exception("The asset associated with CID " + pID.toString() + " does not exist");
            }
            if (!type.equals(Project.TYPE)) {
                throw new Exception("Object " + pID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
            }
            if (pID.isReplica()) {
                throw new Exception("The supplied Project is a replica and this service cannot modify it.");
            }

            // Do the work
            updateProject(executor(), pID.getCiteableID());
        }
    }

    private void updateProject(ServiceExecutor executor, String pID) throws Throwable {

    	// There is nothing to do for the Project object.
    	// Subject
    	//   :template and :meta
    	// ExMethod
    	//   :meta
    	// Study
    	//   :template
        // Iterate over local and primary subjects
        Collection<String> children = PSSDUtils.children(executor, pID, "0", DistributedQuery.ResultAssetType.primary,
                DistributedQuery.ResultFilterPolicy.none);
        if (children == null)
            return;
        for (String child : children) {
            updateSubject(executor(), child);
        }
    }

    private void updateSubject(ServiceExecutor executor, String id) throws Throwable {

        // Get the method used to create this subject.
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
        String mid = r.value("object/method/id");

        // DOn't want to throw exceptions once we are going.
        if (mid == null) return;

        
        // Update Subject and its children
        dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("method", mid);
        dm.add("recursive", true);
        executor.execute("om.pssd.subject.method.replace", dm.root());
    }
}
