##
## Remove the predeccessor (old release): nig-analyzer
##
if { [xvalue exists [package.exists :package nig-analyzer]] == "true" } {
    package.uninstall :package nig-analyzer
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/analyzer-plugin.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/analyzer-plugin.jar
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/nig-commons.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/nig-commons.jar
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/dcmtools.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/dcmtools.jar
}

