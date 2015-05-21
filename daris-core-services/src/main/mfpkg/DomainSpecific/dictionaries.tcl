#=============================================================================
proc createDict_pssd_animal_bodypart { dmn } {

	if { [xvalue exists [dictionary.exists :name ${dmn}:pssd.animal.bodypart]] == "false" } {
		dictionary.create :name ${dmn}:pssd.animal.bodypart :description "Animal Body Parts" :case-sensitive true
	}
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart abdomen
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart arm
  	addDictionaryEntry  ${dmn}:pssd.animal.bodypart "lower arm"
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart "upper arm"
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart chest
    addDictionaryEntry  ${dmn}:pssd.animal.bodypart fibula
    addDictionaryEntry  ${dmn}:pssd.animal.bodypart femur
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart foot
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart groin
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart hand
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart head
   	addDictionaryEntry  ${dmn}:pssd.animal.bodypart humerus
    addDictionaryEntry  ${dmn}:pssd.animal.bodypart knee
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart leg
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart "lower leg"
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart "upper leg"
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart neck
  	addDictionaryEntry  ${dmn}:pssd.animal.bodypart radius
	addDictionaryEntry  ${dmn}:pssd.animal.bodypart shoulder
   	addDictionaryEntry  ${dmn}:pssd.animal.bodypart skull  	
    addDictionaryEntry  ${dmn}:pssd.animal.bodypart thigh
    addDictionaryEntry  ${dmn}:pssd.animal.bodypart tibia
  	addDictionaryEntry  ${dmn}:pssd.animal.bodypart ulna
}

#=============================================================================
proc createDict_pssd_animal_species { dmn } {

	if { [xvalue exists [dictionary.exists :name ${dmn}:pssd.animal.species]] == "false" } {
		dictionary.create :name ${dmn}:pssd.animal.species :description "Animal species" :case-sensitive true
	}
	addDictionaryEntry  ${dmn}:pssd.animal.species human
	addDictionaryEntry  ${dmn}:pssd.animal.species baboon
	addDictionaryEntry  ${dmn}:pssd.animal.species cat
	addDictionaryEntry  ${dmn}:pssd.animal.species "cane toad"
	addDictionaryEntry  ${dmn}:pssd.animal.species chicken
	addDictionaryEntry  ${dmn}:pssd.animal.species dog
    addDictionaryEntry  ${dmn}:pssd.animal.species echidna
	addDictionaryEntry  ${dmn}:pssd.animal.species goanna
	addDictionaryEntry  ${dmn}:pssd.animal.species guineapig
	addDictionaryEntry  ${dmn}:pssd.animal.species harrier
	addDictionaryEntry  ${dmn}:pssd.animal.species marmoset
	addDictionaryEntry  ${dmn}:pssd.animal.species mouse
	addDictionaryEntry  ${dmn}:pssd.animal.species monkey
	addDictionaryEntry  ${dmn}:pssd.animal.species pig
	addDictionaryEntry  ${dmn}:pssd.animal.species rat
	addDictionaryEntry  ${dmn}:pssd.animal.species rabbit
	addDictionaryEntry  ${dmn}:pssd.animal.species sheep
	addDictionaryEntry  ${dmn}:pssd.animal.species wallaby
}



#============================================================================#
proc createDomainDicts { dmn } {

	createDict_pssd_animal_species $dmn
	createDict_pssd_animal_bodypart $dmn
}

#============================================================================#
proc destroyDomainDicts { dmn } {

	set dicts { ${dmn}:pssd.animal.species ${dmn}:pssd.animal.bodypart \
		   }
	foreach dict $dicts {
		if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
			dictionary.destroy :name $dict
		}
	}
}

