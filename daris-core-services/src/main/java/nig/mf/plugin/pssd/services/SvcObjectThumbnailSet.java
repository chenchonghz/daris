package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.object.Thumbnail;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectThumbnailSet extends PluginService {

	private Interface _defn;

	public SvcObjectThumbnailSet() {
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
		return "Uploads a zip archive that contains thumbnail images to be the thumbnail asset of the specified object.";
	}

	@Override
	public int minNumberOfInputs() {
		return 1;
	}

	@Override
	public int maxNumberOfInputs() {
		return 1;
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String cid = args.value("id");
		Thumbnail.set(executor(), cid, inputs.input(0).stream());
	}

	@Override
	public String name() {
		return "om.pssd.object.thumbnail.set";
	}

}
