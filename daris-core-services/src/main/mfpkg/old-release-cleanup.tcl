##
## Remove the predeccessor (old release): PSSD
##
if { [xvalue exists [package.exists :package PSSD]] == "true" } {
    package.uninstall :package PSSD
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/nig-commons.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/nig-commons.jar
}
if { [xvalue exists [asset.exists :id path=/mflux/plugins/libs/dcmtools.jar]] == "true" } {
    asset.hard.destroy :id path=/mflux/plugins/libs/dcmtools.jar
}

set repository_description_asset_id [xvalue id [asset.query :where name='pssd-repository-description']]
if { ${repository_description_asset_id} != "" } {
    asset.set :id ${repository_description_asset_id} :name "daris repository description"
}
