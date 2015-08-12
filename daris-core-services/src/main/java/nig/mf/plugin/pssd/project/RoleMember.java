package nig.mf.plugin.pssd.project;

import java.util.ArrayList;
import java.util.List;

import nig.mf.pssd.ProjectRole.Type;
import arc.xml.XmlWriter;

public class RoleMember extends ProjectMember {

	private String _member;

	protected RoleMember(String id, String member, String projectId, Type memberType, DataUse dataUse) {

		super(id, projectId, memberType, dataUse);
		_member = member;
	}

	public String member() {

		return _member;
	}

	@Override
	public void describe(XmlWriter w) throws Throwable {

		List<String> attributes = new ArrayList<String>();
		if (id() != null) {
			attributes.add("id");
			attributes.add(id());
		}
		attributes.add("member");
		attributes.add(member());
		attributes.add("role");
		attributes.add(role().type().toString());
		if (dataUse() != null) {
			attributes.add("data-use");
			attributes.add(dataUse().toString());
		}
		String[] attrs = new String[attributes.size()];
		attributes.toArray(attrs);
		w.add("role-member", attrs);
	}
}
