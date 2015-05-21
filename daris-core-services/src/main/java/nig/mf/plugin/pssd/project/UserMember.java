package nig.mf.plugin.pssd.project;

import java.util.ArrayList;
import java.util.List;

import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.pssd.ProjectRole.Type;
import arc.xml.XmlWriter;

public class UserMember extends ProjectMember {

	private UserCredential _uc;

	public UserMember(String id, UserCredential uc, String projectId, Type type, DataUse dataUse) {

		super(id, projectId, type, dataUse);
		_uc = uc;
	}

	public UserMember(String id, String authority, String protocol, String domain, String user, String projectId,
			Type type, DataUse dataUse) {

		this(id, new UserCredential(authority, protocol, domain, user), projectId, type, dataUse);
	}

	public UserCredential userCredential() {

		return _uc;
	}

	@Override
	public void describe(XmlWriter w) throws Throwable {

		List<String> attributes = new ArrayList<String>();
		if (id() != null) {
			attributes.add("id");
			attributes.add(id());
		}
		if (_uc.authority() != null) {
			attributes.add("authority");
			attributes.add(_uc.authorityName());
			if (_uc.authorityProtocol() != null) {
				attributes.add("protocol");
				attributes.add(_uc.authorityProtocol());
			}
		}
		attributes.add("domain");
		attributes.add(_uc.domain());
		attributes.add("user");
		attributes.add(_uc.user());
		attributes.add("role");
		attributes.add(role().type().toString());
		if (dataUse() != null) {
			attributes.add("data-use");
			attributes.add(dataUse().toString());
		}
		String[] attrs = new String[attributes.size()];
		attributes.toArray(attrs);
		w.add("member", attrs);
	}

}
