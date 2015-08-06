#============================================================================#
# Very generic Method as an example. No specific Subject meta-data as  that  #
# would be domain specific.                                                  #
#                                                                            #
# Arguments:                                                                 #
#     action: action to take if method pre-exists, action = 0 (do nothing),  #
#             1 (replace), 2 (create new)                                    #
#     fillin: when creating Method,                                          #
#             fillin=0 (don't fill in cid allocator space),                  #
#                    1 (fill in cid allocator space)                         #
#============================================================================#
proc create_domain_method_generic { cid_root doc_ns { action 0 } { fillin 0 } } {
    set name "DaRIS Generic Method"
    set description "DaRIS Method with data acquisition of an unspecified Study type."

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

    # The subject meta-data just lets you populate mf-note
    # Generally this meta-data would be defined in your own package
    set args "${args} \
        :namespace pssd/methods  \
        :name ${name} \
        :cid-root-name ${cid_root} \
        :description ${description} \
        :subject < \
            :project < \
                :public < \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-subject > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-identity > \
                    :metadata < :definition -requirement optional mf-note > > > > \
        :step < \
            :name \"Generic acquisition\" \
            :description \"Generic acquisition of subject\" \
            :study < :type Unspecified > >"

    if { ${id} != "" && ${action} == 1 } {
        # replace (update) the existing method
        om.pssd.method.for.subject.update $args
    } else {
        # create new method
        set id [xvalue id [om.pssd.method.for.subject.update $args]]
    }
    return ${id}
}
