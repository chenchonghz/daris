#============================================================================#
# Simple method for Human MRI acquisitions appropriate to standard RCH usage #
# with no re-usable RSubject (now deprecated)                                #
#                                                                            #
# Arguments:                                                                 #
#     action: action to take if method pre-exists, action = 0 (do nothing),  #
#             1 (replace), 2 (create new)                                    #
#     fillin: when creating Method,                                          #
#             fillin=0 (don't fill in cid allocator space),                  #
#                    1 (fill in cid allocator space)                         #
#============================================================================#
proc create_domain_method_human_unspecified { cid_root doc_ns { action 0 } { fillin 0 } } {
    set name "DaRIS Generic Human Method"
    set description "DaRIS Method for Human subjects with data acquisition of an unspecified Study type."
    
    # look for existing method with the same name
    set id [xvalue id [om.pssd.method.find :name ${name}]]

    # do nothing if it pre-exists
    if { ${id} != "" && ${action}==0 } {
        return
    }
 
    # append the args based the argument values
    set args ""
    if { $id != "" } {
        if { ${action} == 1 } {
            set args "${args} :replace 1 :id ${id}"
        } else {
            return ""
        }
    }
    if { ${fillin} == 1 } {
       set args "${args} :fillin true"
    } else {
       set args "${args} :fillin false"
    }
        
    set args "${args} \
        :namespace \"pssd/methods\"  \
        :name ${name} \
        :cid-root-name ${cid_root} \
        :description ${description} \
        :subject < \
            :project < \
                :public < \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-identity > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-subject :value < :type constant(animal) > > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-animal-subject :value < :species constant(human) > > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-human-subject > \
                    :metadata < :definition -requirement optional mf-note > > \
                :private < \
                    :metadata < :definition -requirement optional daris-dmn:pssd-human-identity > > > > \
        :step < \
            :name \"Generic acquisition for human subject\" \
            :description \"Generic acquisition for human subject\" \
            :study < :type Unspecified :dicom < :modality MR > > >"

    if { ${id} != "" && ${action} == 1 } {
        # replace (update) the existing method
        om.pssd.method.for.subject.update $args
    } else {
        # create new method
        set id [xvalue id [om.pssd.method.for.subject.update $args]]
    }
    return ${id}
}
