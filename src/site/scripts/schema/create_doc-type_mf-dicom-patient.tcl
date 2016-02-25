# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:07:29 AEDT 2016


# Document: mf-dicom-patient [version 2]
#
asset.doc.type.update :create yes :type mf-dicom-patient \
  :label "mf-dicom-patient" \
  :description "DICOM Patient" \
  :definition < \
    :element -name "id" -type "string" -index "true" -comparable "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Primary hospital identification number or code for the patient. Derived from DICOM element (0010,0020)." \
    > \
    :element -name "name" -type "string" -index "true" -comparable "true" -min-occurs "0" \
    < \
      :description "Patient name. Derived from DICOM element (0010,0010)." \
      :attribute -name "type" -type "enumeration" -index "true" \
      < \
        :description "Type of name." \
        :restriction -base "enumeration" \
        < \
          :value "first" \
          :value "last" \
          :value "middle" \
          :value "prefix" \
          :value "suffix" \
          :value "other" \
          :value "full" \
        > \
      > \
    > \
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
    :element -name "dob" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Patient's date of birth. Derived from DICOM elements (0010,0030) and (0010,0032)." \
      :restriction -base "date" \
      < \
        :time "false" \
      > \
    > \
   >


