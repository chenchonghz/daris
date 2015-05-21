package nig.mf.plugin.pssd.services;

/**
 * Specialised service to replace the :template meta-data (specified by an ExMethod) on a Study 
 * This is an Admin task when the Study specification part of a Method changes 
 * 
 * In the current PSSD implementation, only Subject and Study objects incur a :template
 * 
 */

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcStudyTemplateReplace extends PluginService {
    private Interface _defn;

    public SvcStudyTemplateReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the Study (managed by the local server). ", 1, 1));

    }

    public String name() {
        return "om.pssd.study.method.template.replace";
    }

    public String description() {
        return "Replaces the :template meta-data on a Study with new supplied from the parent ExMethod.  Administration operation if an ExMethod is changed and the Study :template meta-data needs to be kept in step.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


        // Set distributed citeable ID for the local SUbject
        DistributedAsset dSID = new DistributedAsset(args.element("id"));

        // Check the Study
        PSSDObject.Type sType = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID));
        if (sType == null) {
            throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
        }
        if (!sType.equals(Study.TYPE)) {
            throw new Exception("Object " + dSID.getCiteableID() + " [type=" + sType + "] is not a " + Study.TYPE);
        }
        if (dSID.isReplica()) {
            throw new Exception("The supplied Study is a replica and this service cannot modify it.");
        }

        // Fetch the CID of the parent ExMethod.
        String mid = CiteableIdUtil.getParentId(dSID.getCiteableID());
        DistributedAsset dEID = new DistributedAsset(null, mid);

        // Find the step Path that created this Study
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", dSID.getCiteableID());
        XmlDoc.Element r = executor().execute("om.pssd.object.describe", dm.root());
        String step = r.value("object/method/step");

        // Generate template meta-data
        dm = new XmlDocMaker("args");
        dm.add("cid", dSID.getCiteableID());
        String etype = Study.addMethodStudyTemplates(executor(), dm, dEID, step);
        XmlDoc.Element t = dm.root().element("template");

        // Either replace or remove
        if (t == null) {
            executor().execute("asset.template.remove", dm.root());
        } else {
            executor().execute("asset.template.set", dm.root());
        }
    }
}
