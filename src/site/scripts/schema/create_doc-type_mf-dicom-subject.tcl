# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:08:37 AEDT 2016


# Document: mf-dicom-subject [version 1]
#
asset.doc.type.update :create yes :type mf-dicom-subject \
  :label "mf-dicom-subject" \
  :description "DICOM Subject Identity" \
  :definition < \
    :element -name "id" -type "string" -index "true" -comparable "true" -max-occurs "1" \
    < \
      :description "Primary identification of the subject of a DICOM Study." \
      :restriction -base "string" \
      < \
        :case "upper" \
      > \
    > \
   >


