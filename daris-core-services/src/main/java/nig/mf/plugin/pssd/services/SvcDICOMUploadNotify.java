package nig.mf.plugin.pssd.services;


import java.util.Collection;

import nig.iio.metadata.XMLUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;




/**
 * This service is used by the DICOM engine framework to notify recipients that data
 * have been uploaded. It is configured in the DICOM control e.g.
 *            :arg -name study.load.services "om.pssd.dicom.upload.notify"
 *
 * @author nebk
 *
 */
public class SvcDICOMUploadNotify extends PluginService {
	private Interface _defn;

	public SvcDICOMUploadNotify() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", AssetType.DEFAULT, "The asset identity (not citable ID) of the uploaded Study.",
				1, 1);
		_defn.add(me);
		_defn.add(new Interface.Element ("add-meta", BooleanType.DEFAULT, "Add a description of the object meta-data (Defaults to true).", 0, 1));
//		_defn.add(new Interface.Element ("destroy-if-empty", BooleanType.DEFAULT, "If the uploaded Study has no children, destroy and notify. Defaults to false.  An email will be sent to notification recipients regarldess if an empty stufy arises.", 0, 1));
	}

	@Override
	public String name() {

		return "om.pssd.dicom.upload.notify";
	}

	@Override
	public String description() {

		return "Notifies recipients (configured in Project meta-data daris:pssd-notification) with the om.pssd.project.mail.send service that a DICOM Study has been uploaded.";
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
		String id = args.value("id");
		Boolean addMeta = args.booleanValue("add-meta");
		Boolean destroyIfEmpty = false; // args.booleanValue("destroy-if-empty");
		String studyCid = CiteableIdUtil.idToCid(executor(), id);
		if (studyCid!=null) {
			
			// If we drop modalities, like the dose report (see dicom control "nig.dicom.dose-reports.drop)
			// we may end up with an empty study
			int nChildren = nChildren (executor(), studyCid);

			// Get the description of the Study
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", studyCid);
			XmlDoc.Element objectMeta = executor().execute("om.pssd.object.describe", dm.root());

			// Configure
			String projectCid = nig.mf.pssd.CiteableIdUtil.getProjectId(studyCid);
			if (projectCid==null) projectCid = "unknown";            // Perhaps should really be an error

			dm = new XmlDocMaker("args");
			dm.add ("id", projectCid);
			dm.add("use-notification", new String[] {"category", "data-upload"}, true);
			//
			String subjectString = "New PSSD DICOM data for Project "+projectCid + " has been uploaded";
			dm.add("subject", subjectString);
			//
			String bodyString = null;
			if (nChildren==0) {
				if (destroyIfEmpty) {     
					bodyString = "Project : " + projectCid  + "\n Study   :  " + studyCid + " has no Series\n Destroying.\n regards \n Mediaflux";
				} else {
					bodyString = "Project : " + projectCid  + "\n Study   :  " + studyCid + " has no Series\n You may wish to destroy. \n regards \n Mediaflux";
				}
			} else {
				bodyString = "Project : " + projectCid  + "\n Study   :  " + studyCid + " \n \n regards \n Mediaflux";
			}
			
			// Add nicely formatted copy of the meta-data
			if (addMeta && objectMeta!=null) {
				XmlDoc.Element r = objectMeta.element("object");
				if (r!=null) {
					bodyString += "\n\n\n Meta-data \n\n " + XMLUtil.toText(r);
				}
			}
			dm.add("message", bodyString);
			dm.add("async", false);

			// Do it
			executor().execute("om.pssd.project.mail.send", dm.root());
			
			if (destroyIfEmpty && nChildren==0) {
				// Destroy the Study
				dm = new XmlDocMaker("args");
				dm.add ("cid", studyCid);
//				executor().execute("om.pssd.object.destroy", dm.root());						
			}
		}
	}

	private int nChildren (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid starts with '" + cid + "'");
		// Although mostly we have just made the study with the DICOM server, it's possible
		// we are adding to it and so it pre-exists and may have children elsewhere.  
		// So we have to look in the federation. If the federation is not on, we 
		// are in trouble.  This is a very very remote risk.
		dm.add("pdist", "infinity");  
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return 0;
		Collection<String> ids = r.values("id");
		if (ids==null) return 0;
		return ids.size();
	}

}
