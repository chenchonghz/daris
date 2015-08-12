package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentRemove extends PluginService {
	private Interface _defn;

	public SvcObjectAttachmentRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The citeable identity of the object.", 1, 1));
		_defn.add(new Interface.Element("aid", AssetType.DEFAULT,
				"The asset id of the attachment to be removed.", 0,
				Integer.MAX_VALUE));
	}

	public String name() {
		return "om.pssd.object.attachment.remove";
	}

	public String description() {
		return "Removes one or more attachments from the specified object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		String cid = args.value("id");
		String assetId = executor().execute("asset.get",
				"<args><cid>" + cid + "</cid></args>", null, null).value(
				"asset/@id");
		Collection<String> attachmentAssetIds = args.values("aid");
		if (attachmentAssetIds != null) {
			for (String aaid : attachmentAssetIds) {
				executor().execute(
						"asset.relationship.remove",
						"<args><id>" + assetId
								+ "</id><to relationship=\"attachment\">" + aaid
								+ "</to></args>", null, null);
				executor().execute("asset.hard.destroy",
						"<args><id>" + aaid + "</id></args>", null, null);
			}
		}
		// Attachment.destroy(executor(), cid, aids);
	}

}
