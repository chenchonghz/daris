package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.object.Attachment;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentRemove extends PluginService {
	private Interface _defn;

	public SvcObjectAttachmentRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable identity of the object.", 1, 1));
		_defn.add(new Interface.Element("aid", AssetType.DEFAULT, "The asset id of the attachment to be removed.", 0,
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

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String cid = args.value("id");
		Collection<String> aids = args.values("aid");
		Attachment.destroy(executor(), cid, aids);
	}

}
