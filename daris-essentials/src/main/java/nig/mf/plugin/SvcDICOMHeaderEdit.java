package nig.mf.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nig.dicom.util.DicomModify;
import nig.io.FileUtils;
import nig.mf.dicom.plugin.util.DICOMModelUtil;
import nig.mf.plugin.util.ArchiveUtil;
import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;

import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;



public class SvcDICOMHeaderEdit extends PluginService {
	private Interface _defn;

	public SvcDICOMHeaderEdit() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element(
				"patient",
				AssetType.DEFAULT,
				"The asset id of the Patient (mf-dicom-patient) record for which all associated data will be edited.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"study",
				AssetType.DEFAULT,
				"The asset id of the Study (mf-dicom-study) record for which all associated data will be edited.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"series",
				AssetType.DEFAULT,
				"The asset id of the Series (mf-dicom-series) record which will be edited.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"where",
				StringType.DEFAULT,
				"A query to restrict the assets (local to this server only); e.g. a namespace or date query. Don't use queries pertaining to the selection of document types or asset types.   ",
				0, 1));
		//

		_defn.add(new Interface.Element("group", StringType.DEFAULT,
				"The DICOM  group edit (e.g. 0010).", 1, 1));
		_defn.add(new Interface.Element("element", StringType.DEFAULT,
				"The DICOM element to edit (e.g. 0020).", 1, 1));
		_defn.add(new Interface.Element("value", StringType.DEFAULT,
				"The new value.", 1, 1));

	}

	@Override
	public String name() {

		return "dicom.header.edit";
	}

	@Override
	public String description() {

		return "Replace the value of a DICOM header element.";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_MODIFY;
	}

	@Override
	public boolean canBeAborted() {

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Parse
		Collection<String> patients = args.values("patient");
		Collection<String> studies = args.values("study");
		Collection<String> series = args.values("series");
		if (patients!=null) {
			if (studies!=null || series!=null) {
				throw new Exception ("You can only give one of patient/study/series");
			}
		} else {
			if (studies!=null && series !=null) {
				throw new Exception ("You can only give one of patient/study/series");
			}
		}
		//
		String where = args.value("where");
		if (where!=null) {
			if (patients !=null || studies!=null || series!=null) {
				throw new Exception ("You can only give one of 'where' and 'patient/study/series'");
			}
		}
		//
		if (patients==null && studies==null && series==null && where==null) {
			throw new Exception("Only one of 'where' and 'patient/study/series' must be suipplied");
		}
		//
		String group = args.value("group");
		String element = args.value("element");
		String val = args.value("value");


		// Find the DataSets with DICOM content. 
		Collection<String> assets = DICOMModelUtil.findSeries (executor(), where, patients, studies, series);
		int clevel = 6;

		// Iterate over Series assets.
		if (assets != null) {


			for (String asset : assets) {
				w.add("id", asset);

				// Make new temporary file for archive and temporary directory to unpack into
				File tempArchive = createTemporaryFile();
				File tempDir = createTemporaryDirectory();

				try {
					// Unpack content  into temporary directory
					String containerType = AssetUtil.getContentMimeType(executor(), asset);
					ArchiveUtil.unpackContent (executor(), asset, tempDir);

					// Edit files if required and put into DICOM files container
					DicomModify.editFiles (tempDir, group, element, val);

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
				} catch (Throwable t) {
					FileUtils.delete(tempDir);
					tempArchive.delete();
					throw t;
				}
			}
		}
	}
}
