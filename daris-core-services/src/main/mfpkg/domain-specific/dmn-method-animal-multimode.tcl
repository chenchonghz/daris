#============================================================================#
# Simple method for multi mode animal acquisitions.                          #
#                                                                            #
# Arguments:                                                                 #
#     action: action to take if method pre-exists, action = 0 (do nothing),  #
#             1 (replace), 2 (create new)                                    #
#     fillin: when creating Method,                                          #
#             fillin=0 (don't fill in cid allocator space),                  #
#                    1 (fill in cid allocator space)                         #
#============================================================================#
 
proc create_domain_method_animal_multimode { cid_root doc_ns { action 0 } { fillin 0 } } {
    
    set name "DaRIS Generic Animal Method for Multi-mode Imaging"
    set description "DaRIS Method for animal (including humans) subjects with multi-mode image data acquisitions."

    # look for existing method with the same name
    set id [xvalue id [om.pssd.method.find :name ${name}]]

    # do nothing if it pre-exists
    if { ${id} != "" && ${action} == 0 } {
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
        :namespace pssd/methods  \
        :cid-root-name ${cid_root} \
        :name ${name} \
        :description ${description} \
        :subject < \
            :project < \
                :public < \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-identity > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-subject  :value < :type constant(animal) > > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-animal-subject > \
                    :metadata < :definition -requirement optional mf-note > > > > \
        :step < \
            :name \"Combined PET/CT acquisition\" \
            :description \"PET/CT acquisition of subject\" \
            :study < \
                :type \"Positron Emission Tomography/Computed Tomography\" \
                :dicom < :modality PT :modality CT > > > \
        :step < \
            :name \"MRI acquisition\" \
            :description \"MRI acquisition of subject\" \
            :study < \
                :type \"Magnetic Resonance Imaging\" \
                :dicom < :modality MR > > > \
        :step < \
            :name \"PET acquisition\" \
            :description \"PET acquisition of subject\" \
            :study < \
                :type \"Positron Emission Tomography\" \
                :dicom < :modality PT > > > \
        :step < \
            :name \"Electron Microscopy acquisition\" \
            :description \"EM acquisition of subject\" \
            :study < \
                :type \"Electron Microscopy\" > > \
        :step < \
            :name \"Optical Microscopy acquisition\" \
            :description \"OM acquisition of subject\" \
            :study < \
                :type \"Optical Microscopy\" > > \
        :step < \
            :name \"Computed Tomography acquisition\" \
            :description \"CT acquisition of subject\" \
            :study < \
                :type \"Computed Tomography\" :dicom < :modality CT > > > \
        :step < \
            :name \"Nuclear Medicine acquisition\" \
            :description \"NM acquisition of subject\" \
            :study < \
                :type \"Nuclear Medicine\" \
                :dicom < :modality NM > > >"

    if { ${id} != "" && ${action} == 1 } {
        # replace (update) the existing method
        om.pssd.method.for.subject.update $args
    } else {
        # create new method
        set id [xvalue id [om.pssd.method.for.subject.update $args]]
    }
    return ${id}     
}
