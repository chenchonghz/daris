
# ===========================================================================
# Very generic Method as an example. No specific Subject meta-data as 
# that would be domain specific.
# =========================================================================== 
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
# If creating Method, fillin - 0 (don't fill in cid allocator space), 1 (fill in cid allocator space)
#
proc createMethod { { action 0 } { fillin 0 } } {
	set name "DaRIS Generic Method"
	set description "DaRIS Method with data acquisition of an unspecified Study type."
	#
	set name1 "Generic acquisition" 
	set desc1 "Generic acquisition of subject" 
	set type1 "Unspecified"
	#
	set margs ""
	# See if Method pre-exists
	set id [getMethodId $name]
	    
	# Set arguments based on desired action	
	set margs [setMethodUpdateArgs $id $action $fillin]
	if { $margs == "quit" } {
		return
	}
	#
	# The Subject meta-data just lets you populate mf-note
	# Generally this meta-data would be defined in your own package
	set args "${margs} \
	    :namespace pssd/methods  \
	    :name ${name} \
	    :cid-root-name pssd.method.dmn \
	    :description ${description} \
	    :subject < \
	    	:project < \
   		       :public < \
   		         :metadata < :definition -requirement optional daris-dmn:pssd-subject > \
   		         :metadata < :definition -requirement optional daris-dmn:pssd-identity > \
			     :metadata < :definition -requirement optional mf-note > \
		        > \
	         > \
   	     > \
	    :step < \
		    :name ${name1} :description ${desc1} :study < :type ${type1} > \
	    >"
	set id2 [xvalue id [om.pssd.method.for.subject.update $args]]
	if { $id2 == "" } {
	   # An existng Method was updated
	   return $id
	} else {
	   # A new Method was created
	   return $id2
	}
}
