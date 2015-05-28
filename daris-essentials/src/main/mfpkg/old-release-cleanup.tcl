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

