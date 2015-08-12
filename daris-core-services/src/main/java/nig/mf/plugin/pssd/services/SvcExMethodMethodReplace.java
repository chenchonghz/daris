package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class SvcExMethodMethodReplace extends PluginService {
    private Interface _defn;

    public SvcExMethodMethodReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the ExMethod (managed by the local server).", 1, 1));
        _defn.add(new Interface.Element(
                "method",
                CiteableIdType.DEFAULT,
                "The identity of the new Method (managed by the local server) to replace the old. Can be the same as the existing Method (so if the Method has changed, this will update the ExMethod).",
                1, 1));
        _defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
                "Set to true will replace the method for the children Studies. Defaults to false.", 1, 1));
    }

    public String name() {
        return "om.pssd.ex-method.method.replace";
    }

    public String description() {
        return "Specialized service function to replace the Method in an ExMethod and optionally its children Studies (see om.pssd.study.method.template.replace); replaces the daris:pssd-method document and pssd-exmethod/method components.  Must be done in conjuction with changes to the related Project and Subjects. Only operates on primary objects an dtheir children.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

	Boolean recursive = args.booleanValue("recursive", false);

        // Set distributed citeable ID for the local ExMethod.
        DistributedAsset eID = new DistributedAsset(args.element("id"));
        if (eID.isReplica()) {
            throw new Exception("The supplied ExMethod is a replica and this service cannot modify it.");
        }

        // Find primary parent Project
        Boolean readOnly = false;
        DistributedAsset dPID = eID.getParentProject(readOnly);
        if (dPID == null) {
            throw new Exception("Cannot find primary Project parent of the given ExMethod");
        }

        // Modifier must have administrator role locally
        Boolean isAdmin = (ModelUser.hasRole(null, executor(),
                Project.projectAdministratorRoleName(dPID.getCiteableID())) || ModelUser.hasRole(null, executor(),
                Role.objectAdminRoleName()));
        if (!isAdmin) {
            throw new Exception("User not authorised: requires '"
                    + Project.projectAdministratorRoleName(dPID.getCiteableID()) + "' or '"
                    + Role.objectAdminRoleName() + " role");
        }

        // Check a few things...
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), eID));
        if (type == null) {
            throw new Exception("The asset associated with CID " + eID.toString() + " does not exist");
        }
        if (!type.equals(ExMethod.TYPE)) {
            throw new Exception("Object " + eID.getCiteableID() + " [type=" + type + "] is not a " + ExMethod.TYPE);
        }
 
        // NOw the Method
        DistributedAsset mID = new DistributedAsset(args.element("method"));

        // Check a few things...
        type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), mID));
        if (type == null) {
            throw new Exception("The asset associated with CID " + mID.toString() + " does not exist");
        }
        if (!type.equals(Method.TYPE)) {
            throw new Exception("Object " + eID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
        }
        if (mID.isReplica()) {
            throw new Exception("The supplied Method is a replica; this service only consumes primary Methods.");
        }

        // replace in ExMethod
        replace(executor(), eID.getCiteableID(), mID);
        
        // Now replace the :template element of children Studies
        String pdist = "0";
        if (recursive) replaceInStudies (executor(), eID.getCiteableID(), pdist);
    }

    private void replaceInStudies (ServiceExecutor executor, String eID, String pdist) throws Throwable {
        Collection<String> children = PSSDUtils.children(executor, eID, pdist, 
        		DistributedQuery.ResultAssetType.primary,
                DistributedQuery.ResultFilterPolicy.none);
        if (children == null)
            return;
        
        // Update Study :template for children Studies
        for (String child : children) {
        	XmlDocMaker dm = new XmlDocMaker("args");
        	dm.add("id", child);
        	executor.execute("om.pssd.study.method.template.replace", dm.root());
        }

    }
    
    private void replace(ServiceExecutor executor, String eID, DistributedAsset mID) throws Throwable {
        // Get ExMethod XML
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", eID);
        XmlDoc.Element asset = executor().execute("asset.get", dm.root());

        // Remove daris:pssd-method
        XmlDoc.Element pssdMethodOld = asset.element("asset/meta/daris:pssd-method");
        if (pssdMethodOld != null) {

            // Remove daris:pssd-method from asset
            // Exception here is ok.
            AssetUtil.removeDocument(executor(), eID, null, pssdMethodOld);
        }

        // Instantiate new Method
        Method m = Method.lookup(executor, mID);

        // Take a copy, and fully instantiate the method to which we are
        // referring.
        // The server route is passed down requiring all sub-Methods
        // to be managed on the same server
        m.convertBranchesToSubSteps(mID.getServerRoute(), executor);

        // Save daris:pssd-method meta-data
        dm = new XmlDocMaker("args");
        dm.add("cid", eID);
        dm.push("meta", new String[] { "action", "replace" });
        dm.push("daris:pssd-object");
        dm.add("type", "ex-method");
        dm.add("name", m.name());
        dm.add("description", m.description());
        dm.pop();
        dm.push("daris:pssd-method");
        if (m.version() != null)
            dm.add("version", m.version());
        if (m.numberOfSteps() > 0) {
            XmlDocWriter w = new XmlDocWriter(dm);
            m.saveSteps(w);
        }
        dm.pop();

        // daris:pssd-ex-method/method)
        dm.push("daris:pssd-ex-method"); // There are no attribute (ns or tag)
        dm.push("method");
        dm.add("id", mID.getCiteableID());
        dm.add("name", m.name()); // mandatory
        dm.add("description", m.description()); // mandatory
        dm.pop();
        dm.pop();
        //
        dm.pop(); // meta

        // Update asset
        executor.execute("asset.set", dm.root());
    }
}
