# Document creation script.
#
#   Server: 1128
#   Date:   Thu Feb 25 10:08:03 AEDT 2016


# Document: mf-dicom-series [version 2]
#
asset.doc.type.update :create yes :type mf-dicom-series \
  :label "mf-dicom-series" \
  :description "DICOM Series" \
  :definition < \
    :element -name "uid" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Global unique identifier. DICOM element (0020,000E)." \
    > \
    :element -name "id" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Identification number. Derived from DICOM element (0020,0011)." \
    > \
    :element -name "description" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Description, if any. Derived from DICOM element (0008,103E), the series description, or if no description, synthesized from the series number - DICOM element (0020,0011)." \
    > \
    :element -name "sdate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Date and time the series was started. Derived from DICOM elements (0008,0021) and (0008,0031)." \
    > \
    :element -name "adate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Date and time the series was acquired. Derived from DICOM elements (0008,0022) and (0008,0032)." \
    > \
    :element -name "imin" -type "integer" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Minimum instance number in series. Derived from DICOM element (0020,0013)." \
      :restriction -base "integer" \
      < \
        :minimum "0" \
      > \
    > \
    :element -name "imax" -type "integer" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Maximum instance number in series. Derived from DICOM element (0020,0013)." \
      :restriction -base "integer" \
      < \
        :minimum "0" \
      > \
    > \
    :element -name "size" -type "integer" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Number of instances (e.g. images) in the series." \
      :restriction -base "integer" \
      < \
        :minimum "0" \
      > \
    > \
    :element -name "modality" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Type of series (e.g. MR - Magnetic Resonance). Derived from DICOM element (0008,0060)." \
    > \
    :element -name "protocol" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "User defined description of conditions under which the series was performed. Derived from DICOM element (0018,1030)." \
    > \
    :element -name "image" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Information about the image." \
      :element -name "position" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "The x, y, and z coordinates of the upper left hand corner (center of the first voxel transmitted) of the image, in mm. Derived from DICOM element (0020,0032)." \
        :element -name "x" -type "double" -index "true" -max-occurs "1" \
        < \
          :description "The X position, in mm" \
        > \
        :element -name "y" -type "double" -index "true" -max-occurs "1" \
        < \
          :description "The Y position, in mm" \
        > \
        :element -name "z" -type "double" -index "true" -max-occurs "1" \
        < \
          :description "The Z position, in mm" \
        > \
      > \
      :element -name "orientation" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "The direction cosines of the first row and the first column with respect to the patient. Derived from DICOM element (0020,0037)." \
        :element -name "value" -type "double" -index "true" -min-occurs "6" -max-occurs "6" \
        < \
          :description "One of the direction cosines." \
        > \
      > \
    > \
   >


