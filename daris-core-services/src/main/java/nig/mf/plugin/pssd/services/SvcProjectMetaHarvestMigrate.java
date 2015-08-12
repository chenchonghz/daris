package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialized function to migrate meta-data from daris:pssd-project-harvest into
 * daris:pssd-project-owner and pssd-research-category
 * 
 * @author nebk
 * 
 */
public class SvcProjectMetaHarvestMigrate extends PluginService {
	private Interface _defn;

	public SvcProjectMetaHarvestMigrate() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT,
				"The citeable asset id of the local Project to be migrated. If not set, Projects will be found and migrated.",
				0, 1));
	}

	public String name() {
		return "om.pssd.project.meta.harvest.migrate";
	}

	public String description() {
		return "Specialized service to migrate daris:pssd-project-harvest/{project-owner,field-of-research) to daris:pssd-project-{owner,research-category}.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified Project if any
		String cid = args.value("id");

		// Construct a List of Project asset IDs to loop over
		Collection<String> projects = null;
		if (cid == null) {
			// Find all Subjects
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("where", "model='om.pssd.project' and daris:pssd-project-harvest has value");
			doc.add("size", "infinity");
			doc.add("pdist", 0);          // Force local
			XmlDoc.Element r1 = executor().execute("asset.query", doc.root());
			projects = r1.values("id");
		} else {
			// Get this asset and verify it is a Project; exception if not
			PSSDUtil.isValidProject(executor(), cid, true);
			projects = new Vector<String>();
			projects.add(AssetUtil.getId(executor(), cid));
		}

		// Iterate over all Projects
		for (String project : projects) {
			cid = AssetUtil.getCid(executor(), project);
			if (PSSDUtil.isReplica(executor(), cid)) {
				w.add("project", "The given Project '" + cid + "' is a replica. Cannot modify it.");
			} else {

				// Get the asset
				XmlDoc.Element meta = AssetUtil.getAsset(executor(), null, project);

				// Populate daris:pssd-project-research-category
				createCategory (executor(), meta, project);
				
				// Populate daris:pssd-project-owner
				createOwner (executor(), meta, project);

				// Clean up old documents
				cleanUp (executor(), project);

				w.add("project", cid);
			}
		}
	}



	private void cleanUp (ServiceExecutor executor, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker ("args");
		dm.add("id", id);
		dm.add("type", "daris:pssd-project-harvest");
		dm.add("element", "project-owner");
		executor.execute("nig.asset.doc.element.remove", dm.root());
		//
		dm = new XmlDocMaker ("args");
		dm.add("id", id);
		dm.add("type", "daris:pssd-project-harvest");
		dm.add("element", "field-of-research");
		executor.execute("nig.asset.doc.element.remove", dm.root());		
	}
	

	private void createCategory (ServiceExecutor executor, XmlDoc.Element meta, String id) throws Throwable {

		XmlDoc.Element project = meta.element("asset/meta/daris:pssd-project-harvest");

		if (project!=null) {
			Collection<XmlDoc.Element> fors = project.elements("field-of-research");
			if (fors!=null) {

				// Prepare
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.push("meta", new String[] {"action", "merge"});
				dm.push("daris:pssd-project-research-category", new String[] { "ns", "pssd.public", "tag", "pssd.meta"});
				//
				addElements (dm, fors, "ANZSRC-11");
				//
				dm.pop();
				dm.pop();

				// Set
				executor().execute("asset.set", dm.root());

			}
		}
	}

	private void createOwner (ServiceExecutor executor, XmlDoc.Element meta, String id) throws Throwable {

		XmlDoc.Element project = meta.element("asset/meta/daris:pssd-project-harvest");

		if (project!=null) {
			Collection<XmlDoc.Element> owners = project.elements("project-owner");
			if (owners!=null) {

				// Prepare
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.push("meta", new String[] {"action", "merge"});
				dm.push("daris:pssd-project-owner", new String[] { "ns", "pssd.public", "tag", "pssd.meta"});
				//
				addElements (dm, owners, null);
				//
				dm.pop();
				dm.pop();

				// Set
				executor().execute("asset.set", dm.root());

			}
		}
	}


	private void addElements (XmlDocMaker dm, Collection<XmlDoc.Element> things, String newName) throws Throwable {
		if (things!=null) {
			for (XmlDoc.Element thing : things) {
				if (newName!=null) thing.setName(newName);
				dm.add(thing);
			}
		}

	}

}
