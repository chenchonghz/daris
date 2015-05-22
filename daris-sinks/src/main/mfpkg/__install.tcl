source __uninstall.tcl

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label           [string toupper PACKAGE_$package]
set plugin_namespace       /mflux/plugins
set plugin_zip             daris-sinks-plugin.zip
set plugin_jar             daris-sinks-plugin.jar
set module_class           nig.mf.plugin.sink.SinkPluginModule

# extract transform-plugin.jar to /mflux/plugins
asset.import :url archive:${plugin_zip} \
        :namespace -create yes ${plugin_namespace} \
        :label -create yes ${plugin_label} :label PUBLISHED \
        :update true

# install the plugin module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "false" } {
	plugin.module.add :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

# reload the services     
system.service.reload

# refresh the enclosing shell
srefresh

# ============================================================================
# Define roles and service permissions
# ============================================================================
source service-perms.tcl
