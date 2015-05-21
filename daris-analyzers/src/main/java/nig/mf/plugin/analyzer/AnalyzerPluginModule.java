package nig.mf.plugin.analyzer;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.content.ContentAnalyzerPluginModule;
import arc.mf.plugin.content.PluginContentAnalyzer;

/**
 * This class is the entry point for the plugin module and will be registered by
 * the installation package.
 * 
 * @author nebk
 * 
 */
public class AnalyzerPluginModule implements ContentAnalyzerPluginModule {

    private List<PluginContentAnalyzer> _analyzers;
    private List<PluginService> _services;

    public AnalyzerPluginModule() {
    }

    public Collection<PluginContentAnalyzer> analyzers() {

        return _analyzers;
    }

    public String description() {

        return "Biomedical imaging format content analyzer via LONI ImageIO Plugins.";
    }

    public void initialize(ConfigurationResolver config) throws Throwable {

        _analyzers = new Vector<PluginContentAnalyzer>();
        _analyzers.add(new BioMedImageAnalyzer());

        _services = new Vector<PluginService>();
        _services.add(new SvcImageGet());
        _services.add(new SvcMetadataGet());

        // Declare image readers
        ImageIOUtil.registerImageReaders();

    }

    public Collection<PluginService> services() {

        return _services;
    }

    public void shutdown(ConfigurationResolver config) throws Throwable {
    }

    public String vendor() {
        return "The University of Melbourne.";
    }

    public String version() {
        return "1.0";
    }

}
