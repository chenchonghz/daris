package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.pssd.Role;

public class SvcDICOMAEAccess extends PluginService {
	
	
	private Interface _defn;

	public SvcDICOMAEAccess() {
		_defn = new Interface();
	}


	
	public String name() {
		return "om.pssd.dicom.ae.access";
	}

	public String description() {
		return "Returns the DIOCOM AE access type that the calling user is allowed to create.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
	
		ServerRoute route = null;
		if (ModelUser.hasRole(route, executor(), Role.objectAdminRoleName()) ||
				ModelUser.hasRole(route, executor(), Role.powerModelUserRoleName())) {
			w.add("access", "public");
		}
		if (ModelUser.hasRole(route, executor(), Role.modelUserRoleName())  || 
				ModelUser.hasRole(route, executor(), Role.objectAdminRoleName())) {     // daris:pssd.object.admin does not hold daris:pssd.model.user role
			w.add("access", "private");
		}
	}
}
