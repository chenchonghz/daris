#
# A proc to destroy all the triggers for the specified namespace, also the script assets they refer to.
#
proc destroyTriggers { ns } {

    if { [xvalue exists [asset.namespace.exists :namespace ${ns}]] == "true" } {
	    set ids [xvalues namespace/trigger/@id [asset.trigger.on.list :namespace ${ns}]]
	    set scripts [xvalues namespace/trigger/script [asset.trigger.on.describe :namespace ${ns}]]
	
	    foreach id ${ids} {
	        asset.trigger.destroy :namespace ${ns} :tid ${id}
	    }
	    
	    foreach script ${scripts} {
	        if { [ xvalue exists [ asset.exists :id path=${script} ] ] == "true" } {
	            asset.hard.destroy :id path=${script}
	        }
	    }
    }

}

#
# destroy the triggers for /dicom namespace
#

destroyTriggers /dicom

#
# destroy the triggers for /pssd namespace
#
destroyTriggers /pssd
