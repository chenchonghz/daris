package nig.mf.plugin.pssd.services;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDatasetNameGrabFromFileName extends PluginService {
	private Interface _defn;

	public SvcDatasetNameGrabFromFileName() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset ID of the local PSSD dataset (holding DICOM data)", 0, 1));

		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citeable ID of the local PSSD dataset (holding DICOM data)", 0, 1));

		_defn.add(new Element("overwrite", BooleanType.DEFAULT,
						"Set to true to overwrite the existing daris:pssd-object/name. Defaults to false.",
						0, 1));


	}

	public String name() {
		return "om.pssd.dataset.name.grab.from.filename";
	}

	public String description() {
		return "This service sets the name from the filename held in daris:pssd-filename/original";
	}
	
	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");
		String cid = args.value("cid");
		boolean overwrite = args.booleanValue("overwrite", false);

		if (id == null && cid == null) {
			throw new Exception("You need to specify either id or cid.");
		}
		if (id != null && cid != null) {
			throw new Exception("You should not specify both id and cid.");
		}
		if (cid==null) cid = AssetUtil.getCid(executor(), id);
		if (PSSDUtil.isReplica(executor(), cid)) {
			throw new Exception ("This object is a replica. Cannot modify");
		}

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0);    // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		id = r1.value("asset/@id");
		cid = r1.value("asset/cid");
		if (cid == null) {
			throw new Exception("No asset/cid found. Asset(id=" + id
					+ ") is not a valid PSSD asset.");
		}
		if (r1.value("asset/model") == null) {
			throw new Exception("No asset/model found. Asset(id=" + id
					+ ") is not a valid PSSD asset.");
		}
		if (!r1.value("asset/model").equals("om.pssd.dataset")) {
			throw new Exception("Wrong asset/model. Asset(id=" + id
					+ ") is not a valid PSSD dataset asset.");
		}

		//
		String pssdName = r1.value("asset/meta/daris:pssd-object/name");
		if(pssdName!=null&&overwrite==false){
			throw new Exception("There is existing daris:pssd-object/name. To overwrite it, set overwrite to true.");
		}

		String newPssdName = r1.value("asset/meta/daris:pssd-filename/original");
		if (newPssdName==null) return;
		updatePssdName(id,newPssdName);

	}
	
	private void updatePssdName(String id, String newPssdName) throws Throwable {
		
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.push("meta");
		doc.push("daris:pssd-object");
		doc.add("name",newPssdName);
		doc.pop();
		doc.pop();
		executor().execute("asset.set", doc.root());	
	}

}