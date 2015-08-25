set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        daris-essentials-plugin.zip
set plugin_jar        daris-essentials-plugin.jar
set module_class      daris.essentials.EssentialsPluginModule
set plugin_libs       { libs/daris-commons.jar libs/daris-dcmtools.jar }

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

