# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:07:49 AEDT 2016


# Document: mf-dicom-study [version 1]
#
asset.doc.type.update :create yes :type mf-dicom-study \
  :label "DICOM Study" \
  :description "DICOM Study" \
  :definition < \
    :element -name "uid" -type "string" -index "true" -comparable "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Global unique identifier. DICOM element (0020,000D)." \
    > \
    :element -name "id" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Operator supplied identifier. DICOM element (0020,0010)." \
    > \
    :element -name "ingest" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Ingest details." \
      :element -name "date" -type "date" -index "true" -max-occurs "1" \
      < \
        :description "Date and time when the study was ingested." \
      > \
      :element -name "domain" -type "string" -index "true" -max-occurs "1" \
      < \
        :description "Domain of the user that ingested this study." \
      > \
      :element -name "user" -type "string" -index "true" -max-occurs "1" \
      < \
        :description "User that ingested this study." \
      > \
    > \
    :element -name "location" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "The place where the study was undertaken." \
      :element -name "institution" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Name of the institution. Derived from DICOM element (0008,0080)." \
      > \
      :element -name "station" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Name of the station. Derived from DICOM element (0008,1010)." \
      > \
    > \
    :element -name "equipment" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Information on the scanning equipment." \
      :element -name "manufacturer" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Equipment manufacturer. Derived from DICOM element (0008,0070)." \
      > \
      :element -name "model" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Equipment model. Derived from DICOM element (0008,1090)." \
      > \
    > \
    :element -name "description" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Description. Derived from DICOM element (0008,1030)." \
    > \
    :element -name "sdate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Date and time on which acquisition of the study was started. Derived from DICOM elements (0008,0020) and (0008,0030)." \
    > \
    :element -name "rpn" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Referring Physician's Name. Derived from DICOM element (0008,0090)." \
    > \
    :element -name "subject" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Subject statistics, at the time of study." \
      :element -name "sex" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Patient's sex. Derived from DICOM element (0010,0040)." \
        :restriction -base "enumeration" \
        < \
          :value "male" \
          :value "female" \
          :value "other" \
        > \
      > \
      :element -name "age" -type "float" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Age in years. Derived from DICOM element (0x0010,0x1010)." \
        :restriction -base "float" \
        < \
          :minimum "0" \
        > \
      > \
      :element -name "weight" -type "float" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Weight in kilograms. Derived from DICOM element (0010,1030)." \
        :restriction -base "float" \
        < \
          :minimum "0" \
        > \
      > \
      :element -name "size" -type "float" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Height/length in metres. Derived from DICOM element (0010,1020)." \
        :restriction -base "float" \
        < \
          :minimum "0" \
        > \
      > \
    > \
   >


