# This script creates addition (to the basic Doc Types defined in doctypes-PSSD.tcl) specific
# Document Types for meta-data to be attached to PSSD objects for the AMBMC project.
# This script also creates the Method for this project.
# 
# ================================
# MRI scan
# ================================

set r [ om.pssd.method.create \
    :namespace pssd/methods \
    :name "AMBMC Live MRI scan" \
    :description "Production of MRI data from mouse" \
    :step < \
        :name "Anaesthetize" \
        :description "Mouse is anaesthetized" \
        :subject -part p < \
           :metadata < \
              :definition -requirement mandatory "nig-daris:pssd-anaesthetic" \
           > \
        > \
    > \
    :step < \
        :name "Live MR Acquisition" \
        :description "MR acquisition" \
        :study < \
           :type MR \
        > \
    > \
    :step < \
        :name "Recovery" \
        :description "Mouse recovers under warming light" \
        :subject -part p < \
           :metadata < \
              :definition -requirement mandatory "nig-daris:pssd-recovery" \
           > \
        > \
    >
    ]
set mr_mthd [xvalue id $r]


###################
# Kill and Perfuse
###################

set r [ om.pssd.method.create \
    :namespace pssd/methods \
    :name "AMBMC Kill & Perfuse" \
    :description "Kill mouse, perfuse brain in skull" \
    :step < \
        :name "Kill" \
        :description "Mouse is killed" \
        :subject -part p < \
           :metadata < \
              :definition -requirement mandatory "nig-daris:pssd-animal-kill" \
           > \
        > \
    > \
    :step < \
      :name "Perfusion" \
      :description "Mouse brain is perfused in situ" \
      :subject -part p < \
        :metadata < \
           :definition -requirement mandatory "nig-daris:pssd-EAE-perfusion"  \
        > \
      > \
    > \
    :step < \
      :name "Post Fix" \
      :description "Mouse brain is post fixed in situ" \
      :subject -part p < \
        :metadata < \
           :definition -requirement mandatory "nig-daris:pssd-EAE-perfusion"  \
        > \
      > \
    > \

 ]
set kill_perfuse_mthd [xvalue id $r]



#######################
# Poste mortem MR scans
#######################

set r [ om.pssd.method.create \
    :namespace pssd/methods \
    :name "AMBMC post-mortem MR scans" \
    :description "Post mortem MR scan of mouse brain" \
  :step < \
     :name "In skull MR scan" \
     :description "MR acquisition" \
     :study < :type MR > \
    > \
   :step < \
     :name "Brain Removal" \
     :description "Mouse brain is removed" \
     :subject -part p < \
        :metadata < \
           :definition -requirement mandatory "nig-daris:pssd-AMBMC-brain-removal" \
        > \
      > \
    > \
    :step < \
       :name "Isolated MR scan" \
       :description "Isolated (brain removed) MR scan" \
       :study < :type MR > \
    > \
 ]
set mr_post_mortem_mthd [xvalue id $r]



# ================================
# AMBMC  - overall method
# ================================

om.pssd.method.for.subject.create \
    :namespace pssd/methods \
    :name "AMBMC Overall Method" \
    :description "Australian Mouse Brain Mapping Consortium Method" \
    :subject <\
       :project <\
          :public < \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-subject"  \
                :value < \
                  :type constant(animal) \
                > \
             > \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-animal-subject" \
                :value < \
                  :species constant(mouse) \
                > \
             > \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-animal-disease" \
             > \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-animal-genetics" \
             > \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-animal-modified-genetics" \
             > \
             :metadata < \
                :definition -requirement mandatory "nig-daris:pssd-identity" \
             > \
          > \
       >\
    >\
    :step < \
        :name "Live MRI" \
        :method < :id $mr_mthd > \
    > \
    :step < \
        :name "Kill and Perfuse" \
        :method < :id $kill_perfuse_mthd > \
    > \
    :step < \
        :name "Post mortem MR scans" \
        :method < :id $mr_post_mortem_mthd > \
    > \
 

#####################################################################################
