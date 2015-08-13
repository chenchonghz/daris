##
## Remove the predeccessor (old release): nig-transcode
##
if { [xvalue exists [package.exists :package nig-transcode]] == "true" } {
    package.uninstall :package nig-transcode
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/nig-commons.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/nig-commons.jar
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/dcmtools.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/dcmtools.jar
}

#
# Remove the old plugin module as the module class name has been changed.
#
set old_module_class "nig.mf.plugin.transcode.TranscodePluginModule"
set old_module_path "/mflux/plugins/daris-transcoders-plugin.jar"
if { [xvalue exists [plugin.module.exists :path ${old_module_path} :class ${old_module_class}]] == "true" } {
    plugin.module.remove :path ${old_module_path} :class ${old_module_class}
}

