# ============================================================================
# Create MIME Types for asset types
# ============================================================================
proc createAssetMimeType { type description } {
    
    if { [xvalue exists [type.exists :type $type]] == "false" } {
       type.create :type $type :description $description
    }
}

proc createContentMimeType { type description extension compressable } {
    
    if { [xvalue exists [type.exists :type $type]] == "false" } {
	   type.create :type $type :description $description :extension $extension :compressable $compressable
    }
}


#########################################################
# Asset TYpes. Asset types may also be added in Java code.
#########################################################

createAssetMimeType dicom/series      "DICOM series"
createAssetMimeType analyze/series/nl "Analyze(Neurological)"
createAssetMimeType analyze/series/rl "Analyze(Radiological)"
createAssetMimeType nifti/series      "NIFTI series"
createAssetMimeType minc/series       "MINC series"
createAssetMimeType bruker/series     "Bruker/Paravision image series"
createAssetMimeType bruker/fid        "Bruker Free-Induction Decay data"

# Siemens RDA spectrum type
createAssetMimeType siemens/rda       "RDA(Siemens Spectrum)"
# Siemens RAW PET/CT format.  The Doc Types are created in the PSSD package
# and used by the Siemens raw PET/CT client to upload data.
createAssetMimeType  siemens-raw-petct/study "Siemens RAW PET/CT Study data"
# DataSets (Series) are either PET or CT
createAssetMimeType  siemens-raw-pet/series "Siemens RAW PET data"
createAssetMimeType  siemens-raw-ct/series "Siemens RAW CT data"



# Content types. These parallel those found already
# in Mediaflux like application/dicom, application/dcm
createContentMimeType "application/minc" "MINC image" "mnc" yes
createContentMimeType "application/siemens-raw-pet" "Siemens RAW PET format" "ptd" no
createContentMimeType "application/siemens-raw-ct" "Siemens RAW CT format" "ptr" no
