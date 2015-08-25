package daris.essentials;

import java.util.Collection;


import nig.mf.dicom.plugin.util.DICOMModelUtil;
import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcDICOMDestroy extends PluginService {
	private Interface _defn;

	public SvcDICOMDestroy() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element(
				"id",
				AssetType.DEFAULT,
				"The asset id of the Patient (mf-dicom-patient) or study (mf-dicom-study) record for which all associated data will be destroyed.",
				1, Integer.MAX_VALUE));
		_defn.add(new Interface.Element("list", BooleanType.DEFAULT, "List rather than destroy. Defaults to true", 0, 1));
	}

	@Override
	public String name() {

		return "dicom.destroy";
	}

	@Override
	public String description() {

		return "Destroys DICOM data model assets.";
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

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Parse
		Collection<String> ids = args.values("id");
		Boolean listOnly = args.booleanValue("list", true);

		if (ids!=null) {
			for (String id : ids) {
				XmlDoc.Element m = AssetUtil.getAsset(executor(), null, id);
				String type = m.value("asset/type");
				if (type.equals("dicom/patient")) {
					DICOMModelUtil.destroyPatient(executor(), id, listOnly, w);
				} else if (type.equals("dicom/study") || type.equals("siemens-raw-petct/study")) {
					w.add("study", id);
					if (!listOnly) DICOMModelUtil.destroyStudy(executor(), id);
				} else {
					throw new Exception ("The asset " + id + " is neither a DICOM nor raw Siemens Study");
				}
			}
		}
	}
}
