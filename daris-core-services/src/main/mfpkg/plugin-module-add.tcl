set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        daris-core-services-plugin.zip
set plugin_jar        daris-core-services-plugin.jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
set plugin_libs       { libs/daris-commons.jar libs/daris-dcmtools.jar }

# import the plugin jar from the zip file to Mediaflux system. It will be an asset
# in the specified namespace with plugin jar file as content.
asset.import :url archive:${plugin_zip} \
    :namespace -create yes ${plugin_namespace} \
	:label -create yes ${plugin_label} :label PUBLISHED \
    :update true

# add plugin module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "false" } {
    set svc_args " :path ${plugin_namespace}/${plugin_jar} :class ${module_class}"
    foreach lib ${plugin_libs} {
        set svc_args "${svc_args} :lib $lib"
    }
	plugin.module.add ${svc_args}
}
    
# Now that the plugins have been registered, we need to refresh the known services
# with this session so that we can grant permissions for those plugins.
system.service.reload

# Make the (new) commands available to the enclosing shell.
srefresh
