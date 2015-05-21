package daris.client.model.type.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.type.MimeTypeRef;
import arc.mf.object.ObjectMessage;

public class TypeList extends ObjectMessage<List<MimeTypeRef>> {

	private String _stype;

	public TypeList(String stype) {
		_stype = stype;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		if (_stype != null) {
			w.add("stype", _stype);
		}
	}

	@Override
	protected String messageServiceName() {
		return "type.list";
	}

	@Override
	protected List<MimeTypeRef> instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			List<XmlElement> tes = xe.elements("type");
			if (tes != null && !tes.isEmpty()) {
				List<MimeTypeRef> types = new ArrayList<MimeTypeRef>(tes.size());
				for (XmlElement te : tes) {
					types.add(new MimeTypeRef(te.value()));
				}
				return types;
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return "Mime Type List";
	}

	@Override
	protected String idToString() {
		return null;
	}

}
