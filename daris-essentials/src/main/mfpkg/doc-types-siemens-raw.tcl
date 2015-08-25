#
# doc types for siemens raw data
# 

# ============================================================================
#  daris:siemens-raw-petct-study
# ============================================================================
asset.doc.type.update :create yes :type daris:siemens-raw-petct-study \
    :label "siemens-raw-petct-study" \
    :description "Meta-data describing a Study for raw PET/CT data from the Siemens scanner. The meta-data are very limited as they are parsed from the file name of the raw data." \
    :definition < \
        :element -name "date" -type "date" -index "true" -max-occurs "1" < \
            :description "Date of acquisition" \
            :restriction -base "date" < \
                :time "false" > > \
        :element -name "ingest" -type "document" -index "true" -max-occurs "1" < \
            :description "Details for when the data were ingested" \
            :element -name "date" -type "date" -index "true" -max-occurs "1" < \
                :description "Date of ingestion" > \
            :element -name "domain" -type "string" -index "true" -min-occurs 0 -max-occurs "1" < \
                :description "Domain of the user that ingested this study" > \
            :element -name "user" -type "string" -index "true" -min-occurs 0 -max-occurs "1" < \
                :description "User that ingested this study." > \
            :element -name "from-token" -type boolean -index true -min-occurs 0 -max-occurs 1 < \
                :description "Was the domain and user generated from a secure identity token?" > > >

# ============================================================================
# daris:siemens-raw-mr-study
# ============================================================================
asset.doc.type.update :create yes :type daris:siemens-raw-mr-study \
    :label "siemens-raw-mr-study" \
    :description "Meta-data describing a Study for raw MR data from the Siemens 7T scanner.  The meta-data are very limited as they are parsed from the file header." \
    :definition < \
        :element -name "date" -type "date" -index "true" -min-occurs 0 -max-occurs "1" < \
            :description "Date of acquisition" \
            :restriction -base "date" < \
                :time "false" > > \
        :element -name "ingest" -type "document" -index "true" -max-occurs "1" < \
            :description "Details for when the data were ingested" \
            :element -name "date" -type "date" -index "true" -max-occurs "1" < \
                :description "Date of ingestion" > \
            :element -name "domain" -type "string" -index "true" -min-occurs 0 -max-occurs "1" < \
                :description "Domain of the user that ingested this study" > \
            :element -name "user" -type "string" -index "true" -min-occurs 0 -max-occurs "1" < \
                :description "User that ingested this study." > \
            :element -name "from-token" -type boolean -index true -min-occurs 0 -max-occurs 1 < \
                :description "Was the domain and user generated from a secure identity token?" > > >

#=============================================================================
# daris:siemens-raw-petct-series
#=============================================================================
asset.doc.type.update :create yes :type daris:siemens-raw-petct-series \
    :label "siemens-raw-petct-series" \
    :description "Meta-describing raw Siemens PET and CT files" \
    :definition < \
    :element -name "date" -type "date" -index "true" -max-occurs "1" < \
        :description "Date and time of the acquisition" > \
    :element -name "modality" -type "enumeration" -max-occurs "1" < \
        :description "Modality of data " \
        :restriction -base "enumeration" < \
            :value "PT" \
            :value "CT" \
            :case-sensitive "true" > > \
    :element -name "description" -type "string" -index "true" -max-occurs "1" < \
        :description "Description of the acquisition" > \
    :element -name "series_number" -type "string" -max-occurs "1" < \
        :description "The series number set by the scanner" > \
    :element -name "type" -type "enumeration" -max-occurs "1" < \
        :description "The acquisition type" \
        :restriction -base "enumeration" < \
            :value "RAW" \
            :value "LM" \
            :value "NORM" \
            :value "PROTOCOL" \
            :value "SINO" \
            :case-sensitive "true" > > \
    :element -name "instance" -type "string" -index "true" -max-occurs "1" < \
        :description "The instance number assigned by the scanner" > \
    :element -name "uuid" -type "string" -index "true" -max-occurs "1" < \
        :description "UUID assigned by the scanner" > \
    :element -name "date-export" -type "date" -index "true" -max-occurs "1" < \
        :description "Date and time that the acquisition was exported from the Biograph Console system" > \
    :element -name "date-expire" -type "date" -index true -min-occurs 0 -max-occurs 1 < \
        :description "Date when asset expires from archive and can be destroyed." > >
