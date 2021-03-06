########################################################################################################################################
# pssd script
# MOves nig-daris:pssd-identity from ns=private to ns=public on all Subject objects
#
#           script.execute :in file:/path/move_identity.tcl 
########################################################################################################################################

proc retrofit_all { } {

# Find all Subject assets with the target meta-data
    set assetIds [xvalues id [asset.query :where nig-daris:pssd-identity has value and model='om.pssd.subject' :size infinity]]

# Loop over assets
    foreach assetId $assetIds {

# Get asset
	set assetDetail [asset.get :id $assetId]

# Fetch detail
	set ns [xvalue asset/meta/nig-daris:pssd-identity/@ns $assetDetail]
	set id [xvalue asset/meta/nig-daris:pssd-identity/id $assetDetail]
	set type [xvalue asset/meta/nig-daris:pssd-identity/id/@type $assetDetail]

# Move private to public

        if { $ns == "pssd.private" } {
            asset.set :id $assetId :meta -action add < :nig-daris:pssd-identity -ns pssd.public < :id -type $type $id > >
            asset.set :id $assetId :meta -action remove < :nig-daris:pssd-identity -ns pssd.private >
        }
    }
}


##
## Main
##
retrofit_all 
