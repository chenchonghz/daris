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
import arc.xml.XmlDoc.Element;
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
		experiment.add(new Interface.Element("id", LongType.POSITIVE_ONE, "MyTardis dataset id.", 1, 1));
		experiment.add(new Interface.Element("description", StringType.DEFAULT, "MyTardis dataset description.", 0, 1));
		experiment.add(new Interface.Element("instrument", StringType.DEFAULT, "Instrument.", 0, 1));
		_defn.add(dataset);

		_defn.add(new Interface.Element("source-mytardis-uri", UrlType.DEFAULT, "The source MyTardis server address."));
		_defn.add(new Interface.Element("dicom-only", BooleanType.DEFAULT,
				"Import only data files in DICOM format. All the data in other formats are ignored."));

		_defn.add(new Interface.Element("project", CiteableIdType.DEFAULT,
				"The citeable id of the DaRIS project to import to.", 1, 1));

		_defn.add(new Interface.Element("async", BooleanType.DEFAULT,
				"Run the service asynchronously. Defaults to true."));
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
		final boolean dicomOnly = args.booleanValue("dicom-only", false);
		final String projectCid = args.value("project");
		final String sourceUri = getMyTardisDatasetUri(mytardisUri, expId, datasetId);
		final PluginService.Input input = inputs.input(0);
		final boolean async = args.booleanValue("async", true);

		if (datasetExists(executor(), projectCid, expId, expTitle, expDescription, datasetId, datasetDescription,
				instrument, sourceUri)) {
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
								datasetDescription, instrument, sourceUri, input, dicomOnly);
					} catch (Throwable e) {
						e.printStackTrace(System.out);
					}
				}
			});
		} else {
			importMyTardisDataset(executor(), projectCid, expId, expTitle, expDescription, datasetId,
					datasetDescription, instrument, sourceUri, input, dicomOnly);
		}

	}

	private static void importMyTardisDataset(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDescription, String datasetId, String datasetDescription, String instrument,
			String sourceUri, PluginService.Input input, boolean dicomOnly) throws Throwable {
		File dir = null;
		if (!dicomOnly) {
			dir = PluginTask.createTemporaryDirectory();
		}
		File dicomDir = PluginTask.createTemporaryDirectory();
		try {
			List<File> dicomFiles = new ArrayList<File>();
			extract(input, dir, dicomDir, dicomFiles);

			String subjectCid = findOrCreateSubject(executor, projectCid, expId, expTitle, expDescription, datasetId,
					datasetDescription, instrument, sourceUri);
			if (dicomOnly) {
				ingestDicomData(executor, subjectCid, dicomDir, dicomFiles);
			} else {
				String studyCid = findOrCreateStudy(executor, subjectCid, expId, expTitle, expDescription, datasetId,
						datasetDescription, instrument, sourceUri);
				createDataset(executor, studyCid, expId, expTitle, expDescription, datasetId, datasetDescription,
						instrument, sourceUri, dir);
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
			List<File> dicomFiles) {
		// TODO Auto-generated method stub

	}

	private static void createDataset(ServiceExecutor executor, String studyCid, String expId, String expTitle,
			String expDescription, String datasetId, String datasetDescription, String instrument, String sourceUri,
			File dir) {
		// TODO Auto-generated method stub

	}

	private static boolean datasetExists(ServiceExecutor executor, String projectCid, String expId, String expTitle,
			String expDesc, String datasetId, String datasetDescription, String instrument, String sourceUri)
					throws Throwable {
		// TODO:
		return false;
	}

	private static String findOrCreateSubject(ServiceExecutor executor, String projectCid, String expId,
			String expTitle, String expDesc, String datasetId, String datasetDescription, String instrument,
			String sourceUri) throws Throwable {
		// TODO:
		return null;
	}

	private static String findOrCreateStudy(ServiceExecutor executor, String subjectCid, String expId, String expTitle,
			String expDesc, String datasetId, String datasetDescription, String instrument, String sourceUri)
					throws Throwable {
		// TODO
		return null;
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

	static void aar(File dir, File output, int compressionLevel) throws Throwable {
		archive(dir, output, "application/arc-archive", compressionLevel);
	}

	static void zip(File dir, File output, int compressionLevel) throws Throwable {
		archive(dir, output, "application/zip", compressionLevel);
	}

	private static void archive(File dir, File output, String archiveMimeType, int compressionLevel) throws Throwable {
		ArchiveOutput ao = ArchiveRegistry.createOutput(output, archiveMimeType, compressionLevel, null);
		try {
			File[] files = dir.listFiles();
			for (File f : files) {
				addToArchive(ao, f, dir);
			}
		} finally {
			ao.close();
		}
	}

	private static void addToArchive(ArchiveOutput ao, File file, File baseDir) throws Throwable {
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			for (File f : fs) {
				addToArchive(ao, f, baseDir);
			}
		} else {
			String name = file.getAbsolutePath();
			String base = baseDir.getAbsolutePath();
			if (name.startsWith(base)) {
				name = name.substring(0, base.length());
			}
			while (name.startsWith("/")) {
				name = name.substring(1);
			}
			ao.add(null, name, file);
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
