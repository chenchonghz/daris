
proc createDocType_pssd_subject { dmn } {

asset.doc.type.update :create yes :type ${dmn}:pssd-subject \
  :label "Subject" \
  :description "Basic domain-specific document type for a subject" \
  :definition < \
    :element -name "type" -type "enumeration" -index "true" -max-occurs "1" \
    < \
      :description "Type of subject.  Artificial might be used for, e.g. a phantom in an MR scanner. Internal might be used for an internally generated instrument system test (e.g. quality assurance" \
      :restriction -base "enumeration" \
      < \
        :value "animal" \
        :value "vegetable" \
        :value "mineral" \
        :value "artificial" \
        :value "internal" \
        :value "unknown" \
      > \
    > \
    :element -name "control" -type "boolean" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Subject is a member of a control group" \
    > \
   >
}


proc createDocType_pssd_animal_subject { dmn } {

asset.doc.type.update :create yes :type ${dmn}:pssd-animal-subject \
  :label "Animal subject" \
  :description "Basic document type for a domain specific animal (humans included) subject" \
  :definition < \
    :element -name "species" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Species type of the animal" \
      :restriction -base "enumeration" \
      < \
        :dictionary "pssd.animal.species" \
      > \
    > \
    :element -name "body-part" -type "enumeration" -index "true" -min-occurs "0"  \
    < \
      :description "Body part of the animal" \
      :restriction -base "enumeration" \
      < \
        :dictionary "pssd.animal.bodypart" \
      > \
      :attribute -name "sidedness" -type "boolean" -min-occurs "0" \
      < \
        :description "If the body part comes from the left or right (your convention for orientation) side you can specify here.  Don't supply to leave unspecified." \
      > \
    > \
    :element -name "gender" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Gender of the subject" \
      :restriction -base "enumeration" \
      < \
        :value "male" \
        :value "female" \
        :value "other" \
        :value "unknown" \
      > \
    > \
    :element -name "birthDate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Birth date of the subject" \
      :restriction -base "date" < :time false > \
    > \
    :element -name "deceased" -type "boolean" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Subject is deceased (cadaver)" \
    > \
    :element -name "deathDate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Death date of the subject" \
      :restriction -base "date" < :time false > \
    > \
    :element -name "age-at-death" -type "integer" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Age of subject (days or weeks) at time of death (intended for non-human subjects)." \
      :restriction -base "integer" \
      < \
        :minimum "0" \
      > \
      :attribute -name "units" -type "enumeration" -min-occurs "0" \
      < \
        :restriction -base "enumeration" \
        < \
          :value "days" \
          :value "weeks" \
        > \
      > \
    > \
    :element -name "weight-at-death" -type "float" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Weight of subject (g or Kg) at time of death (intended for non-human subjects." \
      :restriction -base "float" \
      < \
        :minimum "0" \
      > \
      :attribute -name "units" -type "enumeration" -min-occurs "0" \
      < \
        :restriction -base "enumeration" \
        < \
          :value "g" \
          :value "Kg" \
        > \
      > \
    > \
   >
}


proc createDocType_pssd_human_subject { dmn } {

asset.doc.type.update :create yes :type ${dmn}:pssd-human-subject \
  :label "Human Subject" \
  :description "Document type for a Human subject" \
  :definition < \
    :element -name "handedness" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Handedness of the subject" \
      :restriction -base "enumeration" \
      < \
        :value "left" \
        :value "right" \
        :value "ambidextrous" \
        :value "unknown" \
      > \
    > \
    :element -name "height" -type "float" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Height of subject (m)" \
      :restriction -base "float" \
      < \
        :minimum "0" \
      > \
      :attribute -name "units" -type "enumeration" -min-occurs "0" \
      < \
        :restriction -base "enumeration" \
        < \
          :value "m" \
        > \
      > \
    > \
   >
   }


proc createDocType_pssd_identity { dmn } {
asset.doc.type.update :create yes :type ${dmn}:pssd-identity \
  :label "External Subject Identifier" \
  :description "Document type for subject identity" \
  :definition < \
    :element -name "id" -type "string" -index "true" -min-occurs "0" \
    < \
      :description "Unique identifier for the subject allocated by some other authority for cross-referencing" \
     > \
   >
}
	
#=============================================================================
# Generally, human subjects are  re-used and so these meta-data should be placed
# on the Identity object
proc createDocType_pssd_human_identity { dmn } {

	asset.doc.type.update \
		:create true \
		:type ${dmn}:pssd-dmn-human-identity \
		:label "Human Identification" \
		:description "Document type for human subject identity" \
		:definition < \
		:element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix" \
		:element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
		:element -name middle -type string -min-occurs 0 -max-occurs 1 -length 100 -label "Middle" < \
		:description "If there are several 'middle' names then put them in this field" \
		> \
		:element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
		:element -name suffix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Suffix" \
		>
}




#============================================================================#
proc createDomainDocTypes { dmn } {

	createDocType_pssd_subject $dmn
	createDocType_pssd_animal_subject $dmn
	createDocType_pssd_human_subject $dmn
	createDocType_pssd_identity $dmn
	createDocType_pssd_human_identity $dmn
}

#============================================================================#
proc destroyDomainDocTypes { dmn } {

	set doctypes {  ${dmn}:pssd-subject ${dmn}:pssd-animal-subject  \
					${dmn}:pssd-human-subject ${dmn}:pssd-identity ${dmn}:pssd-human-identity }

	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}
                                                                        #

