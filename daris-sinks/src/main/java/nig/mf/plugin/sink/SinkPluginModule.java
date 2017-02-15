package nig.mf.plugin.sink;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import nig.mf.plugin.sink.services.SvcSshHostKeyScan;
import nig.mf.plugin.sink.services.SvcUserSelfSinkSettingsGet;
import nig.mf.plugin.sink.services.SvcUserSelfSinkSettingsSet;
import nig.mf.plugin.sink.services.SvcSinkDescribe;
import nig.mf.plugin.sink.services.SvcSshPublicKeyPush;
import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.DataSinkRegistry;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;

public class SinkPluginModule implements PluginModule {

    private List<PluginService> _services;

    @Override
    public String description() {
        return "Sink plugins.";
    }

    @Override
    public void initialize(ConfigurationResolver arg0) throws Throwable {
        if (_services == null) {
            _services = new Vector<PluginService>();
        }
        _services.add(new SvcSshHostKeyScan());
        _services.add(new SvcUserSelfSinkSettingsGet());
        _services.add(new SvcUserSelfSinkSettingsSet());
        _services.add(new SvcSinkDescribe());
        _services.add(new SvcSshPublicKeyPush());

        try {
            DataSinkRegistry.removeAll(this);
        } catch (Throwable e) {

        }

        try {
            
            DataSinkRegistry.add(this, new ScpSink());
            DataSinkRegistry.add(this, new WebDAVSink());
            DataSinkRegistry.add(this, new OwnCloudSink());
        } catch (Throwable e) {
            /*
             * Have to restart MF server to update the sinks :(
             */
        }
    }

    @Override
    public Collection<PluginService> services() {
        return _services;
    }

    @Override
    public void shutdown(ConfigurationResolver arg0) throws Throwable {
        DataSinkRegistry.removeAll(this);
    }

    @Override
    public String vendor() {
        return "Neuroimaging group.";
    }

    @Override
    public String version() {
        return "1.0.2";
    }

}
