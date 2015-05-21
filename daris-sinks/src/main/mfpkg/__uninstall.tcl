# ============================================================================
# Uninstall Plugins
# ============================================================================
set plugin_label           [string toupper PACKAGE_$package]
set plugin_namespace       /mflux/plugins
set plugin_zip             sinks-plugin.zip
set plugin_jar             sinks-plugin.jar
set module_class           nig.mf.plugin.sink.SinkPluginModule

if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "true" } {
    plugin.module.remove :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

if { [xvalue exists [asset.exists :id name=${plugin_namespace}/${plugin_jar}]] == "true" } {
    asset.destroy :id name=${plugin_namespace}/${plugin_jar}
}

system.service.reload

srefresh