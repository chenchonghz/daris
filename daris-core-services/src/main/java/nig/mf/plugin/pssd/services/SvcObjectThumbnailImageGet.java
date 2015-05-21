package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.object.Thumbnail;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectThumbnailImageGet extends PluginService {

	private Interface _defn;

	public SvcObjectThumbnailImageGet() {
		_defn = new Interface();
		Interface.Element id = new Interface.Element("id", AssetType.DEFAULT, "The asset id of the thumbnail archive.",
				1, 1);
		id.add(new Interface.Attribute("idx", IntegerType.POSITIVE, "The index of the image in the thumbnail archive.",
				1));
		_defn.add(id);
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
		return "Retrieve the thumnail image from the thumbnail asset.";
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

	@Override
	public void execute(Element args, Inputs arg1, Outputs outputs, XmlWriter w) throws Throwable {
		String assetId = args.value("id");
		int idx = args.intValue("id/@idx");
		Thumbnail.getImage(executor(), assetId, idx, outputs);
	}

	@Override
	public String name() {
		return "om.pssd.object.thumbnail.image.get";
	}

}
