package daris.client.mf;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ServerVersion extends ObjectMessage<String> {

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {
		return "server.version";
	}

	@Override
	protected String instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			return xe.value("version");
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
