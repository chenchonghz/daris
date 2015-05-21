package nig.mf.plugin.pssd.services;



import nig.iio.metadata.XMLUtil;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.Subject;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;



public class SvcSubjectClone extends PluginService {


	private Interface _defn;

	public SvcSubjectClone() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the local Subject to clone. ", 1, 1));
		//
		_defn.add(new Interface.Element("subject-number", IntegerType.POSITIVE_ONE,
				"Specifies the subject number for the identifier. If not given, the next available subject is created. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing subjects from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the subject-number is not given, fill in the Subject allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to true; use with care in federated envionment.", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
				"Specifies the number of clones you want. Defaults to 1. If the 'subject-number' argument is supplied. then 'number' is forced to 1", 0, 1));
	}


	public String name() {
		return "om.pssd.subject.clone";
	}

	public String description() {
		return "Clone the subject including all ACLs and meta-data.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}


	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// We can only clone local subjects.
		DistributedAsset dSID = new DistributedAsset (null, args.value("id"));

		// Validate
		PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID));
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied Subject is a replica and this service cannot clone it.");
		}
		if (!dSID.isLocal()) {
			throw new Exception("The supplied subject is hosted by a remote server, cannot clone it");
		}

		// If the user does not give subject-number,  we may want to fill in 
		// any holes in the allocator space for subjects as sometimes we use 
		// re-use projects CIDs
		XmlDoc.Element subjectNumber = args.element("subject-number");		
		XmlDoc.Element fillIn = args.element("fillin");
		Integer nClones = args.intValue("size", 1);
		if (subjectNumber!=null) nClones = 1;

		// Get Parent Project
		DistributedAsset dPID = dSID.getParentProject (false);
		if (!dPID.isLocal()) {
			throw new Exception("The supplied subject is hosted by a remote server, cannot clone it");
		}


		// Because the Subject is going to be created locally, the user must have 
		// the Subject admin role locally as well.  The roles have to be distributed.
		Boolean isAdmin = (ModelUser.hasRole(null, executor(), Project.subjectAdministratorRoleName(dPID.getCiteableID())) ||
				ModelUser.hasRole(null, executor(), Role.objectAdminRoleName()));
		if (!isAdmin) {
			throw new Exception ("User not authorised: requires '" + Project.subjectAdministratorRoleName(dPID.getCiteableID()) +
					"' or '" + Role.objectAdminRoleName() + " role on local server"); 
		}

		// CLone it
		clone (executor(), dPID.getCiteableID(), dSID.getCiteableID(), fillIn, subjectNumber, nClones, w);
	}


	private void clone (ServiceExecutor executor, String pid, String sid, XmlDoc.Element fillIn, XmlDoc.Element subjectNumber, Integer nClones, XmlWriter w) throws Throwable {

		// Fetch the meta-data from the existing subject
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", sid);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		String mid = r.value("object/method/id");
		XmlDoc.Element pub = r.element("object/public");
		XmlDoc.Element priv = r.element("object/private");
		//
		XmlDocMaker dm2 = new XmlDocMaker("args");
		dm2.add("pid", pid);
		dm2.add("method", mid);
		if (fillIn!=null) dm2.add(fillIn);
		if (subjectNumber != null) dm2.add(subjectNumber);
		//
		if (pub!=null) {
			XMLUtil.removeAttribute(pub, "id");
			dm2.add(pub);
		}
		if (priv!=null) {
			XMLUtil.removeAttribute(priv, "id");
			dm2.add(priv);
		}

		// Do it
		for (int i=0; i<nClones; i++) {
			XmlDoc.Element r2 = executor.execute("om.pssd.subject.create", dm2.root());
			if (r2!=null) {
				w.add(r2.element("id"));
			}
		}
	}
}

