# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:09:01 AEDT 2016


# Document: mf-dicom-prefs [version 1]
#
asset.doc.type.update :create yes :type mf-dicom-prefs \
  :label "mf-dicom-prefs" \
  :description "DICOM preferences" \
  :definition < \
    :element -name "output" -type "document" -min-occurs "0" \
    < \
      :element -name "sink" -type "string" -min-occurs "0" \
      < \
        :description "The preferred output sink." \
      > \
      :element -name "format" -type "string" -min-occurs "0" \
      < \
        :description "The preferred output format." \
      > \
    > \
    :element -name "query" -type "document" -min-occurs "0" \
    < \
      :element -name "type" -type "string" -min-occurs "0" \
      < \
        :description "The preferred query type." \
      > \
      :element -name "value" -type "string" -min-occurs "0" \
      < \
        :description "The preferred query value." \
      > \
    > \
   >


