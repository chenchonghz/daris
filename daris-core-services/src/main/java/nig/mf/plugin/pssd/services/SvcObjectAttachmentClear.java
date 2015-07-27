package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentClear extends PluginService {

	private Interface _defn;

	public SvcObjectAttachmentClear() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The citeable id of the object.", 1, 1));
	}

	@Override
	public Access access() {
		return ACCESS_MODIFY;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Clear all attachments from the specified object.";
	}

	@Override
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3)
			throws Throwable {
		String cid = args.value("id");
		XmlDoc.Element ae = executor().execute("asset.get",
				"<args><cid>" + cid + "</cid></args>", null, null).element(
				"asset");
		String assetId = ae.value("@id");
		List<XmlDoc.Element> attachments = ae
				.elements("related[@type='attachment']/to");
		if (attachments != null) {
			for (XmlDoc.Element attachment : attachments) {
				String aaid = attachment.value();
				boolean exists = attachment.booleanValue("@exists", true);
				executor().execute(
						"asset.relationship.remove",
						"<args><id>" + assetId
								+ "</id><to relationship=\"attachment\">"
								+ aaid + "</to></args>", null, null);
				if (exists) {
					executor().execute("asset.hard.destroy",
							"<args><id>" + aaid + "</id></args>", null, null);
				}
			}
		}

		// Attachment.clear(executor(), cid);
	}

	@Override
	public String name() {
		return "om.pssd.object.attachment.clear";
	}

}
