package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
/**
 * Specialised service to replace the :template meta-data (specified by a Method) on a Subject 
 * This is an Admin task when the Subject specification part of a Method changes 
 * 
 * In the current PSSD implementation, only Subject and Study objects incur a :template
 * 
 * TBD: This service should be complemented by the om.pssd.study.method.template.replace
 * 
 */
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.PluginService;

import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcSubjectMethodTemplateReplace extends PluginService {
    private Interface _defn;

    public SvcSubjectMethodTemplateReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the Subject (managed by the local server). ", 1, 1));
        _defn.add(new Interface.Element("mid", CiteableIdType.DEFAULT,
                "The identity of the Method (managed by the local server). ", 1, 1));
    }

    public String name() {
        return "om.pssd.subject.method.template.replace";
    }

    public String description() {
        return "Replaces the :template meta-data on a Subject with new supplied from the specified Method. Only operates on primary objects. Administration operation if a Method is changed and the Subject :template meta-data needs to be kept in step.";
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
        if (dSID.isReplica()) {
        	throw new Exception ("This service cannot operate on replica objects");
        }
        // In principe a subject could be made with a replica Method... 
        DistributedAsset dMID = new DistributedAsset(args.element("mid"));
        if (dMID.isReplica()) {
        	throw new Exception ("This service cannot operate on replica objects");
        }


        // Check the Subject
        PSSDObject.Type sType = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID));
        if (sType == null) {
            throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
        }
        if (!sType.equals(Subject.TYPE)) {
            throw new Exception("Object " + dSID.getCiteableID() + " [type=" + sType + "] is not a " + Subject.TYPE);
        }
        if (dSID.isReplica()) {
            throw new Exception("The supplied Subject is a replica and this service cannot modify it.");
        }

        // Check the Subject
        PSSDObject.Type mType = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dMID));
        if (mType == null) {
            throw new Exception("The asset associated with " + dMID.toString() + " does not exist");
        }
        if (!mType.equals(Method.TYPE)) {
            throw new Exception("Object " + dMID.getCiteableID() + " [type=" + mType + "] is not a " + Method.TYPE);
        }

        // Check this Subject was made with this Method
        // TBD: allow over-ride
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", dSID.getCiteableID());
        XmlDoc.Element r = executor().execute("om.pssd.object.describe", dm.root());
        String mid = r.value("object/method/id");
        if (!mid.equals(dMID.getCiteableID())) {
            throw new Exception("The Subject " + dSID.getCiteableID() + " was not created with the Method "
                    + dMID.getCiteableID());
        }

        // Fetch the Method meta-data for subject creation
        dm = new XmlDocMaker("args");
        dm.add("id", dMID.getCiteableID());
        r = executor().execute("om.pssd.method.describe", dm.root());

        // Get public and private components
        XmlDoc.Element publicMeta = r.element("method/subject/project/public");
        XmlDoc.Element privateMeta = r.element("method/subject/project/private");

        // Build output meta-data
        XmlDocMaker dm2 = new XmlDocMaker("args");
        dm2.add("id", dSID.getAssetID());

        // Public
        boolean some = false;
        if (publicMeta != null) {
            dm2.push("template", new String[] { "ns", "pssd.public" });
            Collection<XmlDoc.Element> els = publicMeta.elements("metadata");
            for (XmlDoc.Element el : els) {
                dm2.add(el);
            }
            dm2.pop();
            some = true;
        }

        if (privateMeta != null) {
            dm2.push("template", new String[] { "ns", "pssd.private" });
            Collection<XmlDoc.Element> els = privateMeta.elements("metadata");
            for (XmlDoc.Element el : els) {
                dm2.add(el);
            }
            dm2.pop();
            some = true;
        }

        // Either set or replace
        if (!some) {
            dm2 = new XmlDocMaker("args");
            dm2.add("id", dSID.getAssetID());
            executor().execute("asset.template.remove", dm2.root());
        } else {
            executor().execute("asset.template.set", dm2.root());
        }
    }
}
