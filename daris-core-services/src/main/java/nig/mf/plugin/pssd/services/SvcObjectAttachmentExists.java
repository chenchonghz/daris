package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.object.Attachment;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentExists extends PluginService {

	private Interface _defn;

	public SvcObjectAttachmentExists() {
		_defn = new Interface();
		Interface.Element id = new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable id of the object.", 1,
				1);
		id.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages the citable id.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(id);
		_defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of the attachment asset.", 1, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Checks if the attachment with the specified name exists.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String cid = args.value("id");
		String proute = args.value("id/@proute");
		String name = args.value("name");
		boolean exists = Attachment.exists(executor(), proute == null ? null : new ServerRoute(proute), cid, name);
		w.add("exists", exists);
	}

	@Override
	public String name() {
		return "om.pssd.object.attachment.exists";
	}

}
