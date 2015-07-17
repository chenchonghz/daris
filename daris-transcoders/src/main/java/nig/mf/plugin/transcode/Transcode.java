package nig.mf.plugin.transcode;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import nig.compress.ArchiveUtil;
import nig.compress.TarUtil;
import nig.compress.ZipUtil;
import nig.dicom.siemens.CSAFileUtils;
import nig.mf.MimeTypes;
import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.Exec;
import arc.mf.plugin.MimeTypeRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderImpl;
import arc.mime.MimeType;

/**
 * Registration of conversions for the Mediaflux transcoding framework.
 */
public class Transcode {
	private static HashMap<String, Mapping> _mappings = new HashMap<String, Mapping>();
	// Java Maximum Heap Size
	// Get over-ridden by package installer if supplied
	public static String JavaXmxOption = "-Xmx2000m";

	// Default DICOM to NIFTI transcoder
	public static String DICOMtoNIFTITranscoder = "debabeler";

	/**
	 * Used to work out when there are enough resources to launch a transcoding
	 * The current algorithm just uses the amount of memory notionally available
	 * based on a static maximum amount configured when the transcoder is
	 * installed and the current expected size of existing transcoder processes.
	 * 
	 * 
	 * @author nebk
	 */
	public static class ResourceManager {

		private static Integer maxMem_ = 3000; // Maximum memory available for
		// transcodings
		private static Integer usedMem_ = 0;

		/**
		 * Set maximum available Memory in MB
		 * 
		 * @param maxMem
		 */
		public static void setMax(Integer maxMem) {
			maxMem_ = maxMem;
		}

		public static Integer getMaxMem() {
			return maxMem_;
		};

		/**
		 * Start the resource management process. Wait until there are
		 * sufficient resources.
		 * 
		 * @param size
		 * @throws Throwable
		 */
		public static synchronized void start(Integer size) throws Throwable {
			// System.out.println("Enter RS.start with max/used/new_size= " +
			// maxMem_ + "/" + usedMem_ + "/" + size);
			while (usedMem_ + size > maxMem_) {
				// System.out.println("   RS.start.Waiting");
				ResourceManager.class.wait();
			}
			usedMem_ += size;
			// System.out.println("Exiting RS.start with usedMem = " +
			// usedMem_);
		}

		/**
		 * Decrement the consumed resource and notify others of this.
		 * 
		 * @param size
		 * @throws Throwable
		 */
		public static synchronized void stop(Integer size) throws Throwable {
			// System.out.println("Enter RS.Stop with size " + size);
			usedMem_ -= size;
			ResourceManager.class.notifyAll();
			// System.out.println("Exiting RS.Stop with usedMem = " + usedMem_);
		}

	}

	public static class Mapping {

		public final String from;

		public final String fromDescription;

		public final String to;

		public final String toDescription;

		public final String provider;

		public final String[] arguments;

		public Mapping(String from, String fromDescription, String to,
				String toDescription, String program, String[] arguments) {

			this.from = from;
			this.fromDescription = fromDescription == null ? from
					: fromDescription;
			this.to = to;
			this.toDescription = toDescription == null ? to : toDescription;
			this.provider = program;
			this.arguments = arguments;
		}

	}

	// Transcode Proviers
	public static class Providers {

		private Providers() {

		}

		public static final String COMBINED_TRANSCODER = "Debabeler/mrtrix selectable transcoder";
		public static final String LONI_DEBABELER = "LONI Debabeler Transcoder";
		public static final String PVCONV = "pvconv Transcoder";
		public static final String NIG_TRANSCODER = "NIG Transcoder";
		public static final String MINC_TRANSCODER = "MINC Binary Transcoder";
		public static final String MRTRIX_TRANSCODER = "mrtrix/mrconvert Transcoder";
	}

	private static void addMapping(String from, String fromDescription,
			String to, String toDescription, String provider, String[] arguments) {

		Mapping m = new Mapping(from, fromDescription, to, toDescription,
				provider, arguments);
		_mappings.put(from + "-" + to, m);
	}

	public static Mapping getMapping(String fromMime, String toMime) {

		String mappingString = fromMime + "-" + toMime;
		return _mappings.get(mappingString);

	}

	public static Mapping getMapping(String mappingString) {

		return _mappings.get(mappingString);

	}

	public static String[] getMappingStrings() {

		Set<String> keys = _mappings.keySet();
		String[] mappingStrings = new String[keys.size()];
		keys.toArray(mappingStrings);
		return mappingStrings;

	}

	public static String[] getMappingToMimeTypes() {

		Collection<Transcode.Mapping> values = _mappings.values();
		String[] toMimeTypes = new String[values.size()];
		int i = 0;
		for (Transcode.Mapping m : values) {
			toMimeTypes[i++] = m.to;
		}
		return toMimeTypes;

	}

	static {

		// DICOM to ...
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.ANALYZE_SERIES_NL, "Analyze(Neurological)",
				Providers.LONI_DEBABELER, new String[] { "-target", "analyze",
						"-mapping", "DicomToAnalyze_NL_Wilson_05Jan2007.xml" });
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.ANALYZE_SERIES_RL, "Analyze(Radiological)",
				Providers.LONI_DEBABELER, new String[] { "-target", "analyze",
						"-mapping", "DicomToAnalyze_RL_Wilson_05Jan2007.xml" });

		/*
		 * addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
		 * MimeTypes.NIFTI_SERIES, "NIFTI series", Providers.LONI_DEBABELER, new
		 * String[] { "-target", "nifti", // "-mapping",
		 * "DicomToNifti_Wilson_23Feb2007.xml" }); // "-mapping",
		 * "DicomToNifti_Wilson_05Jul2012.xml" }); "-mapping",
		 * "DicomToNifti_Wilson_14Aug2012.xml" });
		 */

		// addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
		// MimeTypes.NIFTI_SERIES, "NIFTI series",
		// Providers.MRTRIX_TRANSCODER, null);

		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.NIFTI_SERIES, "NIFTI series",
				Providers.COMBINED_TRANSCODER, new String[] { "-target",
						"nifti", "-mapping",
						"DicomToNifti_Wilson_14Aug2012.xml" });

		// debabeler
		/*
		 * addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
		 * MimeTypes.MINC_SERIES, "MINC series with debabeler",
		 * Providers.LONI_DEBABELER, new String[] { "-target", "minc",
		 * "-mapping", "DicomToMinc_26Oct2011.xml" });
		 */

		// MINC binary. Can have only one transcoder per mimetype in/out pair
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.MINC_SERIES, "MINC series with MINC dcm2mnc binary",
				Providers.MINC_TRANSCODER, null);

		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.SIEMENS_RDA, "RDA(Siemens Spectrum)",
				Providers.NIG_TRANSCODER, null);

		// Bruker to...
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.ANALYZE_SERIES_NL, "Analyze(Neurological)",
				Providers.PVCONV, null);
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.ANALYZE_SERIES_RL, "Analyze(Radiological)",
				Providers.PVCONV, null);
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.MINC_SERIES, "Minc", Providers.PVCONV, null);

	}

	public static Collection<Transcoder> transcoders() {

		if (_mappings == null)
			return null;

		Vector<Transcoder> transcoders = new Vector<Transcoder>(
				_mappings.size());
		for (Iterator<String> it = _mappings.keySet().iterator(); it.hasNext();) {
			Mapping mp = _mappings.get(it.next());
			if (mp != null) {
				try {
					MimeTypeRegistry.define(mp.to, mp.toDescription);
				} catch (Throwable t) {
					System.out.println(t.getMessage());
				}
				transcoders.add(new Transcoder(mp.from, mp.to,
						new TranscodeBridge(mp)));
			}
		}
		return transcoders;

	}

	public static String transcode(File in, String fromMime,
			MimeType fromContentType, String toMime, File out) throws Throwable {

		return transcode(in, fromContentType, getMapping(fromMime, toMime), out);

	}

	/**
	 * Primary conversion function. One call here per DataSet.
	 * 
	 * @param in
	 * @param fromContentType
	 * @param mapping
	 * @param out
	 * @return
	 * @throws Throwable
	 */
	public static String transcode(File in, MimeType fromContentType,
			Mapping mapping, File out) throws Throwable {

		// Directory to unpack asset contents
		File tmpDir = PluginService.createTemporaryDirectory();
		try {

			// Unpack the content for this PSSD asset (one asset per call) into
			// the temp directory. If DICOM, there will be one file per 2D
			// slice.

			String name = fromContentType.name();
			if (name.equalsIgnoreCase("application/zip")
					|| name.equalsIgnoreCase(MimeTypes.ZIP)) {
				ZipUtil.unzip2(in, tmpDir, null, true);
			} else if (name.equalsIgnoreCase(MimeTypes.AAR)) {
				ArchiveInput ai = ArchiveRegistry.createInput(in,
						fromContentType);
				try {
					ArchiveExtractor.extract(ai, tmpDir, false, true, true);
				} finally {
					ai.close();
				}
			} else if (name.equalsIgnoreCase(MimeTypes.TAR)) {
				TarUtil.untar(tmpDir, in);
			} else {
				throw new Exception("Content MimeType " + name
						+ " is not handled");
			}

			// Find the top-level files (this is all we want for Bruker) in the
			// unpacked directory
			Collection<File> inputFiles = Arrays.asList(tmpDir.listFiles());

			// Get size of directory in MB
			Integer sizeOf = (Integer) ((int) (org.apache.commons.io.FileUtils
					.sizeOf(tmpDir) / 1000000));
			/*
			 * System.out.println("");
			 * System.out.println("No. of tmp dir files="+inputs.size());
			 * System.out.println("size="+sizeOf + "MB");
			 */
			if (mapping.provider.equals(Providers.LONI_DEBABELER)) {
				doDebabeler(tmpDir, mapping, sizeOf);
			} else if (mapping.provider.equals(Providers.PVCONV)) {
				doPVConv(tmpDir, mapping, inputFiles);
			} else if (mapping.provider.equals(Providers.NIG_TRANSCODER)) {
				doRDA(tmpDir, inputFiles);
			} else if (mapping.provider.equals(Providers.MINC_TRANSCODER)) {
				doMINC(tmpDir, inputFiles, sizeOf);
			} else if (mapping.provider.equals(Providers.COMBINED_TRANSCODER)) {

				// Which transcoder are we using
				String t = DICOMtoNIFTITranscoder;
				if (t.equals("debabeler")) {
					doDebabeler(tmpDir, mapping, sizeOf);
				} else if (t.equals("mrtrix")) {
					doMRTrix(tmpDir, mapping, sizeOf);
				} else {
					throw new Exception(
							t
									+ " is not a valid DICOM to NIFTI transcoder user setting.  Must be one of 'debabeler,mrtrix'");
				}
			}

			// Because we transform in-situ, we need to remove the input files
			// before packaging the output
			for (File inputFile : inputFiles) {
				if (inputFile.isDirectory()) {
					FileUtils.deleteDirectory(inputFile);
					// File.delete seems to fail to work reliably
				} else {
					FileUtils.deleteQuietly(inputFile);
				}
			}

			// Some transcoders (pvconv.pl) don't throw an exception, but simply
			// fail to produce output
			// If this happens as part of a multiple DataSet SHoppingCart
			// process, an empty
			// archive will cause failure, so stick something in there
			File[] finalFiles = tmpDir.listFiles();
			if (finalFiles.length == 0) {
				createErrorFile(tmpDir, "No output was created");
			}

			// Use aar as the output container as it has no limit in size
			int clevel = 6;
			ArchiveUtil.compressDirectory(tmpDir, out, MimeTypes.AAR, clevel,
					null);
		} finally {
			try {
				FileUtils.deleteDirectory(tmpDir);
			} catch (Throwable e) {
				if (tmpDir != null) {
					FileUtils.forceDeleteOnExit(tmpDir);
				}
			}
		}
		return MimeTypes.AAR;
	}

	/**
	 * Converts between a plugin transcoder and a transformer.
	 */
	private static class TranscodeBridge implements TranscoderImpl {

		private Mapping _mapping;

		/**
		 * Constructor.
		 */
		public TranscodeBridge(Mapping mapping) {

			_mapping = mapping;

		}

		/**
		 * Description of the transcoding.
		 */
		public String description() {

			if (_mapping != null) {
				return _mapping.from + " to " + _mapping.to + " transcoder.";
			}
			return null;

		}

		/**
		 * Returns the actual/encapsulation output MIME type of this transcoder
		 * (as distinct from the logical MIME type) for the given "fromMime"
		 * type to the "toMime" type. The output MimeType maybe different to the
		 * "toMime" type. This method is used by the caller, prior to doing the
		 * transcoding, to determine the output MimeType to prepare for.
		 */
		@Override
		public String outputType(MimeType fromMimeType, MimeType toMimeType) {

			// There is no logical connection between the logical mime types and
			// the content mime type. We choose to use AAR as the output
			// container.
			return MimeTypes.AAR;
		}

		/**
		 * Apply the transformation. In our usage in is the input zip file
		 * holding many files for one DataSet (e.g. one volume) out is the
		 * output zip file holdin the transformed data
		 */
		@Override
		public String transcode(File in, MimeType fromType,
				MimeType fromContentType, MimeType toType, File out,
				Map<String, String> params) throws Throwable {

			return Transcode.transcode(in, fromContentType, _mapping, out);
		}

	}

	protected static String[] concat(String[] a, String[] b) {

		if (a == null && b == null) {
			return null;
		}
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		String[] c = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);

		return c;

	}

	protected static String[] concat(String e, String[] a) {

		if (e == null) {
			return a;
		}
		return concat(new String[] { e }, a);

	}

	protected static String[] concat(String[] a, String e) {

		if (e == null) {
			return a;
		}
		return concat(a, new String[] { e });

	}

	private static void createErrorFile(File dir, String error)
			throws Throwable {
		String t = dir.getAbsolutePath() + "/" + "error.txt";
		PrintWriter writer = new PrintWriter(t, "UTF-8");
		writer.println(error);
		writer.close();
	}

	private static void doDebabeler(File tmpDir, Mapping mapping, Integer sizeOf)
			throws Throwable {

		final String debabelerJar = "loni-debabeler.jar";
		String[] jars = new String[] { debabelerJar };
		String[] options = { JavaXmxOption, "-Djava.awt.headless=true" };
		String mainClass = "edu.ucla.loni.debabel.events.engine.DebabelerEngine";

		// Suppress messages so we don't end up with giant XML Documents
		// which fail to parse
		String[] mainArgs = concat(
				new String[] { "-input", tmpDir.getAbsolutePath(), "-suppress" },
				mapping.arguments);
		String[] args = concat(mainClass, mainArgs);

		// Assess whether there are sufficient resources to start this
		// debabeler
		// System.out.println("ReseourceManager max memory = " +
		// ResourceManager.getMaxMem());
		ResourceManager.start(sizeOf);
		try {
			// Run Debabeler externally. Runs in its own thread and so
			// does not block
			// the server for large transcodes.
			String t = Exec.execJava(jars, options, args, null);
		} finally {
			// We are finished, decrement the resource consumed and
			// notify
			ResourceManager.stop(sizeOf);
		}

		// Run Debabeler internally.
		// edu.ucla.loni.debabel.events.engine.DebabelerEngine.main(mainArgs);

	}

	private static void doMRTrix(File tmpDir, Mapping mapping, Integer sizeOf)
			throws Throwable {

		// Use the same resource management approach as debabeler

		// DICOM to NIFTI via the external binary mrconvert
		String cmd = "mrconvert";
		String args = "-datatype int16 "; // Output data type
		String outFileName = tmpDir.getAbsolutePath() + "/" + "mriconvert.nii";
		args += tmpDir.getAbsolutePath() + " " + outFileName;

		// Assess whether there are sufficient resources to start this
		// transcoding
		ResourceManager.start(sizeOf);
		try {
			// Will throw exception if binary missing
			Exec.exec(cmd, args);
		} finally {
			// We are finished, decrement the resource comsumed and
			// notify
			ResourceManager.stop(sizeOf);
		}
	}

	private static void doPVConv(File tmpDir, Mapping mapping,
			Collection<File> inputFiles) throws Throwable {
		// These are fairly light weight and don't need any careful
		// resource
		// management

		// Iterate through the input files. For Bruker, should be a
		// single directory holding the Bruker structure
		for (File inputFile : inputFiles) {
			// Convert the bruker series:
			// pvconv.pl <in> -outdir <out>
			// This command must be installed in the mediaflux
			// plugin/bin directory
			String cmd = "pvconv.pl";
			//
			String args = inputFile.getAbsolutePath() + " ";
			if (mapping.to.equals(MimeTypes.ANALYZE_SERIES_RL)) {
				args += "-radio ";
			} else if (mapping.to.equals(MimeTypes.ANALYZE_SERIES_NL)) {
				args += "-noradio ";
			} else if (mapping.to.equals(MimeTypes.MINC_SERIES)) {
				args += "-outtype minc ";
			}
			args += "-outdir " + tmpDir.getAbsolutePath();
			try {
				Exec.exec(cmd, args);
			} catch (Throwable t) {
				createErrorFile(tmpDir, t.getMessage());
			}
		}
	}

	private static void doMINC(File tmpDir, Collection<File> inputFiles,
			Integer sizeOf) throws Throwable {
		// Use the same resource management approach as debabeler
		// DICOM to MINC via the external MINC binary dcm2mnc
		String cmd = "dcm2mnc";
		String args = "-usecoordinates ";
		for (File inputFile : inputFiles) {
			// Convert the DICOM series:
			// dcm2mnc <in-dir> <out-dir>
			//
			args += inputFile.getAbsolutePath() + " ";
		}
		args += tmpDir.getAbsolutePath();

		// Assess whether there are sufficient resources to start this transcode
		ResourceManager.start(sizeOf);
		ResourceManager.start(sizeOf);
		try {
			// Will throw exception if binary missing
			Exec.exec(cmd, args);
		} finally {
			// We are finished, decrement the resource comsumed and
			// notify
			ResourceManager.stop(sizeOf);
		}
	}

	private static void doRDA(File tmpDir, Collection<File> inputFiles)
			throws Throwable {
		// These are fairly light weight and don't need any careful
		// resource management

		// Siemens DICOM -> Siemens RDA
		for (File inputFile : inputFiles) {
			File rdaFile = new File(tmpDir, inputFile.getName().substring(0,
					inputFile.getName().lastIndexOf("."))
					+ ".rda");
			if (CSAFileUtils.isCSADicomFile(inputFile)) {
				CSAFileUtils.convertToSiemensRDA(inputFile, rdaFile);
			}
		}
	}
}