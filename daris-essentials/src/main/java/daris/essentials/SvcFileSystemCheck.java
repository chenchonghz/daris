package daris.essentials;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlPrintStream;
import arc.xml.XmlWriter;

public class SvcFileSystemCheck extends PluginService {


	private Interface _defn;


	public SvcFileSystemCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("email",StringType.DEFAULT, "Send report to this email address (defaults to none)", 0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element("mflux",StringType.DEFAULT, "Root for Mediaflux system. Defaults to /opt/mediaflux", 0, 1));
		_defn.add(new Interface.Element("store-threshold",IntegerType.DEFAULT, "Threshold percentage for stores.  Defaults to 90.", 0, 1));
		_defn.add(new Interface.Element("volatile-threshold",IntegerType.DEFAULT, "Threshold percentage for volatile file systems.  Defaults to 90.", 0, 1));
		_defn.add(new Interface.Element("list",BooleanType.DEFAULT, "List all stores and file systems (default false) whether they cross the threshold boundary or not.", 0, 1));
		_defn.add(new Interface.Element("include-null-store",BooleanType.DEFAULT, "If it's not possible to work out the store statistics, include anyway with 'null' presentation (default false).", 0, 1));
	}
	public String name() {
		return "nig.file.system.check";
	}

	public String description() {
		return "Reports stores and file systems when usage is above a threshold.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public int executeMode() {
		return EXECUTE_LOCAL;
	}

	public boolean canBeAborted() {

		return true;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String rPath = args.stringValue("mflux", "/opt/mediaflux");
		long storeThresh = args.intValue("store-threshold", 90);
		long volatileThresh = args.intValue("volatile-threshold", 90);
		Boolean listAll = args.booleanValue("list",  false);
		Boolean includeNullStore = args.booleanValue("include-null-store",  false);
		Collection<String> emails = args.values("email");

		//
		XmlDocMaker dm = new XmlDocMaker("args");
		Boolean some = false;

		// Stores		
		XmlDocMaker dm2 = new XmlDocMaker("args");
		dm2.push("stores");
		XmlDoc.Element r = executor().execute("asset.store.describe");
		Collection<XmlDoc.Element> stores = r.elements("store");
		for (XmlDoc.Element store : stores) {
			checkStore(store, storeThresh, listAll, includeNullStore, dm2);
		}
		dm2.pop();
		XmlDoc.Element rr = dm2.root().element("stores");
		if (listAll || rr.hasSubElements()) {
			some = true;
			dm.add(dm2.root().element("stores"));
		}

		// Other volatile directories
		dm2 = new XmlDocMaker("args");
		dm2.push("file-systems");
		checkFileSystem (rPath + "/volatile/database", volatileThresh, listAll, dm2);
		checkFileSystem (rPath + "/volatile/tmp", volatileThresh, listAll, dm2);
		checkFileSystem (rPath + "/volatile/logs", volatileThresh, listAll, dm2);
		checkFileSystem (rPath + "/volatile/shopping", volatileThresh, listAll, dm2);
		checkFileSystem (rPath + "/volatile/staging", volatileThresh, listAll, dm2);
		checkFileSystem ("/tmp", volatileThresh, listAll, dm2);
		dm2.pop();
		rr = dm2.root().element("file-systems");
		if (listAll || rr.hasSubElements()) {
			some = true;
			dm.add(dm2.root().element("file-systems"));
		}

		// Report to caller
		w.add(dm.root(), false);

		// Email
		if (emails!=null && some) {
			String body = XmlPrintStream.outputToString(dm.root());
			String subject = "Mediaflux file system checks (UUID="+PluginService.serverIdentityAsString()+")";
			String from = getServerProperty (executor(), "mail.from");
			sendMessage (executor(), emails, subject, body, from);
		}
	}


	private void checkFileSystem (String path, long threshold, Boolean listAll, XmlDocMaker w) throws Throwable {
		if (path==null) return;
		File p = new File(path);
		double tspace = p.getTotalSpace();
		double fspace = p.getFreeSpace();
		double r1 = (double)1.0 - (fspace/tspace);
		long r = (long) (r1 * 100);

		if (listAll || r>threshold) {	
			w.add("path", new String[]{"threshold", ""+threshold, "total", ""+(long)tspace, "free", ""+(long)fspace, "percentage-used", ""+r}, path);
		}
	}


	private void checkStore (XmlDoc.Element store, long threshold, Boolean listAll, Boolean includeNullStore, XmlDocMaker w) throws Throwable {
		if (store==null) return;
		String name = store.value("@name");
		String type = store.stringValue("type");
		if (type.equals("database")) return;
		//
		String path = store.stringValue("mount/path");
		String tspaceS = store.value("mount/partition-size");
		String fspaceS = store.value("mount/partition-free-space");

		String rS = null;
		long r = 0;
		if (tspaceS!=null && fspaceS!=null) {
			double tspace = Double.parseDouble(tspaceS);
			double fspace = Double.parseDouble(fspaceS);
			//
			double r1 = (double)1.0 - (fspace/tspace);
			r = (long) (r1 * 100);
			rS = "" + r;
		} else {
			tspaceS = "null";
			fspaceS = "null";
		}

		if (listAll || ( (rS!=null && r>threshold) || (rS==null && includeNullStore)) ) {	
			w.add("store", new String[]{"path", path, "type", type, "threshold", ""+threshold, "total", tspaceS, "free", fspaceS, "percentage-used", ""+rS}, 
					name);
		}
	}


	private void sendMessage (ServiceExecutor executor, Collection<String> emails, String subject, String body,  String from) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("body", body);
		dm.add("subject", subject);
		dm.add("from", from);
		//
		for (String email : emails) {
			dm.add("to", email);
		}
		executor.execute("mail.send", dm.root());
	}

	private  String getServerProperty (ServiceExecutor executor, String name) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", name);
		XmlDoc.Element r = executor.execute("server.property.get", dm.root());
		return r.value("property");
	}


}
