package nig.mf.plugin.pssd.services;


import java.util.Collection;

import nig.mf.pssd.plugin.util.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcDaRISProjectMetaDataHarvest extends PluginService {



	private Interface _defn;

	public SvcDaRISProjectMetaDataHarvest() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The asset ID of the local Project to harvest.",
				1, 1));
	}

	public String name() {

		return "daris.project.metadata.harvest";
	}

	public String description() {

		return "Specialized services for use with OMA-PMH meta-data harvesting framework.  It maps from asset id to citeable ID and calls om.pssd.project.metadata.harvest for the given project asset. It does not transform the meta-data to RIF-CS as this is done by the OMI-PMH layer.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}


	public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		// Parse arguments
		String id = args.stringValue("id");
		String cid = CiteableIdUtil.idToCid(executor(), id);
		if (cid==null) {
			throw new Exception ("The supplied asset has no citeable ID");
		}
		
		// Hand on
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		dm.add("transform", false);
		dm.add("transfer", false);
		XmlDoc.Element r = executor().execute("om.pssd.project.metadata.harvest", dm.root());
		System.out.println("r="+r);
		System.out.println("r/result="+r.element("result"));
		Collection<XmlDoc.Element> els = r.elements();
		for (XmlDoc.Element el : els) {
			System.out.println("el="+el);
		}
		w.add(r.elementAt(0));
	}
}
