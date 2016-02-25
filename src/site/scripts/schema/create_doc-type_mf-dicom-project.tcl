# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:08:27 AEDT 2016


# Document: mf-dicom-project [version 1]
#
asset.doc.type.update :create yes :type mf-dicom-project \
  :label "mf-dicom-project" \
  :description "DICOM Project - identification of project for a DICOM Study." \
  :definition < \
    :element -name "id" -type "string" -index "true" -comparable "true" -min-occurs "0" \
    < \
      :description "Unique project identifier." \
      :restriction -base "string" \
      < \
        :case "upper" \
      > \
    > \
   >


