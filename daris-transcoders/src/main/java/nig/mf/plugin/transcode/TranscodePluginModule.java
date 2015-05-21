package nig.mf.plugin.transcode;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderPluginModule;

public class TranscodePluginModule implements PluginModule, TranscoderPluginModule {

	/**
	 * The collection of services can be created and cached.
	 */
	private static final Collection<PluginService> _services = createServices();

	/**
	 * The version of this module.
	 */
	public String version() {
		return "1.1";
	}

	/**
	 * A description of this module.
	 */
	public String description() {
		return "NIG image transcode plugin module for Mediaflux";
	}

	/**
	 * The company or person supplying this module.
	 */
	public String vendor() {
		return "Neuroimaging and Neuroinformatics Group, Centre for Neuroscience Research, the University of Melbourne.";
	}

	/**
	 * Returns the services available in this module. Returns a collection of arc.mf.plugin.PluginService objects.
	 */
	public Collection<PluginService> services() {
		return _services;
	}

	/**
	 * Called once to initialise the set of available services within this module.
	 */
	private static Collection<PluginService> createServices() {
		Vector<PluginService> svs = new Vector<PluginService>();
		svs.add(new SvcTranscode());
		return svs;
	}

	/**
	 * Compatibility check.
	 */
	public boolean isCompatible(ConfigurationResolver config) {
		// Transcoders are not compatible --
		return false;
	}

	/**
	 * Initialization on load.
	 */
	public void initialize(ConfigurationResolver config) throws Throwable {

		// Set max memory per debabeler
		Transcode.JavaXmxOption = config.configurationValue("JavaXmxOption");


		if (Transcode.JavaXmxOption == null) {
			// defaults to 512MB
			config.setConfigurationValue("JavaXmxOption", "-Xmx512m");
			Transcode.JavaXmxOption = "-Xmx512m";
		} 

		// Extract memory as Int and set as max global memory to be used in
		// transcoding. So each transcode can use at most JavaXmxOption
		// AND this is also the max shared over all transcodings
		String s0 = Transcode.JavaXmxOption;
		int n = s0.length();
		String s1 = s0.substring(4, n-1);
		Integer maxMem = Integer.parseInt(s1);
		Transcode.ResourceManager.setMax(maxMem);
		
		// Set DICOM to NIFTI default transcoder
		String  t = config.configurationValue("DICOMTranscoder");
		if (t==null) {
			t = "debabeler";
		} else {
			if (!t.equals("debabeler") && !t.equals("mrtrix")) {
				throw new Exception ("Invalid dicom to nifti transcoder value. Must be one of 'debabeler,mrtrix'.");
			}
		}
		Transcode.DICOMtoNIFTITranscoder = config.configurationValue("DICOMTranscoder");
	}

	/**
	 * Shutdown.
	 */
	public void shutdown(ConfigurationResolver config) throws Throwable {

	}

	public Collection<Transcoder> transcoders() {
		return Transcode.transcoders();
	}
}