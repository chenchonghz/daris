#
# doc types for Bruker data
#

# ============================================================================
# daris:bruker-study
# ============================================================================
asset.doc.type.update \
    :create true :type daris:bruker-study   \
    :description "Document for Bruker Paravision study" \
    :label "Bruker Study" \
    :definition < \
        :element -name id  -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "Identifier of the study. It is the value of SUBJECT_study_nr in subject file." > \
        :element -name uid -type string -index true -min-occurs 0 -max-occurs 1 < \
           :description "Unique identifier of the bruker study. It is the value of SUBJECT_study_instance_uid in subject file." > \
        :element -name date -type date -index true -min-occurs 0 -max-occurs 1 < \
            :description "Date of acquisition" > \
        :element -name coil -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "Scanner coil used for acquisition" > \
        :element -name "ingest" -type "document" -index "true" -min-occurs "0" -max-occurs "1" < \
            :description "Ingest details." \
            :element -name "date" -type "date" -index "true" -max-occurs "1" < \
                :description "Date and time when the study was ingested." > \
            :element -name "domain" -type "string" -index "true" -max-occurs "1" < \
                :description "Domain of the user that ingested this study." > \
            :element -name "user" -type "string" -index "true" -max-occurs "1" < \
                :description "User that ingested this study." > \
            :element -name "from-token" -type boolean -index true -min-occurs 0 -max-occurs 1 < \
                :description "Was the domain and user generated from a secure identity token?" > > >

# ============================================================================
# daris:bruker-series
# ============================================================================
asset.doc.type.update \
    :create true :type daris:bruker-series   \
    :description "Document for Bruker Paravision study" \
    :label "Bruker Series" \
    :definition < \
        :element -name id  -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "Identifier of the Bruker series. It is the value generated from (procno | (expno << 16))." > \
        :element -name uid -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "Unique identifier of the Bruker series. It is the value of RECO_base_image_uid in reco file." > \
        :element -name protocol -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "The protocol name of the Bruker series. " > \
        :element -name acqTime -type string -index true -min-occurs 0 -max-occurs 1 < \
            :description "The acquisition time. " > >
