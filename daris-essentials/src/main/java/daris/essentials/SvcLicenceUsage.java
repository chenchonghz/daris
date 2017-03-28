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


public class SvcLicenceUsage extends PluginService {


	public static final String LICENCE_USAGE_ASSET_NAME = "LicenceUsage";
	public static final String LICENCE_USAGE_DOCTYPE =  "daris:LicenceUsage";
	public static final String MAX_VERSIONS = "10";    // Number of versions of asset


	private Interface _defn;

	public SvcLicenceUsage()  throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("namespace", StringType.DEFAULT, "Namespace to locate the tracking asset in.", 1, 1));
		_defn.add(new Interface.Element("date", StringType.DEFAULT, "A fake date for testing in the form dd-MMM-YYYY.", 0, 1));
		_defn.add(new Interface.Element("nused", IntegerType.DEFAULT, "A fake number of licenses used for testing.", 0, 1));
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public Interface definition() {
		return _defn;

	}

	public String description() {
		return "Track licence usage.  An asset is created which tracks by date, the maximum number of licences used.   Schedule a job to run every 15 minutes.";
	}

	public String name() {
		return "nig.licence.usage";
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

		String date  = args.value("date");
		String nUsedS = args.value("nused");
		String nameSpace = args.value("namespace");

		// FInd current license usage
		XmlDoc.Element r = executor().execute("licence.describe");
		String total = r.value("licence/total");
		String remaining = r.value("licence/remaining");
		//
		Integer nUsed = null;
		if (nUsedS==null) {
			nUsed = Integer.parseInt(total) - Integer.parseInt(remaining);
		} else {
			nUsed = Integer.parseInt(nUsedS);
		}
		w.add("nused", nUsed);
		if (date==null) date = DateUtil.todaysDate(2);

		// FInd the licence usage asset
		String id = findLicenceUsageAsset(executor(), nameSpace, LICENCE_USAGE_ASSET_NAME);
		if (id==null) {
			id = createLicenceUsageAsset (executor(), nameSpace);
		}
		w.add("id", id);
		w.add("date", date);

		// Get the asset
		XmlDoc.Element asset = AssetUtil.getAsset(executor(), null, id);

		// Get the meta-data 
		XmlDoc.Element meta = asset.element("asset/meta/" + LICENCE_USAGE_DOCTYPE);

		// See if today is there already
		XmlDoc.Element usedToday = null;
		if (meta!=null) {
			usedToday = meta.element("max-used[@date='"+date+"']");
		}

		Integer max = null;
		if (usedToday!=null) {
			Integer oldMax = Integer.parseInt(usedToday.value());
			max =  java.lang.Math.max(oldMax,nUsed);
			Boolean update = (max>oldMax);
			//
			w.add("max", new String[]{"old", ""+oldMax}, max);
			if (update) {
				usedToday.setValue(max);

				// Update
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.add("max-versions", MAX_VERSIONS);
				dm.push("meta");
				removeAttribute(meta, "id");
				dm.add(meta);
				dm.pop();
				executor().execute("asset.set", dm.root());
			}
		} else {
			max = nUsed;
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.add("max-versions", MAX_VERSIONS);
			dm.push("meta");
			dm.push(LICENCE_USAGE_DOCTYPE);
			dm.add("max-used", new String[]{"date", date}, max);
			dm.pop();
			dm.pop();
			executor().execute("asset.set", dm.root());
			w.add("max", max);
		}
	}


	public static String findLicenceUsageAsset (ServiceExecutor executor, String nameSpace, String name) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pdist", "0");
		//
		String where = "namespace='" + nameSpace + 
				"' and name='" + name + "'";
		dm.add("where", where);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		return r.value("id");
	}


	private String createLicenceUsageAsset (ServiceExecutor executor, String nameSpace) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");

		dm.add("name",LICENCE_USAGE_ASSET_NAME);
		dm.add("namespace", nameSpace);
		XmlDoc.Element r = executor.execute("asset.create", dm.root());
		return r.value("id");
	}

	private void removeAttribute (XmlDoc.Element doc, String attributeName) throws Throwable {
		XmlDoc.Attribute attr = doc.attribute(attributeName);
		if (attr != null) {
			doc.remove(attr);
		}

	}

}
