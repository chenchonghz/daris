package nig.mf.plugin.pssd.services;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nig.dicom.util.DicomModify;
import nig.io.FileUtils;
import nig.mf.plugin.util.ArchiveUtil;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcDICOMHeaderEdit extends PluginService {
	private Interface _defn;

	public SvcDICOMHeaderEdit() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"id",
				CiteableIdType.DEFAULT,
				"The identity of the parent object under which a query is done for DataSets.  Can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
				1, 1);
		_defn.add(me);
		me = new Interface.Element("where",
				XmlDocType.DEFAULT, "Define additional selection query to be applied when searching for children DataSets.", 0, 1);
		_defn.add(me);
		_defn.add(new Interface.Element("group", StringType.DEFAULT,
				"The DICOM  group edit (e.g. 0010).", 1, 1));
		_defn.add(new Interface.Element("element", StringType.DEFAULT,
				"The DICOM element to edit (e.g. 0020).", 1, 1));
		_defn.add(new Interface.Element("value", StringType.DEFAULT,
				"The new value (or specify argument 'anonymize').", 0, 1));
		me = new Element("anonymize", BooleanType.DEFAULT,
				"Rather than supplying a new value for the field, anonymise it (defaults to false). ", 0, 1);
		me.add(new Interface.Attribute("over-ride", BooleanType.DEFAULT, "By default, if meta-data daris:pssd-derivation/anonymize is true, then the DataSet has been anonymized already by this service and will be skipped.  If over-ride is true, it will (re)anonymize regardless of the value of this meta-data element.", 0));
		_defn.add(me);
	}

	@Override
	public String name() {

		return "om.pssd.dicom.header.edit";
	}

	@Override
	public String description() {

		return "Edits the header of local (this server) DICOM files in the content of DataSets.";
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
		String pid = args.value("id");
		String type = args.stringValue("asset-type", "all");
		String where = args.stringValue("where");
		String group = args.value("group");
		String element = args.value("element");
		String val = args.value("value");
		//
		XmlDoc.Element anonX = args.element("anonymize");
		if (val!=null && anonX!=null) {
			throw new Exception ("You cann't supply 'value' and 'anonymize'");
		}
		Boolean anon = false;
		Boolean overRide = false;
		if (val==null) {
			anon = args.booleanValue("anonymize", false);
			overRide = anonX.booleanValue("@over-ride", false);
		}

		// We don't really want someone rewriting the whole repository
		Boolean ok = PSSDUtil.isValidProject(executor(), pid, false) || 
				PSSDUtil.isValidSubject(executor(), pid, false) ||
				PSSDUtil.isValidExMethod(executor(), pid, false) ||
				PSSDUtil.isValidStudy(executor(), pid, false) ||
				PSSDUtil.isValidDataSet(executor(), pid, false);
		if (!ok) {
			throw new Exception ("Asset must be a Project,Subject,ExMethod,Study or DataSet");
		}

		// Find the DataSets with DICOM content. 
		XmlDocMaker dm = new XmlDocMaker("args");
		StringBuilder query = new StringBuilder("(cid='" + pid + "' or cid starts with '" + pid + "')");
		query.append(" and model='om.pssd.dataset' and type='dicom/series'");
		DistributedQuery.appendResultAssetTypePredicate(query, DistributedQuery.ResultAssetType.instantiate(type));
		if (where!=null) query.append(" and " + where);
		dm.add("where", query.toString());
		dm.add("pdist", 0);                       // Local only
		dm.add("size", "infinity");
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return;

		// Iterate over found DataSets.
		Collection<String> assets = r.values("id");
		if (assets != null) {
			int clevel = 6;
			for (String asset : assets) {
				String cid = CiteableIdUtil.idToCid(executor(), asset);

				// For anonymizing, Skip if already anonymized and not over-riding
				Boolean skip = false;
				if (anon) {
					skip = skip(overRide, cid, w);
				} else {
					w.add("id", cid);
				}

				if (!skip) {
					// Make new temporary file for archive and temporary directory to unpack into
					File tempArchive = createTemporaryFile();
					File tempDir = createTemporaryDirectory();

					try {
						// Unpack content  into temporary directory
						String containerType = AssetUtil.getContentMimeType(executor(), asset);
						ArchiveUtil.unpackContent (executor(), asset, tempDir);

						// Edit files if required and put into DICOM files container
						if (anon) {
							DicomModify.anonymizeFiles (tempDir, group, element);
						} else {
							DicomModify.editFiles (tempDir, group, element, val);
						}

						// Make new archive
						List<File> files = Arrays.asList(tempDir.listFiles());
						nig.compress.ArchiveUtil.compress (files, tempArchive, containerType, clevel, "dicom");

						// Set content
						AssetUtil.setContent(executor(), asset, tempArchive, containerType);

						// Prune asset to remove previous version
						AssetUtil.pruneContent(executor(), asset);

						// Clean up
						FileUtils.delete(tempDir);
						tempArchive.delete();

						// Set anon meta-data
						if (anon) {
							setAnonymized (executor(), cid);
						}
					} catch (Throwable t) {
						FileUtils.delete(tempDir);
						tempArchive.delete();
						throw t;
					}
				}
			}
		}
	}



	public void setAnonymized (ServiceExecutor executor, String cid)	 throws Throwable {

		// DICOM DAtaSets are by definition derived
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		dm.add("anonymized", "true");		
		executor.execute("om.pssd.dataset.derivation.update", dm.root());	
	}

	/**
	 * 
	 * @param overRide
	 * @param id
	 * @return 0 - don't anonymize because already anonymized and no over-ride
	 *         1 - do    anonymize because already anonymized but over-ride
	 *         2 - do    anonymize because not already anonymized
	 * @throws Throwable
	 */
	public Boolean skip (Boolean overRide, String cid, XmlWriter w) throws Throwable {
		if (overRide) {
			w.add("id", cid);
			return false;
		}
		
		XmlDoc.Element meta = AssetUtil.getAsset(executor(), cid, null);
		Boolean t = meta.booleanValue("asset/meta/daris:pssd-derivation/anonymized", false);
		if (t) {
			w.add("id", new String[]{"skipped", "true"}, cid);
			return true;
		} else {
			w.add("id", cid);
			return false;
		}
	}
}