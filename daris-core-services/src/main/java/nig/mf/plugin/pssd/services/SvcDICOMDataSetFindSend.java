package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMDataSetFindSend extends PluginService {
	private Interface _defn;

	public SvcDICOMDataSetFindSend() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("modality", StringType.DEFAULT, "The DICOM modality of interest.",
				0, 1));
		_defn.add(new Interface.Element("pid", CiteableIdType.DEFAULT, "Restrict the search to this parent citeable ID (can be a DataSet).",
				0, 1));
		_defn.add(new Element("age", IntegerType.DEFAULT, "Find Studies exclusively (time>today-age) younger (created/modified) than this in days (defaults to infinity).", 0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		Interface.Element me = new Interface.Element("ae",XmlDocType.DEFAULT,
				"The DICOM Application Entity (e.g. a DICOM server). If not specicied just lists the found DataSets.",0,1);
		me.add(new Interface.Element("host", StringType.DEFAULT, "The AE host name or IP address.", 1, 1));
		me.add(new Interface.Element("port", IntegerType.DEFAULT, "The port number of the AE", 1, 1));
		me.add(new Interface.Element("aet",StringType.DEFAULT,"The AET of the AE.", 1, 1));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("aet", StringType.DEFAULT, "The calling (our) AET (only required if ae provided).", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Set to 0 (default) for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
		_defn.add(new Interface.Element("where", StringType.DEFAULT, "Other query clauses to be applied (ANDed) in selecting the DataSets.",
				0, 1));

		_defn.add(new Interface.Element(
				"patient-name-action",
				new EnumType(SvcDICOMSend.ElementAction.stringValues()),
				"Sets the action performed on the patient name field of the DICOM file header before sending. Defaults to 'unchanged'. Note: it will not change the local objects but only the intermediate files extracted from the objects. If 'use-mf-dicom-patient-name' or 'use-mf-dicom-patient-id' is selected, but there is no mf-dicom-patient document on the Subject (or the name is null) then no edit will occur.",
				0, 1));
		_defn.add(new Interface.Element(
				"patient-id-action",
				new EnumType(SvcDICOMSend.ElementAction.stringValues()),
				"Sets the action performed on the patient ID field of the DICOM file header before sending. Defaults to 'unchanged'. Note: it will not change the local objects but only the intermediate files extracted from the objects. If 'use-mf-dicom-patient-name' or 'use-mf-dicom-patient-id' is selected, but there is no mf-dicom-patient document on the Subject (or the name is null) then no edit will occur.",
				0, 1));

	}

	public String name() {

		return "om.pssd.dicom.dataset.find-and-send";
	}

	public String description() {

		return "Finds DICOM DataSets of a particular age and modality and onsends them to a new AET.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String modality = args.value("modality");
		String pid = args.value("pid");
		String age = args.value("age");
		String assetType = args.stringValue("asset-type", "all");
		String where = args.value("where");
		XmlDoc.Element ae = args.element("ae");
		String  aet = args.value("aet");
		if (ae!=null && aet==null) {
			throw new Exception ("You must supply a sending AET");
		}
		XmlDoc.Element pna = args.element("patient-name-action");
		XmlDoc.Element pia = args.element("patient-id-action");

		//
		String pdist = args.stringValue("pdist", "0");

		// Set up query.
		StringBuilder query = new StringBuilder();
		query.append("(model='om.pssd.dataset' and type='dicom/series')");
		//
		if (modality!=null) query.append("and mf-dicom-series/modality='" + modality + "'");
		//
		if (pid!=null) {
			query.append (" and (cid='" + pid +"' or cid starts with '" + pid + "')");
		}

		if (age!=null) {
			query.append (" and (mtime>'TODAY-" + age + "DAY')");
		}
		if (where!=null) {
			query.append(" and (" + where + ")");
		}

		// Type of asset to find; primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, ResultAssetType.instantiate(assetType));

		// Set up service call.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("action", "get-value");
		dm.add("xpath", "cid");
		if (pdist != null) dm.add("pdist", pdist);

		// Query is distributed in federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return;

		// List/onsend
		Collection<String> cids = r.values("asset/value");
		if (cids==null) return;
		for (String cid : cids) {
			if (ae!=null) {
				dm = new XmlDocMaker("args");
				dm.add("pid", cid);
				dm.add(ae);
				dm.add("aet", aet);
				if (pna!=null) dm.add(pna);
				if (pia!=null) dm.add(pia);
				XmlDoc.Element r2 = executor().execute("om.pssd.dicom.send", dm.root());
				if (r2!=null) {
					Collection<XmlDoc.Element> t = r2.elements("id");
					if (t!=null) w.addAll(t);
				}
			} else {
				w.add("id", cid);
			}
		}
	}
}
