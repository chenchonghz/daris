package nig.mf.plugin.pssd.services;



import java.util.ArrayList;
import java.util.Collection;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;



public class SvcDataSetClone extends PluginService {


	private Interface _defn;

	public SvcDataSetClone() {
		_defn = new Interface();
		_defn.add(new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent Study under which to locate the clone. Defaults to the parent of the input DataSet. ", 0, 1));
		//
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the local DataSet to clone. ", 1, 1));
		//
		_defn.add(new Interface.Element("dataset-number", IntegerType.POSITIVE_ONE,
				"Specifies the data-set number for the new DataSet's identifier. If not given, the next available DataSet is created. If specified, then there cannot be any other asset/object with this identity assigned.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the dataset-number is not given, fill in the DataSet allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to true; use with care in federated envionment.", 0, 1));
	}


	public String name() {
		return "om.pssd.dataset.clone";
	}

	public String description() {
		return "Clone the DataSet including all ACLs, meta-data and content.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}


	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// We can only clone local data sets
		DistributedAsset dID = new DistributedAsset (null, args.value("id"));

		// Validate
		PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID));
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(DataSet.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + DataSet.TYPE);
		}
		if (dID.isReplica()) {
			throw new Exception ("The supplied DataSet is a replica and this service cannot clone it.");
		}
		if (!dID.isLocal()) {
			throw new Exception("The supplied DataSet is hosted by a remote server, cannot clone it");
		}

		//
		XmlDoc.Element datasetNumber = args.element("dataset-number");		
		XmlDoc.Element fillIn = args.element("fillin");

		// Get Parent Project
		DistributedAsset dPID = dID.getParentProject (false);
		if (!dPID.isLocal()) {
			throw new Exception("The supplied DataSet's parent Project is hosted by a remote server; cannot clone it");
		}

		// Get the destination parent Study id and validate
		String pid = args.value("pid");
		if (pid!=null) {
			DistributedAsset dStID = new DistributedAsset (null, pid);
			// Validate
			type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dStID));
			if (type==null) {
				throw new Exception("The asset associated with " + dStID.toString() + " does not exist");
			}
			if ( !type.equals(Study.TYPE) ) {
				throw new Exception("Object " + dStID.getCiteableID() + " [type=" + type + "] is not a " + Study.TYPE);
			}
			if (dStID.isReplica()) {
				throw new Exception ("The supplied parent Study is a replica and this service cannot locate DataSets under it.");
			}
			if (!dStID.isLocal()) {
				throw new Exception("The supplied parent Study is hosted by a remote server and this service cannot locate DataSets under it.");
			}
		} else {
			pid = CiteableIdUtil.getParentId(dID.getCiteableID());
		}


		// CLone it
		clone (executor(), pid, dID.getCiteableID(), fillIn, datasetNumber,  w);
	}


	private void clone (ServiceExecutor executor, String pid, String oldID, XmlDoc.Element fillIn, XmlDoc.Element datasetNumber, XmlWriter w) throws Throwable {

		// Fetch the meta-data from the existing data-set
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", oldID);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());

		// Primary or derived ?
		String type = r.stringValue("object/source/type");
		Boolean isPrimary = (type.equals("primary"));

		// Create empty DataSet with no content
		dm = new XmlDocMaker("args");
		dm.add("pid", pid);
		if (fillIn!=null) dm.add(fillIn);
		if (datasetNumber != null) dm.add(datasetNumber);

		if (isPrimary) {
			r = executor.execute("om.pssd.dataset.primary.create", dm.root());
		} else {
			r = executor.execute("om.pssd.dataset.derivation.create", dm.root());
		}

		// Get cid of new DataSet
		String newID = r.value("id");

		// Get meta-data from new DataSet before cloning and fetch Method info
		XmlDoc.Element meta = AssetUtil.getAsset(executor, newID, null);
		String  methodStep = meta.value("asset/meta/daris:pssd-derivation/method/@step");
		String methodId = meta.value("asset/meta/daris:pssd-derivation/method");

	
		// Clone meta-data and content from old to new
		dm = new XmlDocMaker("args");
		dm.add ("cid", newID);
		dm.add("clone", nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, oldID));
		System.out.println(executor!=null);
		System.out.println(dm.root());
		executor.execute("asset.set", dm.root());

		// Re-fetch meta-data after cloning
		meta = AssetUtil.getAsset(executor, newID, null);
		XmlDoc.Element meta2 = meta.element("asset/meta");
	
		
		// Clean up the duplicated doc types (caused by empty create and then clone)
		removeLastVersion (executor, meta2, newID, "daris:pssd-dataset");

		if (isPrimary) {
			removeLastVersion (executor, meta2, newID, "daris:pssd-acquisition");
		} else {
			// The output parent may be from a different ExMethod
			// Set correct Method meta-data since the new parent may be a different Study
			keepLastVersion (executor, meta2, newID, "daris:pssd-derivation");
			//
			//
			dm = new XmlDocMaker("args");
			dm.add ("id", newID);
			dm.push("method");
			dm.add("id", methodId);
			dm.add("step", methodStep);
			dm.pop();
			executor.execute("om.pssd.dataset.derivation.update", dm.root());
		}

		//
		w.add("id", newID);

	}

	private void removeLastVersion (ServiceExecutor executor, XmlDoc.Element meta, String id, String docType) throws Throwable {

		// FInd all versions of this doc type
		Collection<XmlDoc.Element> docs = meta.elements(docType);
		if (docs==null) return;
		if (docs.size()<=1) return;

		// Find highest version number
		Integer docID = null;
		for (XmlDoc.Element doc : docs) {
			Integer t = Integer.parseInt(doc.value("@id"));
			if (docID==null) docID = t;
			if (t>docID) docID = t;
		}


		// Eradicate highest version document
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.push("meta", new String[]{"action", "remove"});
		dm.add(docType, new String[]{"id", ""+docID});
		dm.pop();
		executor.execute("asset.set", dm.root());
	}


	private void keepLastVersion (ServiceExecutor executor, XmlDoc.Element meta, String id, String docType) throws Throwable {

		// FInd all versions of this doc type
		Collection<XmlDoc.Element> docs = meta.elements(docType);
		if (docs==null) return;
		if (docs.size()<=1) return;

		// Find highest version number
		Integer highestDocID = null;
		ArrayList<Integer> docIDs = new ArrayList<Integer>();
		for (XmlDoc.Element doc : docs) {
			Integer t = Integer.parseInt(doc.value("@id"));
			if (highestDocID==null) highestDocID = t;
			if (t>highestDocID) highestDocID = t;
			docIDs.add(t);
		}


		// Keep highest version document only and purge the rest
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.push("meta", new String[]{"action", "remove"});
		for (Integer docID : docIDs) {
			if (docID<highestDocID) {
				dm.add(docType, new String[]{"id", ""+docID});
			}
		}
		dm.pop();
		executor.execute("asset.set", dm.root());
	}
}

