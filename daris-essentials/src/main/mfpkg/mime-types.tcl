
# ============================================================================
# function to create/register a mime type in Mediaflux.
# ============================================================================
proc create_mime_type { type { description "" } { extension "" } { compressable "" } } {
    if { [xvalue exists [type.exists :type $type]] == "false" } {
        set cmd "type.create :type ${type}"
        if { $description != "" } {
            set cmd "${cmd} :description ${description}"
        }
        if { $extension != "" } {
            set cmd "${cmd} :extension ${extension}"
        }
        if { $compressable != "" } {
            set cmd "${cmd} :compressable ${compressable}"
        }
        $cmd
    }
}

# ============================================================================
# asset mime types
# ============================================================================
create_mime_type "dicom/series"      "DICOM series"
create_mime_type "analyze/series/nl" "Analyze(Neurological)"
create_mime_type "analyze/series/rl" "Analyze(Radiological)"
create_mime_type "nifti/series"      "NIFTI series"
create_mime_type "minc/series"       "MINC series"
create_mime_type "bruker/series"     "Bruker/Paravision image series"
create_mime_type "bruker/fid"        "Bruker Free-Induction Decay data"
# Siemens RDA spectrum type
create_mime_type "siemens/rda"       "RDA(Siemens Spectrum)"
# Siemens RAW PET/CT format. The Doc Types are created in the PSSD package
# and used by the Siemens raw PET/CT client to upload data.
create_mime_type "siemens-raw-petct/study" "Siemens RAW PET/CT Study data"
# DataSets (Series) are either PET or CT
create_mime_type "siemens-raw-pet/series" "Siemens RAW PET data"
create_mime_type  siemens-raw-ct/series "Siemens RAW CT data"

# ============================================================================
# asset content mime types. These parallel those found already in Mediaflux 
# like application/dicom, application/dcm
# ============================================================================
create_mime_type "application/minc"            "MINC image"             "mnc" "yes"
create_mime_type "application/siemens-raw-pet" "Siemens RAW PET format" "ptd" "no"
create_mime_type "application/siemens-raw-ct"  "Siemens RAW CT format"  "ptr" "no"
