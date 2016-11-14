set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins/daris-core-services
set plugin_zip        daris-core-services-plugin.zip
set plugin_jar        daris-core-services-plugin.jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
set plugin_libs       { lib/daris-commons-1.0.4.jar lib/daris-dcmtools-1.0.1.jar }

# import the plugin jar from the zip file to Mediaflux system. It will be an asset
# in the specified namespace with plugin jar file as content.
asset.import :url archive:${plugin_zip} \
    :namespace -create yes ${plugin_namespace} \
	:label -create yes ${plugin_label} :label PUBLISHED \
    :update true

# clean up old version of the module
if { [xvalue exists [plugin.module.exists :path /mflux/plugins/daris-core-services-plugin.jar :class nig.mf.plugin.pssd.PSSDPluginModule]] == "true" } {
    plugin.module.remove :path /mflux/plugins/daris-core-services-plugin.jar :class nig.mf.plugin.pssd.PSSDPluginModule
    asset.hard.destroy :id "path=/mflux/plugins/daris-core-services-plugin.jar"
}

# remove module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "true" } {
    plugin.module.remove :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

# add module
set command "plugin.module.add :path ${plugin_namespace}/${plugin_jar} :class ${module_class}"
foreach lib ${plugin_libs} {
    set command "${command} :lib $lib"
}
eval ${command}
    
# Now that the plugins have been registered, we need to refresh the known services
# with this session so that we can grant permissions for those plugins.
system.service.reload

# Make the (new) commands available to the enclosing shell.
srefresh
