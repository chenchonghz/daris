##
## Remove the predeccessor (old release): nig-essentials
##
if { [xvalue exists [package.exists :package nig-essentials]] == "true" } {
    package.uninstall :package nig-essentials
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
set old_module_class "nig.mf.plugin.NIGPluginModule"
set old_module_path [xvalue module\[@class='${old_module_class}'\]/@path [plugin.module.list]]
if { ${old_module_path} != "" } {
    plugin.module.remove :path ${old_module_path} :class ${old_module_class}
}


