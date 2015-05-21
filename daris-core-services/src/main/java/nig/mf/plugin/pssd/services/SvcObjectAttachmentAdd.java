package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.object.Attachment;
import nig.mf.plugin.pssd.object.RelatedAsset.IfExists;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentAdd extends PluginService {

	private Interface _defn;

	public SvcObjectAttachmentAdd() {

		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the local object to attach to.",
				1, 1));
		Interface.Element ae = new Interface.Element("attachment", XmlDocType.DEFAULT, "The attachement to attach", 1,
				1);
		ae.add(new Interface.Element("name", StringType.DEFAULT, "The name of the attachment file.", 1, 1));
		ae.add(new Interface.Element("description", StringType.DEFAULT, "The description of the attachment file.", 0, 1));
		_defn.add(ae);
		_defn.add(new Interface.Element(
				"ifexists",
				IfExists.enumType(),
				"What to do if the attachment asset with the name exists. Sets to error, it will throw an exception if the asset with the name already exists. Sets to rename, it will make a unique name for the asset, if the name has been used by other existing asset. Defaults to rename.",
				0, 1));

	}

	public String name() {

		return "om.pssd.object.attachment.add";
	}

	public String description() {

		return "Attach local files as attachments to the specified PSSD object. The name and uri of the attachment file must be supplied.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public int minNumberOfInputs() {

		return 1;
	}

	public int maxNumberOfInputs() {

		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs inputs, Outputs out, XmlWriter w) throws Throwable {

		// Parse arguments
		String cid = args.value("id");
		IfExists ifExists = IfExists.fromString(args.stringValue("ifexists", IfExists.rename.toString()));
		XmlDoc.Element ae = args.element("attachment");

		//
		if (ifExists.equals(IfExists.error)) {
			String name = ae.value("name");
			if (Attachment.exists(executor(), null, cid, name)
					|| Asset.exists(executor(), Asset.getNamespaceByCid(executor(), null, cid), name)) {
				throw new Exception("An asset of name='" + name + "' already exists. Failed to add attachment.");
			}
		}

		// Create the attachment asset
		String name = ae.value("name");
		String description = ae.value("description");
		Attachment.create(executor(), cid, name, description, inputs.input(0), ifExists, w);
	}

}
