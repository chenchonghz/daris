package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.object.Attachment;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentClear extends PluginService {

	private Interface _defn;

	public SvcObjectAttachmentClear() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable id of the object.", 1, 1));
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
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
		String cid = args.value("id");
		Attachment.clear(executor(), cid);
	}

	@Override
	public String name() {
		return "om.pssd.object.attachment.clear";
	}

}
