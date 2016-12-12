package daris.essentials;

import java.util.Collection;

import java.util.Vector;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetCheck extends PluginService {


	private Interface _defn;
	private Integer idx_ = 1;
	private Integer count_ = 0;


	public SvcAssetCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the local host. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit the accumulation loop to this number of assets per iteration (if too large, the VM may run out of virtual memory).  Defaults to 500.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
		_defn.add(new Interface.Element("analyze", BooleanType.DEFAULT, "Run the analyser on any assets found to be bad. Defaults to false.", 0, 1));
	}
	public String name() {
		return "nig.asset.check";
	}

	public String description() {
		return "Calls asset.get on each asset found and reports if an exceptiuon occurs.  This may because of malformed XML.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public int executeMode() {
		return EXECUTE_LOCAL;
	}

	public boolean canBeAborted() {

		return true;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		// Init
		idx_ = 1;
		count_ = 0;

		// Get inputs
		String where = args.value("where");
		String size = args.stringValue("size", "500");
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);
		Boolean analyze = args.booleanValue("analyze", false);

		// Iterate through cursor and build list of assets 
		boolean more = true;
		Vector<String> assetIDs = new Vector<String>();
		while (more) {
			more = find (executor(),  where, size, assetIDs, useIndexes, dbg, analyze,  w);
			if (dbg) {
				System.out.println("nig.asset.check : checking for abort \n");
			}
			PluginTask.checkIfThreadTaskAborted();
		}

	
		w.add("total-checked", count_);
	}

	private boolean find (ServiceExecutor executor,  String where,  String size,  Vector<String> assetList, 
			 Boolean useIndexes, Boolean dbg, Boolean analyze, XmlWriter w)
					throws Throwable {

		// Find local  assets  with the given query. We work through the cursor else
		// we may run out of memory
		if (dbg) System.out.println("nig.asset.check : find assets in chunk starting with idx = " + idx_);
		XmlDocMaker dm = new XmlDocMaker("args");
		if (where!=null) dm.add("where", where);
		dm.add("idx", idx_);
		dm.add("size", size);
		dm.add("pdist", 0);
		dm.add("use-indexes", useIndexes);
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return false;  
		Collection<String> assets = r.values("id");
		if (assets==null) return false;
		count_ += assets.size();

		// Get the cursor and increment for next time
		XmlDoc.Element cursor = r.element("cursor");
		boolean more = !(cursor.booleanValue("total/@complete"));
		if (more) {
			Integer next = cursor.intValue("next");
			idx_ = next;
		}

		// See if the replicas exist on the peer. 
		// One query per asset
		
		// Make a list of rids to find
		for (String asset : assets) {
			dm = new XmlDocMaker("args");	
			dm.add("id", asset);
			try {
				executor.execute("asset.get", dm.root());
			} catch (Throwable t) {
				if (analyze) {
					dm = new XmlDocMaker("args");
					dm.add("id", asset);
					executor.execute("asset.reanalyze", dm.root());
					w.add("id", new String[]{"message", t.getMessage(), "reanalyzed", "true"}, asset);
				} else {
					w.add("id", new String[]{"message", t.getMessage(), "reanalyzed", "false"}, asset);
				}
			}
		}
		//
		return more;
	}
}
