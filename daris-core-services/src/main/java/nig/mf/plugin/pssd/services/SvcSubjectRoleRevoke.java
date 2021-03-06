package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcSubjectRoleRevoke extends PluginService {
	private Interface _defn;

	public SvcSubjectRoleRevoke() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the RSubject.", 1, 1));
		_defn.add(new Interface.Element("role",new EnumType(new String[] { "administrator", "guest" }), "The role bestowed on the user.", 1, 1));
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		_defn.add(new Interface.Element("domain",StringType.DEFAULT, "The authentication domain.", 1, 1));
		_defn.add(new Interface.Element("user",StringType.DEFAULT, "The authentication user within the domain.", 1, 1));
	}

	public String name() {
		return "om.pssd.subject.role.revoke";
	}

	public String description() {
		return "Revokes an RSubject-specific role from a local user.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		String id     = args.value("id");
		String role   = args.value("role");
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");
		String user   = args.value("user");
		
		DistributedAsset dID = new DistributedAsset(null, id);
		PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID));
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(RSubject.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + RSubject.TYPE);
		}

		if ( role.equalsIgnoreCase("administrator") ) {
			role = RSubject.administratorRoleName(id);
		} else if ( role.equalsIgnoreCase("guest") ) {
			role = RSubject.guestRoleName(id);
		} 

		XmlDocMaker dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		dm.add("domain",domain);
		dm.add("user",user);
		dm.add("role",new String[] { "type", "role" },role);
		
		// Revoke from local user
		executor().execute("user.revoke",dm.root());
	}
}
