package daris.essentials;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * 
 * @author Wilson Liu
 *
 */
public class SvcAssetClone extends PluginService {
	private Interface _defn;

	public SvcAssetClone() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID to clone.", 0, Integer.MAX_VALUE));
		_defn.add(new Element("namespace", StringType.DEFAULT, "Namespace to clone assets into.", 1, 1));

	}

	public String name() {
		return "nig.asset.clone";
	}

	public String description() {
		return "Clones assets. Any content is set by reference back to the original asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		Collection<String> ids = args.values("id");
		String namespace = args.value("namespace");
		clone (executor(), ids, namespace, w);
	}
	
	private  void clone (ServiceExecutor executor, Collection<String> ids, String namespace, XmlWriter w) throws Throwable {
		
		// CHeck destination namespace exists
		if (!AssetUtil.assetNameSpaceExists(executor, namespace)) {
			throw new Exception ("Asset namespace '" + namespace + "' does not exist");
		}
		
		// Iterate
		for (String id : ids) {
			String t = AssetUtil.cloneAsset (executor, id, namespace, true);
			w.add("id", new String[]{"cloned", id}, t);
		}
	}
}