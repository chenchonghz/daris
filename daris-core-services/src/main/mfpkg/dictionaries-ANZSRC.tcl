#============================================================================================#
# These dictionaries supply values for the Australian and New Zealand Standard Research Classifications
# See http://www.abs.gov.au/ausstats/abs@.nsf/0/4AE1B46AE2048A28CA25741800044242?opendocument
# Values drawn from here may be utilised in standard processes like harvesting meta-data for
# the ANDS registry

proc createDict_FieldOfResearch { } {

#
# These are for division 11, Medical and Health Sciences
#	
	if { [xvalue exists [dictionary.exists :name daris:pssd.ANZSRC.Division-11.field-of-research]] == "false" } {
		dictionary.create :name daris:pssd.ANZSRC.Division-11.field-of-research \
		     :description "Standard Field of Research classification for Division 11 (Medical and Health Sciences) for the Australian and New Zealand Standard Research Classification. See http://www.abs.gov.au/ausstats/abs@.nsf/0/4AE1B46AE2048A28CA25741800044242?opendocument" \
		     :case-sensitive true
	}
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1101 - Medical Biochemistry and Metabolomics"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1102 - Cardiorespiratory Medicine and Haematology"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1103 - Clinical Sciences"
    addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1104 - Complementary and Alternative Medicine"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1105 - Dentistry"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1106 - Human Movement and Sports Science"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1107 - Immunology"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1108 - Medical Microbiology"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1109 - Neurosciences"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1110 - Nursing"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1111 - Nutrition and Dietetics"	
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1112 - Oncology and Carcinogenesis"		
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1113 - Ophthalmology and Optometry"	
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1114 - Paediatrics and Reproductive Medicine"	
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1115 - Pharmacology and Pharmaceutical Sciences"	
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1116 - Medical Physiology"
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1117 - Public Health and Health Services"		
	addDictionaryEntry  daris:pssd.ANZSRC.Division-11.field-of-research "1199 - Other Medical and Health Sciences"								
}

#============================================================================#
proc createUpdateANZSRCPSSDDicts { } {
        createDict_FieldOfResearch
}

#============================================================================#
proc destroyPSSDDicts { } {

	set dicts { daris:pssd.ANZSRC.Division-11.field-of-research }
	foreach dict $dicts {
		if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
			dictionary.destroy :name $dict
		}
	}
}

