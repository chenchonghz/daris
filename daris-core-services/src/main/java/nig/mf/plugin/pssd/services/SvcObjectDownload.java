package nig.mf.plugin.pssd.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectDownload extends PluginService {
	private Interface _defn;
	private static final Map<String, String> directoryPath;
	static {
		directoryPath = new HashMap<String, String>();
		directoryPath.put("project", "cid(-1)if-null(xpath(daris:pssd-object/name),'','_')replace(xpath(daris:pssd-object/name),'/','_')");
		directoryPath.put("subject", "cid(-4,-2)/" +
				"cid(-1)if-null(xpath(daris:pssd-object/name),'','_')replace(xpath(daris:pssd-object/name), '/', '_')");
		directoryPath.put("ex-method", "cid(-5,-3)/cid(-5,-2)/" +
				"cid(-1)if-null(xpath(daris:pssd-object/type),'','_')replace(xpath(daris:pssd-object/type), '/', '_')");
		directoryPath.put("study", "cid(-6,-4)/cid(-6,-3)/cid(-6,-2)/" +
				"cid(-1)if-null(xpath(daris:pssd-object/type),'','_')replace(xpath(daris:pssd-object/type), '/', '_')");
		directoryPath.put("dataset", "cid(-7,-5)/cid(-7,-4)/cid(-7,-3)/cid(-7,-2)/" +
				"cid(-1)if-null(xpath(daris:pssd-object/name),'','_')replace(xpath(daris:pssd-object/name), '/', '_')");
	}

	public SvcObjectDownload() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"the citable id of PSSD object which contents are required to download", 1, 1));
		_defn.add(new Interface.Element(
				"depth",
				IntegerType.DEFAULT,
				"the depth of the descendants  (from 0 to infinity);"
						+ "defaults to infinity; 0 stands for no descendants included",
				0, 1));
		Interface.Element e = new Interface.Element("format", new EnumType(
				new String[] { "aar", "zip", "tar" }),
				"select download format (default to zip)", 0, 1);
		Interface.Attribute a = new Interface.Attribute("compression-level", new EnumType(
				new String[] {"0","1","2","3","4","5","6","7","8","9"}), 
				"select compression level for file compress", 0);
		e.add(a);
		_defn.add(e);
		e = new Interface.Element("parts", new EnumType(new String[] { "content" }),
				"select contents to download, default to content", 0, 1);
		_defn.add(e);
		_defn.add(new Interface.Element("object-type", StringType.DEFAULT,
				"the object type user defined to download", 0, 1));
		e = new Interface.Element(
				"layout-pattern",
				StringType.DEFAULT,
				"the expression used to generate path. The format of path is Asset Path Language (APL)",
				0,Integer.MAX_VALUE);
		a = new Interface.Attribute("type", new EnumType(new String[] {
				"project", "subject", "ex-method", "study", "dataset" }),
				"object type that use the corresponding layout pattern", 1);
		e.add(a);
		_defn.add(e);
		_defn.add(new Interface.Element("query", StringType.DEFAULT,
				"specify the query for the data to be downloaded", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.DEFAULT, "Number of objects to downloads. Defaults to all.", 0, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Download metadata (in XML format) and/or content data " +
				"from object with user specified ID as aar/zip/tar format." +
				"User can also define the level of metadata and/or content need" +
				"to be downloaded based on input ID using \"depth\" argument.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {
		XmlDocMaker result = new XmlDocMaker("downloaded-dataset");
		String id = args.value("id");
		int depth = getDepth(args);
		String format = args.stringValue("format", "zip");
		int cLevel = 6;
		if (args.value("format/@clevel") != null){
			cLevel = Integer.parseInt(args.value("format/@clevel"));
		}
		String parts = args.stringValue("parts", "content");
		String query = args.value("query");
		//TODO: check what is the output of this element
		List<Element> layoutPatterns = args.elements("layout-pattern");
		Map<String, String> dirLayout = new HashMap<String, String>();
		if (layoutPatterns != null) {
		    for (Element e : layoutPatterns) {
				dirLayout.put(e.value("@type"),
						e.value());
			}
		}
		String qsize = args.stringValue("size", "infinity");
		
		// get queried cids
		List<Element> cids = new Vector<Element>();
		if (depth == -1){
			if (parts.equals("content")){
				StringBuilder sb = new StringBuilder();
				XmlDocMaker dm = new XmlDocMaker("args");
				sb.append(" ( cid = '" + id + "' or cid starts with '" + id + "' ) and asset has content");
				if (query != null){
					sb.append( " and " + query);
				}
				dm.add("where", sb.toString());
				dm.add("action", "get-cid");
				dm.add("size", qsize);
				XmlDoc.Element ele = executor().execute("asset.query", dm.root());
				if (ele.elements("cid") != null) {
					cids.addAll(ele.elements("cid"));
				}
			}
		} else {
			getCids(id, parts, depth, cids, query, qsize);
		}
		PluginTask.checkIfThreadTaskAborted();
		if (!cids.isEmpty()) {
			Iterator<Element> icids = cids.iterator();
			// download all the queried dataset content to a temporary directory
			File tempDirectory = PluginTask.createTemporaryDirectory();
			System.out.println(cids.size());
			PluginTask.threadTaskBeginSetOf(cids.size());
			while (icids.hasNext()) {
				PluginTask.checkIfThreadTaskAborted();
				File subDirectory = tempDirectory;
				Element e = icids.next();
				PluginTask.setCurrentThreadActivity("download content of "
						+ e.value() + " to temporary directory");
				result.add("cid", e.value());
				XmlDocMaker xdm = new XmlDocMaker("args");
				StringBuilder sb = new StringBuilder();
				sb.append("cid = '" + e.value() + "'");
				xdm.add("where", sb.toString());
				String[] attr = new String[] { "ename", "type" };
				xdm.add("xpath", attr, "content/type/@ext");
				xdm.add("action", "get-value");
				String dataType = executor().execute("asset.query", xdm.root())
						.element("asset/type").value();
				PluginService.Outputs sos = new PluginService.Outputs(1);
				xdm = new XmlDocMaker("args");
				xdm.add("cid", e.value());
				XmlDoc.Element r = executor().execute("asset.get", xdm.root(),
						null, sos);
				long size = r.longValue("asset/content/size");
				String type = r.value("asset/meta/daris:pssd-object/type");
				xdm = new XmlDocMaker("args");
				if (dirLayout.get(type) != null){
					xdm.add("expr", dirLayout.get(type));
				} else {
					xdm.add("expr",directoryPath.get(type));
				}
				xdm.add("id", r.value("asset/@id"));
				//create the path to generate directory
				XmlDoc.Element epath = executor().execute("asset.path.generate", xdm.root());
				String path = epath.value("path");
				//System.out.println(path);
				String [] sFile = path.split("/");
				PluginTask.checkIfThreadTaskAborted();
				if (sFile.length > 0){
					for (String f : sFile){
						File newFile = new File(subDirectory.getAbsolutePath() + File.separator + f);
						if (! newFile.exists()){
							newFile.mkdir();
							//System.out.println(f);
						}
						subDirectory = newFile;
					}
				}
				PluginTask.checkIfThreadTaskAborted();
				PluginService.Output so = sos.output(0);
				InputStream is = so.stream();
				LongInputStream lis = new SizedInputStream(is, size);
				NamedMimeType mimeType = new NamedMimeType(
						r.value("asset/content/type"));
				PluginTask.setCurrentThreadActivity("extracting the content of " + e.value());
				PluginTask.checkIfThreadTaskAborted();
				if (ArchiveRegistry.isAnArchive(mimeType)) {
					// output the downloaded dataset's cid
					if (dataType.equals("tar")) {
						ArchiveInput ai = ArchiveRegistry.createInput(lis,
								mimeType, ArchiveInput.ACCESS_SEQUENTIAL);
						ArchiveExtractor.extract(ai, subDirectory,
								true, true, false);

					} else {
						ArchiveInput ai = ArchiveRegistry.createInput(lis,
								mimeType, ArchiveInput.ACCESS_RANDOM);
						ArchiveExtractor.extract(ai, subDirectory,
								true, true, false);

					}
				} else {
					byte[] buffer = new byte[1024];
					int len;
					OutputStream fos = new FileOutputStream(new File(
							subDirectory.getAbsolutePath() + File.separator
									+ r.value("asset/@id") + "." + dataType));
					while ((len = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
					}
					fos.flush();
					fos.close();
				}
				PluginTask.checkIfThreadTaskAborted();
			}
			// TODO: check the size of each decompressed file
			Map<Double, Integer> dirInfo = getFolderSize(tempDirectory);
			double length = dirInfo.entrySet().iterator().next().getKey();
			int countDir = dirInfo.entrySet().iterator().next().getValue();
			result.add("total-file-size", getFileSize(length));
			// result.add("length", length);
			// result.add("sub-dir-sum", countDir);
			w.add(result.root());
			// zip limitation is 4GB or 65535 file entries
			if (format.equals("zip") && (length / Math.pow(1024, 3) >= 4 || countDir >= 65535) ) {
				throw new Exception(
						"Data amount exceeded Zip capacity. Can not use zip to compress the data. "
								+ "Please use other format to download the data or reduce the amount of queried data to download");
			}
			// Zip all the content in the temporary directory
			// Download the zipped file to user outputs
			PluginService.Output out = outputs.output(0);
			PluginTask.setCurrentThreadActivity("write content from temp directory to user defined termination");
			PluginTask.checkIfThreadTaskAborted();
			if (format.equals("zip")) {
				Format f = new SimpleDateFormat("yyyy-MM-dd");
				Date d = new Date();
				String zipFileName = f.format(d) + '.' + format;
				File zipFile = new File(tempDirectory.toString()
						+ File.separator + zipFileName);
				String type = "application/x-zip";
				ArchiveOutput ao = ArchiveRegistry.createOutput(zipFile, type, cLevel, null);
				PluginTask.checkIfThreadTaskAborted();
				PluginTask.setCurrentThreadActivity("compress content as zip format");
				compressDirectory(ao, tempDirectory, zipFileName, tempDirectory.getAbsolutePath());
				PluginTask.checkIfThreadTaskAborted();
				ao.end();
				ao.close();
				out.setData(new TempFileInputStream(zipFile), zipFile.length(),type);
			} else if (format.equals("tar")) {
				Format f = new SimpleDateFormat("yyyy-MM-dd");
				Date d = new Date();
				String tarFileName = f.format(d) + '.' +format;
				File tarFile = new File(tempDirectory.toString()
						+ File.separator + tarFileName);
				String type = "application/x-tar";
				ArchiveOutput ao = ArchiveRegistry.createOutput(tarFile, type, cLevel, null);
				PluginTask.checkIfThreadTaskAborted();
				PluginTask.setCurrentThreadActivity("compress content as tar format");
				compressDirectory(ao, tempDirectory, tarFileName, tempDirectory.getAbsolutePath());
				PluginTask.checkIfThreadTaskAborted();
				ao.end();
				ao.close();
				out.setData(new TempFileInputStream(tarFile), tarFile.length(),type);
			} else if (format.equals("aar")) {
				Format f = new SimpleDateFormat("yyyy-MM-dd");
				Date d = new Date();
				String tarFileName = f.format(d) + '.' + format;
				File tarFile = new File(tempDirectory.toString()
						+ File.separator + tarFileName);
				String type = "application/arc-archive";
				ArchiveOutput ao = ArchiveRegistry.createOutput(tarFile, type, cLevel, null);
				PluginTask.checkIfThreadTaskAborted();
				PluginTask.setCurrentThreadActivity("compress content as aar format");
				compressDirectory(ao, tempDirectory, tarFileName, tempDirectory.getAbsolutePath());
				PluginTask.checkIfThreadTaskAborted();
				ao.end();
				ao.close();
				out.setData(new TempFileInputStream(tarFile), tarFile.length(),type);
			}

			// clean-up temporary directory
			deleteDirectory(tempDirectory);
			PluginTask.threadTaskCompleted();
		} else {
			throw new Exception("No object meet selection criteria, no data has been downloaded");
		}

	}

	public int getDepth(Element args) throws Throwable {
		int depth = -1;
		String d = args.value("depth");
		if (d != null) {
			if (Integer.parseInt(d) < 0) {
				throw new Exception(
						"depth must be greater than 0, (default to infinity)");
			} else {
				depth = Integer.parseInt(args.value("depth"));
			}
		}
		return depth;
	}

	public void getCids(String id, String parts, int depth,
			List<Element> cids, String query, String qsize) throws Throwable {
		if (parts.equals("content")) {
			StringBuilder sb = new StringBuilder();
			XmlDoc.Element ele = new Element("args");
			XmlDocMaker dm = new XmlDocMaker("args");
			depth = depth - 1;
			sb.append("cid='" + id + "' and asset has content");
			if (query != null){
				sb.append(" and " + query);
			}
			dm.add("where", sb.toString());
			dm.add("action", "get-cid");
			ele = executor().execute("asset.query", dm.root());
			if (ele.elements("cid") != null) {
				cids.addAll(ele.elements("cid"));
			}
			if (depth >= 0) {
				dm = new XmlDocMaker("args");
				sb = new StringBuilder();
				ele = new Element("args");
				sb.append("cid in '" + id + "'");
				dm.add("where", sb.toString());
				dm.add("action", "get-cid");
				dm.add("size", qsize);
				ele = executor().execute("asset.query", dm.root());
				if (ele.elements("cid") != null) {
					Iterator<Element> it = ele.elements("cid").iterator();
					while (it.hasNext()) {
						String cid = it.next().value();
						if (cid != null) {
							getCids(cid, parts, depth, cids, query, qsize);
						}
					}
				}
			}
		} else if (parts.equals("meta")) {
			//TODO: only download metadata
		} else {
			//TODO: download both meta and content data, thank about how to store?

		}
	}

	public Map<Double, Integer> getFolderSize(File tempDirectory) {
		double length = 0;
		int countDir = 0;
		for (File file : tempDirectory.listFiles()) {
			if (file.isFile()) {
				length += file.length();
			} else {
				countDir = countDir + 1;
				length += getFolderSize(file).entrySet().iterator().next()
						.getKey();
				countDir += getFolderSize(file).entrySet().iterator().next()
						.getValue();
			}
		}
		Map<Double, Integer> dirInfo = new HashMap<Double, Integer>();
		dirInfo.put(length, countDir);
		return dirInfo;
	}

	public void deleteDirectory(File tempDirectory) throws Throwable {
		for (File f : tempDirectory.listFiles()) {
			if (f.isDirectory()) {
				deleteDirectory(f);
			} else {
				f.delete();
			}
		}
		tempDirectory.delete();
	}

	public String getFileSize(double length) {
		if (length <= 0) {
			return "0";
		} else {
			final String[] units = new String[] { "B", "KB", "MB", "GB", "TB",
					"EB" };
			int ind = (int) (Math.log10(length) / Math.log10(1024));
			return String.format("%.1f %s", length / Math.pow(1024, ind),
					units[ind]);
		}
	}
	
	public void compressDirectory(ArchiveOutput ao, File tempDirectory, String fileName, String tempDirectoryPath) throws Throwable{
		for (File f : tempDirectory.listFiles()){
			if (f.isFile()){
				//MimeType mt = new NamedMimeType(ArchiveRegistry.createInput(f).mimeType());
				FileInputStream fis = new FileInputStream(f);
				SizedInputStream fiss = new SizedInputStream(fis, f.length());
				String fullName = f.getName();
				String name = FilenameUtils.getBaseName(fullName) + '.' + FilenameUtils.getExtension(fullName);
				//System.out.println(name);
				if (! name.equals(fileName)){
					name = f.getAbsolutePath().substring(tempDirectoryPath.length()+1, f.getAbsolutePath().length());
					ao.add(null, name, fiss);
				}
				fiss.close();
				fis.close();
			} else if (f.isDirectory()){
				compressDirectory (ao ,f, fileName, tempDirectoryPath);
			}
		}
	}

	public class TempFileInputStream extends FileInputStream {

		private File _file;

		public TempFileInputStream(File file) throws FileNotFoundException {

			super(file);
			_file = file;

		}

		public void close() throws IOException {

			super.close();
			_file.delete();

		}
	}

	@Override
	public String name() {
		return "om.pssd.object.download";
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}
	
	@Override
	public boolean canBeAborted(){
		return true;
	}
}
