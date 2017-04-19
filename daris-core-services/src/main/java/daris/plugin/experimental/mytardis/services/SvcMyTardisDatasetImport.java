package daris.plugin.experimental.mytardis.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveOutput;
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

	public static final String AAR_FILE_EXT = "aar";

	public static final String AAR_MIME_TYPE = "application/arc-archive";

	public static final String DICOM_MIME_TYPE = "application/dicom";

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
		_defn.add(new Interface.Element("encrypt-patient", BooleanType.DEFAULT,
						"Encrypt patient meta-data in mf-dicom-patient-encrypted, else use mf-dicom-patient. Defaults to true.", 0, 1));
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
		final String mytardisUri = args.value("source-mytardis-uri");

		final String expId = args.value("experiment/id");
		final String expTitle = args.value("experiment/title");
		final String expDescription = args.value("experiment/description");
		final String expUri = getMyTardisExperimentUri(mytardisUri, expId);

		final String datasetId = args.value("dataset/id");
		final String datasetDescription = args.value("dataset/description");
		final String instrument = args.value("dataset/instrument");
		final String datasetUri = getMyTardisDatasetUri(mytardisUri, datasetId);

		final String projectCid = args.value("project");

		final boolean dicomIngest = args.booleanValue("dicom-ingest", true);

		final PluginService.Input input = inputs.input(0);
		final boolean async = args.booleanValue("async", true);
		final boolean encrypt = args.booleanValue("encrypt-patient", true);
		final boolean datasetExists = datasetExists(executor(), projectCid, datasetUri);

		if (datasetExists && !dicomIngest) {
			try {
				input.stream().close();
				input.close();
			} catch (Throwable e) {
				e.printStackTrace(System.out);
			}
			return;
		}

		/*
		 * receive the data and extract it into a temp directory.
		 */
		final File dir = PluginTask.createTemporaryDirectory();
		ArchiveInput ai = ArchiveRegistry.createInput(new SizedInputStream(input.stream(), input.length()),
				new NamedMimeType(input.mimeType()));
		try {
			ArchiveExtractor.extract(ai, dir, true, true, false);
		} finally {
			ai.close();
			input.stream().close();
			input.close();
		}

		if (async) {
			PluginThread.executeAsync("Import MyTardis dataset", new Runnable() {

				@Override
				public void run() {
					try {
						importMyTardisDataset(executor(), projectCid, expId, expTitle, expDescription, expUri,
								datasetId, datasetDescription, instrument, datasetUri, dir, datasetExists, dicomIngest,
								encrypt);
					} catch (Throwable e) {
						e.printStackTrace(System.out);
					}
				}
			});
		} else {
			importMyTardisDataset(executor(), projectCid, expId, expTitle, expDescription, expUri, datasetId,
					datasetDescription, instrument, datasetUri, dir, datasetExists, dicomIngest, encrypt);
		}

	}

	private static void importMyTardisDataset(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDescription, String expUri, String datasetId, String datasetDescription,
			String instrument, String datasetUri, File dir, boolean datasetExists, boolean dicomIngest, 
			boolean encrypt) throws Throwable {
		try {
			String subjectCid = findOrCreateSubject(executor, projectCid, expId, expTitle, expDescription, expUri,
					datasetId, datasetDescription, instrument, datasetUri);
			String studyCid = findOrCreateStudy(executor, subjectCid, expId, expTitle, expDescription, expUri,
					datasetId, datasetDescription, instrument, datasetUri);
			if (!datasetExists) {
				// Only create the dataset if it does not exist
				createDataset(executor, studyCid, expId, expTitle, expDescription, datasetId, datasetDescription,
						instrument, datasetUri, dir);
			}
			if (dicomIngest) {
				File dicomArchive = PluginTask.createTemporaryFile("dcm." + AAR_FILE_EXT);
				try {
					ArchiveOutput ao = ArchiveRegistry.createOutput(dicomArchive, AAR_MIME_TYPE, 0, null);
					try {
						int[] counter = new int[] { 0 };
						addDicomFiles(dir, ao, counter);
					} finally {
						ao.close();
					}
					ingestDicomData(executor, studyCid, dicomArchive, encrypt);
				} catch (Throwable e) {
					// NOTE: DO NOT terminate if there is dicom ingest error.
					e.printStackTrace(System.out);
				} finally {
					Files.deleteIfExists(dicomArchive.toPath());
				}
			}
		} finally {
			try {
				FileUtils.forceDelete(dir);
			} catch (Throwable e) {
				if(FileUtils.deleteQuietly(dir)){
					e.printStackTrace(System.out);
				}
			}
		}
	}

	private static void addDicomFiles(File sourceDir, final ArchiveOutput out, final int[] counter) throws Throwable {
		Path path = sourceDir.toPath();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				File file = path.toFile();
				String name = file.getName();
				try {
					/*
					 * check if the entry is an archive file.
					 */
					if (name.endsWith(".zip") || name.toLowerCase().endsWith(".jar")
							|| name.toLowerCase().endsWith(".tar") || name.toLowerCase().endsWith(".tar.gz")
							|| name.toLowerCase().endsWith(".tgz") || name.toLowerCase().endsWith(".tar.bz2")
							|| name.toLowerCase().endsWith(".tbz") || name.toLowerCase().endsWith(".tb2")
							|| name.toLowerCase().endsWith(".tbz2")) {
						ArchiveInputStream in = getArchiveInputStream(file);
						try {
							addDicomFiles(in, out, counter);
						} finally {
							in.close();
						}
					}
					/*
					 * check if the entry is a dicom file.
					 */
					if (DicomFileCheck.isDicomFile(file)) {
						String dicomFileName = String.format("%08d.dcm", counter[0] + 1);
						out.add(DICOM_MIME_TYPE, dicomFileName, file);
						counter[0] += 1;
					}
					return FileVisitResult.CONTINUE;
				} catch (Throwable e) {
					throw new IOException(e);
				}
			}
		});
	}

	private static void addDicomFiles(ArchiveInputStream in, ArchiveOutput out, int[] counter) throws Throwable {
		ArchiveEntry ae = in.getNextEntry();
		while (ae != null) {
			if (!ae.isDirectory()) {
				String dicomFileName = String.format("%08d.dcm", counter[0] + 1);
				File tf = PluginTask.createTemporaryFile();
				try {
					StreamCopy.copy(in, tf);
					if (DicomFileCheck.isDicomFile(tf)) {
						out.add(DICOM_MIME_TYPE, dicomFileName, tf);
					}
				} finally {
					PluginTask.deleteTemporaryFile(tf);
				}
				counter[0] += 1;
			}
			ae = in.getNextEntry();
		}
	}

	private static void ingestDicomData(ServiceExecutor executor, String studyCid, File dicomArchive, boolean encryptPatient) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("engine", "nig.dicom");
		dm.add("arg", new String[] { "name", "nig.dicom.id.ignore-non-digits" }, "true");
		dm.add("arg", new String[] { "name", "nig.dicom.subject.create" }, "true");
		dm.add("arg", new String[] { "name", "nig.dicom.id.citable" }, studyCid);
		dm.add("arg", new String[] { "name", "nig.dicom.write.mf-dicom-patient" }, true);
		if (encryptPatient) {
			dm.add("arg", new String[] {"name", "nig.dicom.encrypt.patient.metadata"}, true);

		}
		dm.add("wait", true);
		dm.add("type", AAR_MIME_TYPE);
		PluginService.Input input = new PluginService.Input(PluginTask.deleteOnCloseInputStream(dicomArchive),
				dicomArchive.length(), AAR_MIME_TYPE, null);
		try {
			executor.execute("dicom.ingest", dm.root(), new PluginService.Inputs(input), null);
		} finally {
			input.close();
		}
	}

	private static String findOrCreateSubject(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDesc, String expUri, String datasetId, String datasetDescription,
			String instrument, String datasetUri) throws Throwable {
		String methodCid = executor.execute("asset.get", "<args><cid>" + projectCid + "</cid></args>", null, null)
				.value("asset/meta/daris:pssd-project/method/id");
		if (methodCid == null) {
			throw new Exception("No method is set for project " + projectCid);
		}
		String subjectCid = executor.execute("asset.query",
				"<args><where>cid in '" + projectCid
						+ "' and model='om.pssd.subject' and cid contains (xpath(daris:mytardis-dataset/uri)='"
						+ datasetUri + "' and model='om.pssd.dataset')</where><action>get-cid</action></args>",
				null, null).value("cid");
		if (subjectCid == null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("pid", projectCid);
			dm.add("method", methodCid);
			dm.add("fillin", true);
			String description = descriptionFor(expId, expTitle, expDesc, datasetId, datasetDescription, instrument,
					datasetUri);
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
			String expDesc, String expUri, String datasetId, String datasetDescription, String instrument,
			String sourceUri) throws Throwable {
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
			dm.add("uri", expUri);
			dm.pop();
			dm.pop();
			studyCid = executor.execute("om.pssd.study.create", dm.root()).value("id");
		}
		return studyCid;
	}

	private static boolean datasetExists(ServiceExecutor executor, String projectCid, String sourceDatasetUri)
			throws Throwable {
		long count = executor
				.execute("asset.query",
						"<args><where>cid starts with '" + projectCid
								+ "' and model='om.pssd.dataset' and xpath(daris:mytardis-dataset/uri)='"
								+ sourceDatasetUri + "'</where><action>count</action></args>",
						null, null)
				.longValue("value", 0);
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
		String filename = "mytardis-dataset-" + datasetId + "." + AAR_FILE_EXT;
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
		String arcMimeType = AAR_MIME_TYPE;
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

	/**
	 * Create an archive input stream from the given file (by checking its file
	 * extension).
	 * 
	 * @param arcFile
	 *            the source archive file.
	 * @return
	 * @throws Throwable
	 */
	private static ArchiveInputStream getArchiveInputStream(File arcFile) throws Throwable {
		String filename = arcFile.getName();
		if (filename != null) {
			if (filename.toLowerCase().endsWith(".zip")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP,
						new BufferedInputStream(new FileInputStream(arcFile)));
			} else if (filename.toLowerCase().endsWith(".jar")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR,
						new BufferedInputStream(new FileInputStream(arcFile)));
			} else if (filename.toLowerCase().endsWith(".tar")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR,
						new BufferedInputStream(new FileInputStream(arcFile)));
			} else if (filename.toLowerCase().endsWith(".tar.gz") || filename.toLowerCase().endsWith(".tgz")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR,
						new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(arcFile))));
			} else if (filename.toLowerCase().endsWith(".tar.bz2") || filename.toLowerCase().endsWith(".tbz")
					|| filename.toLowerCase().endsWith(".tbz2") || filename.toLowerCase().endsWith(".tb2")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR,
						new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(arcFile))));
			} else if (filename.toLowerCase().endsWith(".7z")) {
				return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.SEVEN_Z,
						new BufferedInputStream(new FileInputStream(arcFile)));
			}
		}
		throw new IOException("Unknonwn archive format: " + filename);
	}

	/**
	 * Generate mytardis dataset uri
	 * 
	 * @param mytardisUri
	 * @param expId
	 * @param datasetId
	 * @return
	 */
	private static String getMyTardisDatasetUri(String mytardisUri, String datasetId) {
		String baseUri = mytardisUri;
		while (baseUri.endsWith("/")) {
			baseUri = baseUri.substring(0, baseUri.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(baseUri);
		sb.append("/dataset/").append(datasetId);
		return sb.toString();
	}

	private static String getMyTardisExperimentUri(String mytardisUri, String expId) {
		String baseUri = mytardisUri;
		while (baseUri.endsWith("/")) {
			baseUri = baseUri.substring(0, baseUri.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(baseUri);
		sb.append("/experiment/view/").append(expId).append("/");
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
