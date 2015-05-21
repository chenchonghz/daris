package daris.client.model.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ActorSelfHaveRole extends ObjectMessage<Boolean> {

	private String _role;

	public ActorSelfHaveRole(String role) {
		_role = role;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("role", new String[] { "type", "role" }, _role);
	}

	@Override
	protected String messageServiceName() {
		return "actor.self.have";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			if (xe.element("role") != null) {
				return xe.booleanValue("role");
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}

}
