package nig.mf.plugin.pssd.services;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nig.dicom.util.DicomModify;
import nig.io.FileUtils;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcDICOMNormalizeHeader extends PluginService {
	private Interface _defn;

	public SvcDICOMNormalizeHeader() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"id",
				CiteableIdType.DEFAULT,
				"The identity of the parent object, can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
				1, 1);
		_defn.add(me);
		me = new Interface.Element("where",
				XmlDocType.DEFAULT, "Define additional selection query to be applied when searching for children DataSets.", 0, 1);
		_defn.add(me);
		_defn.add(new Interface.Element("asset-type", new EnumType(DistributedQuery.ResultAssetType.stringValues()),
				"Specify type of asset to send. Defaults to all.", 0, 1));

	}

	@Override
	public String name() {

		return "om.pssd.dicom.header.normalize";
	}

	@Override
	public String description() {

		return "Rewrites the header of local (this server) DICOM files in the content of DataSets. This cleans the header and fixes a problem with wrong total group length (fixed in Mediaflux 3.7.003)";
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

		// We don't really want someone rewriting the whole repository
		int depth = CiteableIdUtil.getIdDepth(pid);
		if (depth < 3) {
			throw new Exception ("The depth of the given parent citeable ID must be at least 3 (a Project)");
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
		Collection<String> dataSets = r.values("id");
		if (dataSets != null) {
			for (String id : dataSets) {
				String cid = CiteableIdUtil.idToCid(executor(), id);
				String contentMimeType = AssetUtil.getContentMimeType(executor(), id);


				// Make a temporary directory and archive
				File tempDir = createTemporaryDirectory();
				File tempFile = createTemporaryFile();

				try {
					// Unpack content, if any, and copy to temporary directory
					if (contentMimeType!=null) {
						nig.mf.plugin.util.ArchiveUtil.unpackContent(executor(), id, tempDir);					
						int clevel = 6;

						// Fix the header of the extracted DICOM files
						DicomModify.editFiles (tempDir, null, null, null);

						
						// Create a new archive of the modified content
						List<File> files = Arrays.asList(tempDir.listFiles());
						nig.compress.ArchiveUtil.compress(files, tempFile,  contentMimeType, clevel, "dicom");

						// Set new content
						AssetUtil.setContent (executor(), id, tempFile, contentMimeType);

						// Prune asset to remove previous version
						AssetUtil.pruneContent(executor(), id);

						// Add to output
						w.add("id", cid);

						// Clean up
						FileUtils.delete(tempDir);
						FileUtils.delete(tempFile);
					}
				} catch (Throwable t) {
					// Clean up and rethrow
					FileUtils.delete(tempDir);
					FileUtils.delete(tempFile);
					throw t;
				}
			}
		}
	}
}
