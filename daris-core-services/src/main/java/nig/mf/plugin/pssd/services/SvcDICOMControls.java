package nig.mf.plugin.pssd.services;


import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMControls extends PluginService {
	private Interface _defn;

	public SvcDICOMControls() throws Throwable {
	}

	public String name() {
		return "om.pssd.dicom.controls";
	}

	public String description() {
		return "Lists the available DICOM controls for configuring the PSSD DICOM engine.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "nig.dicom");
		XmlDoc.Element r = executor().execute("dicom.asset.engine.describe", dm.root());	
		w.add(r.element("engine"));
	}

}
