
#============================================================================#
# creates dictionary of study types.                                         #
# This populates the framework dictionary pssd.study.types dictionary (this  #
# could be changed to something more extensible). This list is clearly       #
# specific to bio-medical imaging and is derived originally partly from the  #
# DICOM modalities.                                                          #
#============================================================================#
proc create_domain_study_types { } {
    om.pssd.study.type.create :type "Bone Densitometry (ultrasound)"
    om.pssd.study.type.create :type "Computed Radiography"
    om.pssd.study.type.create :type "Computed Tomography"
    om.pssd.study.type.create :type "Electrocardiography"
    om.pssd.study.type.create :type "Dose Report"
    om.pssd.study.type.create :type "Cardiac Electrophysiology"
    om.pssd.study.type.create :type "General Microscopy"
    om.pssd.study.type.create :type "Intravascular Optical Coherence Tomography"
    om.pssd.study.type.create :type "Magnetic Resonance Imaging"
    om.pssd.study.type.create :type "Optical Microscopy"
    om.pssd.study.type.create :type "Electron Microscopy"
    om.pssd.study.type.create :type "Mammography"
    om.pssd.study.type.create :type "Nuclear Medicine"
    om.pssd.study.type.create :type "Positron Emission Tomography"
    om.pssd.study.type.create :type "Radio Fluoroscopy"
    om.pssd.study.type.create :type "Slide Microscopy"
    om.pssd.study.type.create :type "Ultra Sound"
    om.pssd.study.type.create :type "X-Ray Angiography"
    om.pssd.study.type.create :type "Combined Results" :description "Combined Results from other Studies"
    om.pssd.study.type.create :type "Positron Emission Tomography/Computed Tomography"
    om.pssd.study.type.create :type "Quality Assurance"
    om.pssd.study.type.create :type "Unspecified"	
}
