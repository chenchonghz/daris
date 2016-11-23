package daris.essentials;

import nig.mf.plugin.util.AssetUtil;
import nig.util.DateUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcLicenceUsageDescribe extends PluginService {

	private Interface _defn;

	public SvcLicenceUsageDescribe()  throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("namespace", StringType.DEFAULT, "Namespace where the licence tracking asset is located.", 1, 1));
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public Interface definition() {
		return _defn;

	}

	public String description() {
		return "Describe tracked licence usage asset (created by servcie nig.licence.usage).";
	}

	public String name() {
		return "nig.licence.usage.describe";
	}

	public boolean canBeAborted() {
		return false;
	}

	@Override
	public int minNumberOfOutputs() {
		return 0;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 0;
	}



	public void execute(Element args, Inputs inputs, Outputs outputs, final XmlWriter w) throws Throwable {

		String nameSpace = args.value("namespace");
		

		// FInd the licence usage asset
		String id = SvcLicenceUsage.findLicenceUsageAsset(executor(), nameSpace, SvcLicenceUsage.LICENCE_USAGE_ASSET_NAME);
		if (id==null) {
			throw new Exception ("Asset of name '" + SvcLicenceUsage.LICENCE_USAGE_ASSET_NAME + 
					"' could not be found in specified namespace '" + nameSpace + "'");
		}
	
		// Get the meta-data
		XmlDoc.Element asset = AssetUtil.getAsset(executor(), null, id);
		if (asset==null) return;
		XmlDoc.Element meta = asset.element("asset/meta/" + SvcLicenceUsage.LICENCE_USAGE_DOCTYPE);
		if (meta==null) return;
		w.addAll(meta.elements());

	}

}
