set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins/daris-core-services
set plugin_zip        daris-core-services-plugin.zip
set plugin_jar        daris-core-services-plugin.jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
set plugin_libs       { lib/daris-commons-1.0.4.jar lib/daris-dcmtools-1.0.1.jar lib/ij-1.51h.jar }

# clean up old version of the module
if { [xvalue exists [plugin.module.exists :path /mflux/plugins/daris-core-services-plugin.jar :class nig.mf.plugin.pssd.PSSDPluginModule]] == "true" } {
    plugin.module.remove :path /mflux/plugins/daris-core-services-plugin.jar :class nig.mf.plugin.pssd.PSSDPluginModule
    asset.hard.destroy :id "path=/mflux/plugins/daris-core-services-plugin.jar"
}

# remove the plugin module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "true" } {
		plugin.module.remove :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

# destroy the plugin asset
if { [xvalue exists [asset.exists :id path=${plugin_namespace}/${plugin_jar}]] == "true" } {
    asset.hard.destroy :id path=${plugin_namespace}/${plugin_jar}
}

# destroy the plugin libraries
foreach lib ${plugin_libs} {
    asset.hard.destroy :id path=${plugin_namespace}/${lib} 
}

system.service.reload

srefresh

