proc install_fcp { namespace name description } {

# Check store mounted ok. The query will generate an error if not
    set s [xvalue namespace/store [asset.namespace.describe :namespace $namespace]]
    set e [xvalue store/mount/error [asset.store.describe :name $s]] 
    if { $e != "" } {
# There is something wrong with the store (may not be mounted)
     return
    } 


	if { [xvalue exists [asset.exists :id path=$namespace/$name]] == "true" } {
		asset.set :id path=$namespace/$name \
		          :url archive:$name \
		          :type application/arc-fcp \
		          :description $description
	} else {
		asset.create :url archive:$name \
		          :type application/arc-fcp \
		          :namespace -create true $namespace \
		          :name $name \
		          :description $description
    }
}

proc uninstall_fcp { namespace name } {
	if { [xvalue exists [asset.exists :id path=$namespace/$name]] == "true" } {
		asset.hard.destroy :id path=$namespace/$name
	}
}