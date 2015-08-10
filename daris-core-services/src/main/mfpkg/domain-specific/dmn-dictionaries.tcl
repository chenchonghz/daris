#============================================================================#
# creates dictionary: ${ns}:pssd.animal.bodypart                             #
#============================================================================#
proc create_dict_pssd_animal_bodypart { ns } {
    set dict ${ns}:pssd.animal.bodypart
    set desc "Animal body parts"
    if { [xvalue exists [dictionary.exists :name ${dict}]] == "false" } {
        dictionary.create :name ${dict} :description ${desc} :case-sensitive true
    }
    dictionary.entry.add :dictionary ${dict} :term abdomen
    dictionary.entry.add :dictionary ${dict} :term arm
    dictionary.entry.add :dictionary ${dict} :term "lower arm"
    dictionary.entry.add :dictionary ${dict} :term "upper arm"
    dictionary.entry.add :dictionary ${dict} :term chest
    dictionary.entry.add :dictionary ${dict} :term fibula
    dictionary.entry.add :dictionary ${dict} :term femur
    dictionary.entry.add :dictionary ${dict} :term foot
    dictionary.entry.add :dictionary ${dict} :term groin
    dictionary.entry.add :dictionary ${dict} :term hand
    dictionary.entry.add :dictionary ${dict} :term head
    dictionary.entry.add :dictionary ${dict} :term humerus
    dictionary.entry.add :dictionary ${dict} :term knee
    dictionary.entry.add :dictionary ${dict} :term leg
    dictionary.entry.add :dictionary ${dict} :term "lower leg"
    dictionary.entry.add :dictionary ${dict} :term "upper leg"
    dictionary.entry.add :dictionary ${dict} :term neck
    dictionary.entry.add :dictionary ${dict} :term radius
    dictionary.entry.add :dictionary ${dict} :term shoulder
    dictionary.entry.add :dictionary ${dict} :term skull    
    dictionary.entry.add :dictionary ${dict} :term thigh
    dictionary.entry.add :dictionary ${dict} :term tibia
    dictionary.entry.add :dictionary ${dict} :term ulna
}

#============================================================================#
# creates dictionary: ${ns}:pssd.animal.species                              #
#============================================================================#
proc create_dict_pssd_animal_species { ns } {
    set dict ${ns}:pssd.animal.species
    set desc "Animal species"
    if { [xvalue exists [dictionary.exists :name ${dict}]] == "false" } {
        dictionary.create :name ${dict} :description ${desc} :case-sensitive true
    }
    dictionary.entry.add :dictionary ${dict} :term human
    dictionary.entry.add :dictionary ${dict} :term baboon
    dictionary.entry.add :dictionary ${dict} :term cat
    dictionary.entry.add :dictionary ${dict} :term "cane toad"
    dictionary.entry.add :dictionary ${dict} :term chicken
    dictionary.entry.add :dictionary ${dict} :term dog
    dictionary.entry.add :dictionary ${dict} :term echidna
    dictionary.entry.add :dictionary ${dict} :term goanna
    dictionary.entry.add :dictionary ${dict} :term guineapig
    dictionary.entry.add :dictionary ${dict} :term harrier
    dictionary.entry.add :dictionary ${dict} :term marmoset
    dictionary.entry.add :dictionary ${dict} :term mouse
    dictionary.entry.add :dictionary ${dict} :term monkey
    dictionary.entry.add :dictionary ${dict} :term pig
    dictionary.entry.add :dictionary ${dict} :term rat
    dictionary.entry.add :dictionary ${dict} :term rabbit
    dictionary.entry.add :dictionary ${dict} :term sheep
    dictionary.entry.add :dictionary ${dict} :term wallaby
}

#============================================================================#
# creates all domain-specific dictionaries.                                  #
#============================================================================#
proc create_domain_dicts { ns } {
    create_dict_pssd_animal_species ${ns}
    create_dict_pssd_animal_bodypart ${ns}
}

#============================================================================#
# destroys all domain-specific dictionaries.                                 #
#============================================================================#
proc destroy_domain_dicts { ns } {
    set dicts { \
        ${ns}:pssd.animal.species \
        ${ns}:pssd.animal.bodypart }
    foreach dict $dicts {
        if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
            dictionary.destroy :name ${dict}
        }
    }
}
