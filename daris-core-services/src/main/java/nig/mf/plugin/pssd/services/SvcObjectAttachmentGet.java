package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.object.Attachment;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentGet extends PluginService {
	private Interface _defn;

	public static final int BUFFER_SIZE = 2048;

	public SvcObjectAttachmentGet() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable id of the object.", 1,
				1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages the citable id.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);
		me = new Interface.Element(
				"aid",
				AssetType.DEFAULT,
				"The identity of the attachment asset. if not specified, all the attachments to the object will be packaged into a zip archive.",
				0, Integer.MAX_VALUE);
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.object.attachment.get";
	}

	public String description() {
		return "Retrieves the data for a specified object attachment.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public int minNumberOfOutputs() {
		return 1;
	}

	public int maxNumberOfOutputs() {
		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs outputs, XmlWriter w) throws Throwable {

		String cid = args.value("id");
		String proute = args.value("id/@proute");
		ServerRoute sroute = proute == null ? null : new ServerRoute(proute);
		Collection<String> aids = args.values("aid");
		if (aids == null || aids.isEmpty()) {
			aids = Attachment.findAll(executor(), sroute, cid);
			if (aids == null) {
				throw new Exception("Object (id=" + cid + ") does not have related attachment assets.");
			}
		}
		Attachment.get(executor(), sroute, aids, outputs);
	}

}
