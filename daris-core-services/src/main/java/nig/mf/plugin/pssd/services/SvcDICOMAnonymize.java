package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcDICOMAnonymize extends PluginService {
	private Interface _defn;

	public SvcDICOMAnonymize() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"cid",
				CiteableIdType.DEFAULT,
				"The identity of the parent object; can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
				0, Integer.MAX_VALUE);
		_defn.add(me);
		//
		me = new Interface.Element("id", AssetType.DEFAULT, "The asset identity (not citable ID) of the parent - can be any of Project,Subject,ExMethod,Study,DataSet.",
				0, 1);
		_defn.add(me);
		me = new Interface.Element("over-ride", BooleanType.DEFAULT, "Over-rides (default false) any meta-data 'daris:pssd-derivation/anonymize' located on DataSets specifying that child DataSets have already been anonymized", 0, 1);
		_defn.add(me);
	}

	@Override
	public String name() {

		return "om.pssd.dicom.anonymize";
	}

	@Override
	public String description() {

		return "Anonymizes the patient name field DICOM element (0010,0010) of the DICOM header (i.e. that embedded in the actual DICOM files) in every DataSet contained by the given parent PSSD object.  The Project must have the meta-data 'daris:pssd-dicom-ingest/project/anonymize=true' or nothing will occur.";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public boolean canBeAborted() {

		return false;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
	
		// Parse
		String id = args.value("id");
		String cid = args.value("cid");
		if (id!=null && cid!=null) {
			throw new Exception ("Can't supply 'id' and 'cid'");
		}
		if (id==null && cid==null) {
			System.out.println("om.pssd.dicom.anonymize - no id or cid=" + cid);
			throw new Exception ("Must supply 'id' or 'cid'");
		}
		if (cid==null) cid = CiteableIdUtil.idToCid(executor(), id);
		Boolean overRide = args.booleanValue("over-ride", false);

		// Get meta-data to see if should anonymize for this Project
		String pid = nig.mf.pssd.CiteableIdUtil.getProjectId(cid);
		XmlDoc.Element meta = AssetUtil.getAsset(executor(), pid, null);
		if (meta== null) return;
		Boolean anon = meta.booleanValue("asset/meta/daris:pssd-dicom-ingest/project/anonymize", false);

		// Execute
		if (anon) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", cid);
			dm.add("anonymize", new String[]{"over-ride", overRide.toString()}, true);
			dm.add("group", "0010");
			dm.add("element", "0010");

			// CHecks object is correct type
			XmlDoc.Element r = executor().execute("om.pssd.dicom.header.edit", dm.root());
			if (r==null) return;
			Collection<XmlDoc.Element> els = r.elements();
			if (els!=null) {
				for (XmlDoc.Element el : els) {
					w.add(el);
				}  
			}
		}
	}
}