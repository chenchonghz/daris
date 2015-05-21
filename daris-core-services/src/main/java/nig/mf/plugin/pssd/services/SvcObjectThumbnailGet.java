package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.object.Thumbnail;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectThumbnailGet extends PluginService {

	private Interface _defn;

	public SvcObjectThumbnailGet() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable id of the object.", 1, 1));
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
		return "Retrieves meta data and/or content for the thumbnail asset of the specified object.";
	}

	@Override
	public int minNumberOfOutputs() {
		return 0;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String cid = args.value("id");
		
		Thumbnail.get(executor(), cid, outputs, w);
	}

	@Override
	public String name() {
		return "om.pssd.object.thumbnail.get";
	}

}
