package daris.transcode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderPluginModule;
import arc.xml.XmlDocMaker;
import daris.transcode.debabeler.Debabeler;
import daris.transcode.services.SvcTranscode;
import daris.transcode.services.SvcTranscodeActivate;
import daris.transcode.services.SvcTranscodeList;
import daris.transcode.services.SvcTranscodeProviderDescribe;

public class DarisTranscodePluginModule implements PluginModule,
        TranscoderPluginModule {

    public static final String CFG_VALUE_ANY = "any";

    public static enum ConfigName {
        // @formatter:off
        DEBABELER_JAVA_XMX("debabelerJavaXmx",null,null),
        DICOM_TO_NIFTI("dicomNifti", nig.mf.MimeTypes.DICOM_SERIES, nig.mf.MimeTypes.NIFTI_SERIES),
        DICOM_TO_ANALYZE_NL("dicomAnalyzeNL", nig.mf.MimeTypes.DICOM_SERIES, nig.mf.MimeTypes.ANALYZE_SERIES_NL), 
        DICOM_TO_ANALYZE_RL("dicomAnalyzeRL",nig.mf.MimeTypes.DICOM_SERIES, nig.mf.MimeTypes.ANALYZE_SERIES_RL), 
        DICOM_TO_MINC("dicomMinc", nig.mf.MimeTypes.DICOM_SERIES, nig.mf.MimeTypes.MINC_SERIES),
        DICOM_TO_RDA("dicomRda", nig.mf.MimeTypes.DICOM_SERIES, nig.mf.MimeTypes.SIEMENS_RDA), 
        BRUKER_TO_ANALYZE_NL("brukerAnalyzeNL", nig.mf.MimeTypes.BRUKER_SERIES, nig.mf.MimeTypes.ANALYZE_SERIES_NL), 
        BRUKER_TO_ANALYZE_RL("brukerAnalyzeRL", nig.mf.MimeTypes.BRUKER_SERIES, nig.mf.MimeTypes.ANALYZE_SERIES_RL), 
        BRUKER_TO_MINC("brukerMinc",nig.mf.MimeTypes.BRUKER_SERIES, nig.mf.MimeTypes.MINC_SERIES);
        // @formatter:on
        private String _configName;
        private String _fromType;
        private String _toType;

        ConfigName(String configName, String fromType, String toType) {
            _configName = configName;
            _fromType = fromType;
            _toType = toType;
        }

        @Override
        public final String toString() {
            return _configName;
        }

        public String configName() {
            return _configName;
        }

        public String fromType() {
            return _fromType;
        }

        public String toType() {
            return _toType;
        }

        public boolean isTranscoderConfig() {
            return _fromType != null && _toType != null;
        }

        public static ConfigName configNameFor(String fromType, String toType) {
            if (fromType != null && toType != null) {
                ConfigName[] vs = values();
                for (ConfigName cn : vs) {
                    if (fromType.equals(cn.fromType())
                            && toType.equals(cn.toType())) {
                        return cn;
                    }
                }
            }
            return null;
        }

    }

    /**
     * The collection of services can be created and cached.
     */
    private Collection<PluginService> _services;

    public DarisTranscodePluginModule() {
        _services = new Vector<PluginService>();
        _services.add(new SvcTranscode());
        _services.add(new SvcTranscodeActivate());
        _services.add(new SvcTranscodeList());
        _services.add(new SvcTranscodeProviderDescribe());

        // initialize the registry (add all providers).
        DarisTranscodeRegistry.initialize();
    }

    /**
     * The version of this module.
     */
    public String version() {
        return "2.0.2";
    }

    /**
     * A description of this module.
     */
    public String description() {
        return "DaRIS transcode plugin module.";
    }

    /**
     * The company or person supplying this module.
     */
    public String vendor() {
        return "Neuroimaging and Neuroinformatics Group, Centre for Neuroscience Research, The University of Melbourne.";
    }

    /**
     * Returns the services available in this module. Returns a collection of
     * arc.mf.plugin.PluginService objects.
     */
    public Collection<PluginService> services() {
        return _services;
    }

    /**
     * Compatibility check.
     */
    // public boolean isCompatible(ConfigurationResolver config) {
    // // Transcoders are not compatible --
    // return false;
    // }

    /**
     * Initialization on load.
     */
    public void initialize(ConfigurationResolver config) throws Throwable {

        Map<ConfigName, String> cfgValues = new HashMap<ConfigName, String>();
        if (config != null) {
            ConfigName[] cns = ConfigName.values();
            for (ConfigName cn : cns) {
                String cv = config.configurationValue(cn.configName());
                if (cv != null) {
                    cfgValues.put(cn, cv);
                } else {
                    if (cn.isTranscoderConfig()) {
                        DarisTranscodeImpl impl = DarisTranscodeRegistry
                                .getActiveTranscoderImpl(cn.fromType(),
                                        cn.toType());
                        if (impl != null) {
                            cfgValues.put(cn, impl.provider().name());
                            config.setConfigurationValue(cn.configName(), impl
                                    .provider().name());
                        } else {
                            cfgValues.put(cn, CFG_VALUE_ANY);
                            config.setConfigurationValue(cn.configName(),
                                    CFG_VALUE_ANY);
                        }
                    }
                }
            }
        }
        if (cfgValues.containsKey(ConfigName.DEBABELER_JAVA_XMX)) {
            Debabeler.setJavaXmx(cfgValues.get(ConfigName.DEBABELER_JAVA_XMX));
        }
        DarisTranscodeRegistry.initialize();
        for (ConfigName cn : cfgValues.keySet()) {
            if (!cn.isTranscoderConfig()) {
                continue;
            }
            String provider = cfgValues.get(cn);
            if (provider != null && !CFG_VALUE_ANY.equals(provider)) {
                DarisTranscodeRegistry.activate(provider, cn.fromType(),
                        cn.toType());
            }
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown(ConfigurationResolver config) throws Throwable {

    }

    public Collection<Transcoder> transcoders() {
        return DarisTranscodeRegistry.transcoders();
    }

    public static void setTranscoderConfigValue(ServiceExecutor executor,
            String fromType, String toType, String provider) throws Throwable {
        ConfigName configName = ConfigName.configNameFor(fromType, toType);
        if (configName != null) {
            setConfigValue(executor, configName, provider);
        }
    }

    public static void setConfigValue(ServiceExecutor executor,
            ConfigName configName, String value) throws Throwable {
        String className = DarisTranscodePluginModule.class.getName();
        String path = executor.execute("plugin.module.list").value(
                "module[@class='" + className + "']/@path");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("class", className);
        dm.add("path", path);
        dm.add("config", new String[] { "name", configName.configName() },
                value);
        executor.execute("plugin.module.config.set", dm.root());
    }
}