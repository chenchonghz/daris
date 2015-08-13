proc create_mime_type { type desc } {
    if { [xvalue exists [type.exists :type ${type}]] == "false" } {
        type.create :type ${type} :description ${desc}
    }
}

# create all supported mime types if not exist. These are primarily made 
# in the essentials package.But in case you use this package without it, 
# they are made here too.

create_mime_type "dicom/series"      "DICOM series"
create_mime_type "analyze/series/nl" "Analyze(Neurological)"
create_mime_type "analyze/series/rl" "Analyze(Radiological)"
create_mime_type "nifti/series"      "NIFTI series"
create_mime_type "siemens/rda"       "RDA(Siemens Spectrum)"
create_mime_type "bruker/series"     "Bruker/Paravision image series"
create_mime_type "bruker/fid"        "Bruker Free-Induction Decay data"
create_mime_type "minc/series"       "MINC series"

