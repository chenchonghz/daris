# ===========================================================================
# Simple method for Human MRI acquisitions appropriate to standard RCH usage
# With no re-usable RSubject (now deprecated)
# =========================================================================== 
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
# If creating Method, fillin - 0 (don't fill in cid allocator space), 1 (fill in cid allocator space)
#
proc createMethod_human_unspecified { { action 0 } { fillin 0 } } {
	set name "DaRIS Generic Human Method"
	set description "DaRIS Method for Human subjects with data acquisition of an unspecified Study type."
	#
	set name1 "Generic acquisition for human subject"
	set desc1 "Generic acquisition for human subject"
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
		set args "${margs} \
	    :namespace pssd/methods  \
	    :name ${name} \
	    :cid-root-name pssd.method.dmn \
	    :description ${description} \
	    :subject < \
	      :project < \
   		     :public < \
	           :metadata < :definition -requirement optional daris-dmn:pssd-identity > \
	           :metadata < :definition -requirement optional daris-dmn:pssd-subject :value < :type constant(animal) > > \
	           :metadata < :definition -requirement optional daris-dmn:pssd-animal-subject :value < :species constant(human) > > \
	           :metadata < :definition -requirement optional daris-dmn:pssd-human-subject > \
	           :metadata < :definition -requirement optional mf-note > \
	          > \
	          :private < \
	             :metadata < :definition -requirement optional daris-dmn:pssd-human-identity > \
	           > \
	      > \
	    > \
	    :step < \
	         :name ${name1} :description ${desc1} :study < :type ${type1} :dicom < :modality MR > > \
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
