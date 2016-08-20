package daris.plugin.experimental.mytardis.services;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.output.NullOutputStream;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.dicom.util.DicomFileCheck;

public class SvcMyTardisDatasetImport extends PluginService {

	public static final String SERVICE_NAME = "daris.mytardis.dataset.import";

	private Interface _defn;

	public SvcMyTardisDatasetImport() {
		_defn = new Interface();

		Interface.Element experiment = new Interface.Element("experiment", XmlDocType.DEFAULT,
				"MyTardis experiment information.", 1, 1);
		experiment.add(new Interface.Element("id", LongType.POSITIVE_ONE, "MyTardis experiment id.", 1, 1));
		experiment.add(new Interface.Element("title", StringType.DEFAULT, "MyTardis experiment title.", 0, 1));
		experiment.add(
				new Interface.Element("description", StringType.DEFAULT, "MyTardis experiment description.", 0, 1));
		_defn.add(experiment);

		Interface.Element dataset = new Interface.Element("dataset", XmlDocType.DEFAULT,
				"MyTardis dataset information.", 1, 1);
		dataset.add(new Interface.Element("id", LongType.POSITIVE_ONE, "MyTardis dataset id.", 1, 1));
		dataset.add(new Interface.Element("description", StringType.DEFAULT, "MyTardis dataset description.", 0, 1));
		dataset.add(new Interface.Element("instrument", StringType.DEFAULT, "Instrument.", 0, 1));
		_defn.add(dataset);

		_defn.add(new Interface.Element("source-mytardis-uri", UrlType.DEFAULT, "The source MyTardis server address.",
				1, 1));
		_defn.add(new Interface.Element("dicom-ingest", BooleanType.DEFAULT,
				"Ingest the contained DICOM data files as DaRIS DICOM datasets via DaRIS DICOM server engine. Defaults to true.",
				0, 1));
		_defn.add(new Interface.Element("project", CiteableIdType.DEFAULT,
				"The citeable id of the DaRIS project to import to.", 1, 1));

		_defn.add(new Interface.Element("async", BooleanType.DEFAULT,
				"Run the service asynchronously. Defaults to true.", 0, 1));
	}

	@Override
	public Access access() {
		return ACCESS_MODIFY;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Import a MyTardis dataset into DaRIS project.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		final String expId = args.value("experiment/id");
		final String expTitle = args.value("experiment/title");
		final String expDescription = args.value("experiment/description");
		final String datasetId = args.value("dataset/id");
		final String datasetDescription = args.value("dataset/description");
		final String instrument = args.value("dataset/instrument");
		final String mytardisUri = args.value("source-mytardis-uri");
		final boolean dicomIngest = args.booleanValue("dicom-ingest", true);
		final String projectCid = args.value("project");
		final String sourceUri = getMyTardisDatasetUri(mytardisUri, expId, datasetId);
		final PluginService.Input input = inputs.input(0);
		final boolean async = args.booleanValue("async", true);
		final boolean datasetExists = datasetExists(executor(), projectCid, expId, expTitle, expDescription, datasetId,
				datasetDescription, instrument, sourceUri);

		if (datasetExists && !dicomIngest) {
			try {
				input.close();
				input.stream().close();
			} catch (Throwable e) {
				e.printStackTrace(System.out);
			}
			return;
		}

		if (async) {
			PluginThread.executeAsync("Import MyTardis dataset", new Runnable() {

				@Override
				public void run() {
					try {
						importMyTardisDataset(executor(), projectCid, expId, expTitle, expDescription, datasetId,
								datasetDescription, instrument, sourceUri, input, datasetExists, dicomIngest);
					} catch (Throwable e) {
						e.printStackTrace(System.out);
					}
				}
			});
		} else {
			importMyTardisDataset(executor(), projectCid, expId, expTitle, expDescription, datasetId,
					datasetDescription, instrument, sourceUri, input, datasetExists, dicomIngest);
		}

	}

	private static void importMyTardisDataset(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDescription, String datasetId, String datasetDescription, String instrument,
			String sourceUri, PluginService.Input input, boolean datasetExists, boolean dicomIngest) throws Throwable {
		File dir = PluginTask.createTemporaryDirectory();
		File dicomDir = PluginTask.createTemporaryDirectory();
		try {
			List<File> dicomFiles = new ArrayList<File>();

			// extract the input archive to two directories:

			// 1) all content files, including dicom data, are extracted to dir/

			// 2) all dicom data are extracted to dicomDir/
			extract(input, dir, dicomDir, dicomFiles);
			String subjectCid = findOrCreateSubject(executor, projectCid, expId, expTitle, expDescription, datasetId,
					datasetDescription, instrument, sourceUri);
			if (!datasetExists) {
				// Only create the dataset if it does not exist
				String studyCid = findOrCreateStudy(executor, subjectCid, expId, expTitle, expDescription, datasetId,
						datasetDescription, instrument, sourceUri);
				createDataset(executor, studyCid, expId, expTitle, expDescription, datasetId, datasetDescription,
						instrument, sourceUri, dir);
			}
			if (dicomIngest) {
				// ingest dicom data
				ingestDicomData(executor, subjectCid, dicomDir, dicomFiles);
			}
		} finally {
			deleteDirectory(dir);
			deleteDirectory(dicomDir);
		}
	}

	private static void deleteDirectory(File dir) throws Throwable {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return;
		}
		Path path = dir.toPath();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				if (e == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					throw e;
				}
			}

		});
	}

	private static void ingestDicomData(ServiceExecutor executor, String subjectCid, File dicomDir,
			List<File> dicomFiles) throws Throwable {
		String mimeTypeAAR = "application/arc-archive";
		File dcmArchive = PluginTask.createTemporaryArchive(dicomDir.getAbsolutePath(), dicomDir, mimeTypeAAR, 0);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("engine", "nig.dicom");
		dm.add("arg", new String[] { "name", "nig.dicom.id.ignore-non-digits" }, "true");
		dm.add("arg", new String[] { "name", "nig.dicom.subject.create" }, "true");
		dm.add("arg", new String[] { "name", "nig.dicom.id.citable" }, subjectCid);
		dm.add("arg", new String[] { "name", "nig.dicom.write.mf-dicom-patient" }, true);
		dm.add("wait", true);
		dm.add("type", mimeTypeAAR);
		InputStream in = PluginTask.deleteOnCloseInputStream(dcmArchive);
		PluginService.Input input = new PluginService.Input(in, dcmArchive.length(), mimeTypeAAR, null);
		try {
			executor.execute("dicom.ingest", dm.root(), new PluginService.Inputs(input), null);
		} finally {
			input.close();
			in.close();
			Files.deleteIfExists(dcmArchive.toPath());
		}
	}

	private static String findOrCreateSubject(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDesc, String datasetId, String datasetDescription, String instrument,
			String sourceUri) throws Throwable {
		String methodCid = executor.execute("asset.get", "<args><cid>" + projectCid + "</cid></args>", null, null)
				.value("asset/meta/daris:pssd-project/method/id");
		if (methodCid == null) {
			throw new Exception("No method is set for project " + projectCid);
		}
		String subjectCid = executor.execute("asset.query",
				"<args><where>cid in '" + projectCid
						+ "' and model='om.pssd.subject' and cid contains (xpath(daris:mytardis-dataset/uri)='"
						+ sourceUri + "' and model='om.pssd.dataset')</where><action>get-cid</action></args>",
				null, null).value("cid");
		if (subjectCid == null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("pid", projectCid);
			dm.add("method", methodCid);
			dm.add("fillin", true);
			String description = descriptionFor(expId, expTitle, expDesc, datasetId, datasetDescription, instrument,
					sourceUri);
			dm.add("description", description);
			subjectCid = executor.execute("om.pssd.subject.create", dm.root()).value("id");
		}
		return subjectCid;
	}

	private static String descriptionFor(String expId, String expTitle, String expDesc, String datasetId,
			String datasetDescription, String instrument, String sourceUri) {
		StringBuilder sb = new StringBuilder();
		sb.append("mytardis experiment id: ").append(expId).append("\n");
		if (expTitle != null) {
			sb.append("mytardis experiment title: ").append(expTitle).append("\n");
		}
		if (expDesc != null) {
			sb.append("mytardis experiment description: ").append(expDesc).append("\n");
		}
		sb.append("mytardis dataset id: ").append(datasetId).append("\n");
		if (datasetDescription != null) {
			sb.append("mytardis dataset description: ").append(datasetDescription).append("\n");
		}
		if (instrument != null) {
			sb.append("mytardis dataset instrument: ").append(instrument).append("\n");
		}
		sb.append("mytardis dataset uri: ").append(sourceUri).append("\n");
		return sb.toString();
	}

	private static String findOrCreateStudy(ServiceExecutor executor, String subjectCid, String expId, String expTitle,
			String expDesc, String datasetId, String datasetDescription, String instrument, String sourceUri)
					throws Throwable {
		String studyCid = executor.execute("asset.query",
				"<args><where>cid starts with '" + subjectCid
						+ "' and model='om.pssd.study' and cid contains (model='om.pssd.dataset' and xpath(daris:mytardis-dataset/uri)='"
						+ sourceUri + "')</where><action>get-cid</action></args>",
				null, null).value("cid");
		if (studyCid == null) {
			String exMethodCid = executor.execute("asset.query",
					"<args><where>cid in '" + subjectCid + "'</where><action>get-cid</action></args>", null, null)
					.value("cid");
			if (exMethodCid == null) {
				throw new Exception("No ex-method is found in subject " + subjectCid);
			}
			// NOTE: pick the first step. We need to improve this when this
			// service is used for production.
			String exMethodStep = executor.execute("om.pssd.ex-method.study.step.find",
					"<args><id>" + exMethodCid + "</id></args>", null, null).value("ex-method/step");
			if (exMethodStep == null) {
				throw new Exception("No step is found in ex-method " + exMethodCid);
			}
			// create study
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("pid", exMethodCid);
			dm.add("step", exMethodStep);
			if (expTitle != null) {
				dm.add("name", expTitle);
			}
			String description = descriptionFor(expId, expTitle, expDesc, datasetId, datasetDescription, instrument,
					sourceUri);
			if (expDesc != null) {
				dm.add("description", description + "\n\n" + expDesc);
			} else {
				dm.add("description", description);
			}
			dm.add("fillin", true);
			dm.push("meta");
			dm.push("daris:mytardis-experiment");
			dm.add("id", expId);
			if (expTitle != null) {
				dm.add("title", expTitle);
			}
			if (expDesc != null) {
				dm.add("description", expDesc);
			}
			dm.pop();
			dm.pop();
			studyCid = executor.execute("om.pssd.study.create", dm.root()).value("id");
		}
		return studyCid;
	}

	private static boolean datasetExists(ServiceExecutor executor, String projectCid, String expId, String expTitle,
			String expDesc, String datasetId, String datasetDescription, String instrument, String sourceUri)
					throws Throwable {
		long count = executor.execute("asset.query",
				"<args><where>cid starts with '" + projectCid
						+ "' and model='om.pssd.dataset' and xpath(daris:mytardis-dataset/uri)='" + sourceUri
						+ "'</where><action>count</action></args>",
				null, null).longValue("value", 0);
		return count > 0;
	}

	private static String createDataset(ServiceExecutor executor, String studyCid, String expId, String expTitle,
			String expDescription, String datasetId, String datasetDescription, String instrument, String sourceUri,
			File dir) throws Throwable {
		XmlDoc.Element studyAE = executor.execute("asset.get", "<args><cid>" + studyCid + "</cid></args>", null, null)
				.element("asset");
		String exMethodCid = studyAE.value("meta/daris:pssd-study/method");
		String exMethodStep = studyAE.value("meta/daris:pssd-study/method/@step");

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pid", studyCid);
		dm.push("method");
		dm.add("id", exMethodCid);
		dm.add("step", exMethodStep);
		dm.pop();
		if (datasetDescription != null) {
			dm.add("name", datasetDescription.length() > 32 ? datasetDescription.substring(0, 32) : datasetDescription);
		}
		String description = descriptionFor(expId, expTitle, expDescription, datasetId, datasetDescription, instrument,
				sourceUri);
		dm.add("description", description);
		dm.add("fillin", true);
		String filename = "mytardis-dataset-" + datasetId + ".zip";
		dm.add("filename", filename);
		dm.push("meta");
		dm.push("daris:mytardis-dataset");
		dm.add("id", datasetId);
		if (datasetDescription != null) {
			dm.add("description", datasetDescription);
		}
		if (instrument != null) {
			dm.add("instrument", instrument);
		}
		dm.add("uri", sourceUri);
		dm.pop();
		dm.pop();
		String arcMimeType = "application/arc-archive";
		File arcFile = PluginTask.createTemporaryArchive(dir.getAbsolutePath(), dir, arcMimeType, 0);
		InputStream in = PluginTask.deleteOnCloseInputStream(arcFile);
		PluginService.Input input = new PluginService.Input(in, arcFile.length(), arcMimeType, filename);
		try {
			return executor
					.execute("om.pssd.dataset.derivation.create", dm.root(), new PluginService.Inputs(input), null)
					.value("id");
		} finally {
			input.close();
			in.close();
			Files.deleteIfExists(arcFile.toPath());
		}
	}

	private static void extract(PluginService.Input input, File dir, File dicomDir, List<File> dicomFiles)
			throws Throwable {
		ArchiveInput ai = ArchiveRegistry.createInput(new SizedInputStream(input.stream(), input.length()),
				new NamedMimeType(input.mimeType()));
		try {
			ArchiveInput.Entry e = ai.next();
			while (e != null) {
				if (!e.isDirectory()) {
					extract(e, dir, dicomDir, dicomFiles);
				}
				e = ai.next();
			}
		} finally {
			ai.close();
			input.stream().close();
		}
	}

	private static void extract(ArchiveInput.Entry e, File dir, File dicomDir, List<File> dicomFiles) throws Throwable {
		InputStream in = e.stream();
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		/*
		 * check if the entry is an archive file.
		 */
		boolean isArchive = !e.isDirectory() && e.name() != null
				&& (e.name().toLowerCase().endsWith(".zip") || e.name().toLowerCase().endsWith(".jar")
						|| e.name().toLowerCase().endsWith(".tar") || e.name().toLowerCase().endsWith(".tar.gz")
						|| e.name().toLowerCase().endsWith(".tgz") || e.name().toLowerCase().endsWith(".tar.bz2")
						|| e.name().toLowerCase().endsWith(".tbz") || e.name().toLowerCase().endsWith(".tb2")
						|| e.name().toLowerCase().endsWith(".tbz2"));
		/*
		 * check if the entry is a dicom file.
		 */
		boolean isDicom = false;
		if (!e.isDirectory() && e.name() != null) {
			if ("application/dicom".equals(e.mimeType()) || e.name().toLowerCase().endsWith(".dcm")) {
				isDicom = true;
			} else {
				// check dicom file preamble
				in.mark(132);
				byte[] b = new byte[132];
				int n = 0;
				while (n < b.length) {
					int count = in.read(b, n, b.length - n);
					if (count < 0)
						throw new EOFException();
					n += count;
				}
				in.reset();
				isDicom = "DICM".equals(new String(b, 128, 4));
			}
		}

		if (isArchive) {
			String archiverName = getArchiverName(e.name());
			if (dir != null) {
				File outputFile = new File(dir, e.name());
				outputFile.getParentFile().mkdirs();
				StreamCopy.copy(in, outputFile);
				extractDicomFiles(new BufferedInputStream(new FileInputStream(outputFile)), archiverName, dicomDir,
						dicomFiles);
			} else {
				extractDicomFiles(in, archiverName, dicomDir, dicomFiles);
			}
		} else if (isDicom) {
			File dicomFile = new File(dicomDir, String.format("%08d.dcm", dicomFiles.size() + 1));
			if (dir != null) {
				File outputFile = new File(dir, e.name());
				outputFile.getParentFile().mkdirs();
				StreamCopy.copy(in, outputFile);
				StreamCopy.copy(outputFile, dicomFile);
			} else {
				StreamCopy.copy(in, dicomFile);
			}
			dicomFiles.add(dicomFile);
		} else {
			if (dir != null) {
				File outputFile = new File(dir, e.name());
				outputFile.getParentFile().mkdirs();
				StreamCopy.copy(in, outputFile);
			} else {
				consumeInputStream(in);
			}
		}
	}

	private static String getArchiverName(String filename) throws Throwable {
		if (filename != null) {
			if (filename.toLowerCase().endsWith(".zip")) {
				return ArchiveStreamFactory.ZIP;
			} else if (filename.toLowerCase().endsWith(".jar")) {
				return ArchiveStreamFactory.JAR;
			} else if (filename.toLowerCase().endsWith(".tar") || filename.toLowerCase().endsWith(".tar.gz")
					|| filename.toLowerCase().endsWith(".tgz") || filename.toLowerCase().endsWith(".tar.bz2")
					|| filename.toLowerCase().endsWith(".tbz") || filename.toLowerCase().endsWith(".tbz2")
					|| filename.toLowerCase().endsWith(".tb2")) {
				return ArchiveStreamFactory.TAR;
			} else if (filename.toLowerCase().endsWith(".7z")) {
				return ArchiveStreamFactory.SEVEN_Z;
			}
		}
		throw new IOException("Unknonwn archive format: " + filename);
	}

	private static void extractDicomFiles(InputStream in, String archiverName, File dicomDir, List<File> dicomFiles)
			throws Throwable {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		ArchiveInputStream ai = new ArchiveStreamFactory().createArchiveInputStream(archiverName, in);
		try {
			ArchiveEntry ae = ai.getNextEntry();
			while (ae != null) {
				if (!ae.isDirectory()) {
					extractDicomFile(ai, ae, dicomDir, dicomFiles);
				}
				ae = ai.getNextEntry();
			}

		} finally {
			ai.close();
		}
	}

	private static void extractDicomFile(ArchiveInputStream ai, ArchiveEntry ae, File dicomDir, List<File> dicomFiles)
			throws Throwable {

		String name = ae.getName();
		File dicomFile = new File(dicomDir, String.format("%08d.dcm", dicomFiles.size() + 1));
		if (name.toLowerCase().endsWith(".dcm")) {
			StreamCopy.copy(ai, dicomFile);
		} else {
			File tf = PluginTask.createTemporaryFile();
			try {
				StreamCopy.copy(ai, tf);
				if (DicomFileCheck.isDicomFile(tf)) {
					Files.move(tf.toPath(), dicomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					PluginTask.deleteTemporaryFile(tf);
				}
			} finally {
				if (tf.exists()) {
					PluginTask.deleteTemporaryFile(tf);
				}
			}
		}
	}

	private static void consumeInputStream(InputStream in) throws Throwable {
		OutputStream out = new NullOutputStream();
		try {
			StreamCopy.copy(in, out);
		} finally {
			out.close();
		}
	}

	private static String getMyTardisDatasetUri(String mytardisUri, String expId, String datasetId) {
		String baseUri = mytardisUri;
		while (baseUri.endsWith("/")) {
			baseUri = baseUri.substring(0, baseUri.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(baseUri);
		sb.append("/experiment-").append(expId);
		sb.append("/dataset-").append(datasetId);
		return sb.toString();
	}

	@Override
	public int minNumberOfInputs() {
		return 1;
	}

	@Override
	public int maxNumberOfInputs() {
		return 1;
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
