# ============================================================================
# This dictionary supplies asset namespaces for creating PSSD projects in that
# the portal uses to present Project creators a pull-down list.  The service 
# interface for project creation just takes a string still as we don't want to
# remove flexibility domain specific packages can add to this dictionary.  The 
# pssd namespace is made in the installation of the daris-core-services package
# ============================================================================
proc createDict_pssd_project_asset_namespaces {} {
	if { [xvalue exists [dictionary.exists :name daris:pssd.project.asset.namespaces]] == "false" } {
		dictionary.create :name daris:pssd.project.asset.namespaces :description "Asset namespaces for PSSD projects (used by the portal)." :case-sensitive true
	}
    set default_ns [xvalue property [application.property.get :property -app daris -ifexists true  daris.namespace.default]]
    if { ${default_ns} != "" } {
        set default_project_ns "${default_ns}/pssd"
        if { [xvalue exists [asset.namespace.exists :namespace ${default_project_ns}]] == "true" && [xvalue exists [dictionary.entry.exists :dictionary daris:pssd.project.asset.namespaces :term ${default_project_ns}]] == "false" } {
            dictionary.entry.add :dictionary daris:pssd.project.asset.namespaces :term ${default_project_ns}
        }
    }
}

# ============================================================================
# This dictionary supplies CID root strings  for creating PSSD projects in 
# that the portal uses to present Project creators a pull-down list.  The 
# service interface for project creation just takes a string still as we don't
# want to remove flexibility domain specific packages can add to this dictionary.
# These standard CID roots are created in the pssd package installer.
# ============================================================================
proc createDict_pssd_project_cid_roots {} {
	if { [xvalue exists [dictionary.exists :name daris:pssd.project.cid.rootnames]] == "false" } {
		dictionary.create :name daris:pssd.project.cid.rootnames :description "Project CID root names (used by the portal)." :case-sensitive true
	}
    addDictionaryEntry daris:pssd.project.cid.rootnames "pssd.project" "Standard CID root names for PSSD Projects"
}

# ============================================================================
# This dictionary supplies keywords.
# ============================================================================
proc createDict_pssd_research_keywords {} {
	if { [xvalue exists [dictionary.exists :name daris:pssd.research.keyword]] == "false" } {
		dictionary.create :name daris:pssd.research.keyword :description "Standard key words to be set on Projects." :case-sensitive true
	}
}

# ============================================================================
# This dictionary is not research domain specific.  I.e. the list of 
# organizations transcends research domains. Therefore it's ok for it to be in 
# the PSSD package. It is used in the om.pssd.project.metadata.harvest service
# to harvest meta-data for ANDS.
# ============================================================================
proc createDict_research_organization { } {
	if { [xvalue exists [dictionary.exists :name daris:pssd.research.organization]] == "false" } {
		dictionary.create :name daris:pssd.research.organization :description "Research Organizations" :case-sensitive true
	}
	addDictionaryEntry  daris:pssd.research.organization "Austin Health"
	addDictionaryEntry  daris:pssd.research.organization "Baker IDI Heart and Diabetes Institute"
	addDictionaryEntry  daris:pssd.research.organization "Commonwealth Scientific Industrial Research Organisation"
	addDictionaryEntry  daris:pssd.research.organization "Florey Neuroscience Institutes"
	addDictionaryEntry  daris:pssd.research.organization "La Trobe University"
	addDictionaryEntry  daris:pssd.research.organization "Ludwig Institute for Cancer Research"
	addDictionaryEntry  daris:pssd.research.organization "Melbourne Health"
	addDictionaryEntry  daris:pssd.research.organization "Mental Health Research Institute"
	addDictionaryEntry  daris:pssd.research.organization "Murdoch Childrens Research Institute"
	addDictionaryEntry  daris:pssd.research.organization "Monash University"
	addDictionaryEntry  daris:pssd.research.organization "Royal Melbourne Hospital"
	addDictionaryEntry  daris:pssd.research.organization "Sunshine Hospital"
	addDictionaryEntry  daris:pssd.research.organization "The University of Melbourne"
	addDictionaryEntry  daris:pssd.research.organization "The University of Sydney"
	addDictionaryEntry  daris:pssd.research.organization "The University of Queensland"
	addDictionaryEntry  daris:pssd.research.organization "Victoria University"
	addDictionaryEntry  daris:pssd.research.organization "Walter and Eliza Hall Institute"
	addDictionaryEntry  daris:pssd.research.organization "Other research organization"
}

# ============================================================================
# Dictionary for ethics organizations.
#=============================================================================
proc createDict_ethics_organization { } {

	if { [xvalue exists [dictionary.exists :name daris:pssd.ethics.organization]] == "false" } {
		dictionary.create :name daris:pssd.ethics.organization :description "Organizations that supply ethics approval for projects" :case-sensitive true
	}
	addDictionaryEntry  daris:pssd.ethics.organization "Austin Heath Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Alfred Medical Research and Education Precinct Animal Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Eye and Ear Hospital Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Florey Neuroscience Institutes Animal Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Howard Florey Institute Animal Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Melbourne Health Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Murdoch Childrens Research Institute Animal Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Monash University Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Royal Children's Hospital Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "University of Melbourne Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "The University of Sydney Human Research Ethics Committee"
	addDictionaryEntry  daris:pssd.ethics.organization "Other"
}

# ============================================================================
# Dictionary for funding organizations
# ============================================================================
proc createDict_funding_organization { } {
	if { [xvalue exists [dictionary.exists :name daris:pssd.funding.organization]] == "false" } {
		dictionary.create :name daris:pssd.funding.organization :description "Organizations that supply funding" :case-sensitive true
	}
	addDictionaryEntry  daris:pssd.funding.organization "Australian and New Zealand College of Anaesthetists"
	addDictionaryEntry  daris:pssd.funding.organization "Australian Research Council"
	addDictionaryEntry  daris:pssd.funding.organization "Derek Denton Endowment"
	addDictionaryEntry  daris:pssd.funding.organization "Florey Neuroscience Institutes"
	addDictionaryEntry  daris:pssd.funding.organization "Fred P Archer Charitable Trust"
	addDictionaryEntry  daris:pssd.funding.organization "General Electric"
	addDictionaryEntry  daris:pssd.funding.organization "National Health and Medical Research Council"
	addDictionaryEntry  daris:pssd.funding.organization "National Imaging Facility"
	addDictionaryEntry  daris:pssd.funding.organization "Research Data Services"
	addDictionaryEntry  daris:pssd.funding.organization "Other funding entity"					
	addDictionaryEntry  daris:pssd.funding.organization "The University of Melbourne"
	addDictionaryEntry  daris:pssd.funding.organization "The University of Sydney"
	addDictionaryEntry  daris:pssd.funding.organization "The University of Queensland"
	addDictionaryEntry  daris:pssd.funding.organization "Other"							
}

# ============================================================================
# Dictionary for DICOM modalities. The reference is the NEMA standard
# (http://medical.nema.org/) volume 03 Section C.7.3.1.1 Modality
# Modality is DICOM element  (0008,0060)
# ============================================================================
proc createDict_DICOM_Modalities {} {
	if { [xvalue exists [dictionary.exists :name daris:pssd.dicom.modality]] == "false" } {
		dictionary.create :name daris:pssd.dicom.modality \
		   :description "DICOM Modality strings. See http://medical.nema.org, volume 03 Section C.7.3.1.1 Modality." :case-sensitive true	
	}
	addDictionaryEntry  daris:pssd.dicom.modality AR "Autorefraction"
	addDictionaryEntry  daris:pssd.dicom.modality AU "Audio"
	addDictionaryEntry  daris:pssd.dicom.modality BDUS "Bone Densitometry (ultrasound)"
	addDictionaryEntry  daris:pssd.dicom.modality BI "Biomagnetic imaging"
	addDictionaryEntry  daris:pssd.dicom.modality CR "Computed Radiography"
	addDictionaryEntry  daris:pssd.dicom.modality CT "Computed Tomography"
	addDictionaryEntry  daris:pssd.dicom.modality DG "Diaphanography"
	addDictionaryEntry  daris:pssd.dicom.modality DX "Digital Radiography"
	addDictionaryEntry  daris:pssd.dicom.modality DOC "Document"
	addDictionaryEntry  daris:pssd.dicom.modality ECG "Electrocardiography"
	addDictionaryEntry  daris:pssd.dicom.modality EPS "Cardiac Electrophysiology"
	addDictionaryEntry  daris:pssd.dicom.modality ES "Endoscopy"
	addDictionaryEntry  daris:pssd.dicom.modality GM "General Microscopy"
	addDictionaryEntry  daris:pssd.dicom.modality HC "Hard Copy"
	addDictionaryEntry  daris:pssd.dicom.modality IO "Intra-oral Radiography"
	addDictionaryEntry  daris:pssd.dicom.modality IVOCT "Intravascular Optical Coherence Tomography"
	addDictionaryEntry  daris:pssd.dicom.modality KO "Key Object Selection"
	addDictionaryEntry  daris:pssd.dicom.modality LS "Laser surface scan"
	addDictionaryEntry  daris:pssd.dicom.modality MG "Mammography"
	addDictionaryEntry  daris:pssd.dicom.modality MR "Magnetic Resonance"
	addDictionaryEntry  daris:pssd.dicom.modality NM "Nuclear Medicine"
	addDictionaryEntry  daris:pssd.dicom.modality OAM "Ophthalmic Axial Measurements"
	addDictionaryEntry  daris:pssd.dicom.modality OCT "Optical Coherence Tomography (non-Ophthalmic)"
	addDictionaryEntry  daris:pssd.dicom.modality OP "Ophthalmic Photography"
	addDictionaryEntry  daris:pssd.dicom.modality OPV "Ophthalmic Visual Field"
	addDictionaryEntry  daris:pssd.dicom.modality OT "Other"
	addDictionaryEntry  daris:pssd.dicom.modality PLAN "Plan"
	addDictionaryEntry  daris:pssd.dicom.modality PR "Presentation State"
	addDictionaryEntry  daris:pssd.dicom.modality PT "Positron emission tomography (PET)"
	addDictionaryEntry  daris:pssd.dicom.modality PX "Panoramic X-Ray"
	addDictionaryEntry  daris:pssd.dicom.modality REG "Registration"
	addDictionaryEntry  daris:pssd.dicom.modality RF "Radio Fluoroscopy"
	addDictionaryEntry  daris:pssd.dicom.modality RG "Radiographic imaging (conventional film/screen)"
	addDictionaryEntry  daris:pssd.dicom.modality RTDOSE "Radiotherapy Dose"
	addDictionaryEntry  daris:pssd.dicom.modality RTIMAGE "Radiotherapy Image"
	addDictionaryEntry  daris:pssd.dicom.modality RTPLAN "Radiotherapy Plan"
	addDictionaryEntry  daris:pssd.dicom.modality RTSTRUCT "Radiotherapy Structure Set"
	addDictionaryEntry  daris:pssd.dicom.modality RTRECORD "RT Treatment Record"
	addDictionaryEntry  daris:pssd.dicom.modality SM "Slide Microscopy"
	addDictionaryEntry  daris:pssd.dicom.modality SR "Structured Reporting Document"
	addDictionaryEntry  daris:pssd.dicom.modality TG "Thermography"
	addDictionaryEntry  daris:pssd.dicom.modality US "Ultrasound"
	addDictionaryEntry  daris:pssd.dicom.modality VA "Visual Acuity"
	addDictionaryEntry  daris:pssd.dicom.modality XA "X-Ray Angiography"
	addDictionaryEntry  daris:pssd.dicom.modality XC "External-camera Photography"
}

# ============================================================================
# Dictionary for name prefixes.
# ============================================================================
proc createDict_human_name_prefixes { } {

	if { [xvalue exists [dictionary.exists :name daris:pssd.human.name.prefix]] == "false" } {
		dictionary.create :name daris:pssd.human.name.prefix :description "Human name prefixes" :case-sensitive true
	}
	addDictionaryEntry  daris:pssd.human.name.prefix "Associate Professor"
	addDictionaryEntry  daris:pssd.human.name.prefix "Dr."
	addDictionaryEntry  daris:pssd.human.name.prefix "Mr."
	addDictionaryEntry  daris:pssd.human.name.prefix "Mrs."
	addDictionaryEntry  daris:pssd.human.name.prefix "Ms."
	addDictionaryEntry  daris:pssd.human.name.prefix "Professor"
	addDictionaryEntry  daris:pssd.human.name.prefix "Sir"	
}

# ============================================================================
# Dictionary for public identifier types.
# ============================================================================
proc createDict_publication_identifier_types { } {

   if { [xvalue exists [dictionary.exists :name daris:pssd.publication.identifier.type]] == "false" } {
		dictionary.create :name daris:pssd.publication.identifier.type :description "Common publication identifier types" :case-sensitive false
	}
	addDictionaryEntry daris:pssd.publication.identifier.type ark
	addDictionaryEntry daris:pssd.publication.identifier.type doi
	addDictionaryEntry daris:pssd.publication.identifier.type ean13
	addDictionaryEntry daris:pssd.publication.identifier.type eissn
	addDictionaryEntry daris:pssd.publication.identifier.type handle
	addDictionaryEntry daris:pssd.publication.identifier.type infouri
	addDictionaryEntry daris:pssd.publication.identifier.type purl
	addDictionaryEntry daris:pssd.publication.identifier.type uri
	addDictionaryEntry daris:pssd.publication.identifier.type issn
	addDictionaryEntry daris:pssd.publication.identifier.type isbn
	addDictionaryEntry daris:pssd.publication.identifier.type istc
	addDictionaryEntry daris:pssd.publication.identifier.type lissn
	addDictionaryEntry daris:pssd.publication.identifier.type upc
    addDictionaryEntry daris:pssd.publication.identifier.type urn
	addDictionaryEntry daris:pssd.publication.identifier.type mediatype
}

# ============================================================================
# Create all dictionaries.
# ============================================================================
proc createUpdatePSSDDicts { } {
    createDict_research_organization
	createDict_ethics_organization
	createDict_funding_organization
	createDict_DICOM_Modalities
	createDict_human_name_prefixes
	createDict_publication_identifier_types
    createDict_pssd_project_asset_namespaces
    createDict_pssd_project_cid_roots
    createDict_pssd_research_keywords
}

# ============================================================================
# Destroy all dictionaries.
# ============================================================================
proc destroyPSSDDicts { } {
	set dicts { daris:pssd.research.organization daris:pssd.dicom.modality \
	            daris:pssd.human.name.prefix daris:pssd.publication.identifier.type \
	            daris:pssd.funding.organization daris:pssd.ethics.organization \
	            daris:pssd.project.asset.namespaces daris:pssd.project.cid.rootnames \
	            daris:pssd.research.keyword}
	foreach dict $dicts {
		if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
			dictionary.destroy :name $dict
		}
	}
}
