# ============================================================================
# daris:dicom-dataset
# ============================================================================
asset.doc.type.update :create yes :type daris:dicom-dataset \
    :label dicom-dataset \
    :description "The metadata for a DICOM dataset." \
    :definition < \
        :element -name object -type document -index true -min-occurs 1 -max-occurs infinity < \
            :description "The DICOM object in the series." \
            :attribute -name idx -type integer -index true -min-occurs 1 < :description "The index of the object in the series. Starts from zero." > \
            :element -name de -type document -index true -min-occurs 0 -max-occurs infinity < \
                :description "The DICOM element." \
                :attribute -name tag -type string -index true -min-occurs 1 < :description "The 8 hex-digits DICOM tag." > \
                :attribute -name type -type enumeration -index true -min-occurs 1 < \
                    :description "The value representation." \
                    :restriction -base enumeration < :value AE :value AS :value AT :value CS :value DA \
                                                     :value DS :value DT :value FL :value FD :value IS \
                                                     :value LO :value LT :value OB :value OF :value OW \
                                                     :value PN :value SH :value SL :value SQ :value SS \
                                                     :value ST :value TM :value UI :value UL :value UN \
                                                     :value US :value UT :case-sensitive false > > \
                :element -name defn -type string -index true -min-occurs 0 -max-occurs 1 < :description "The name of the element(tag)." > \
                :element -name value -type string -index true -min-occurs 0 -max-occurs infinity < :description "The value of the element." > > > >

# ============================================================================
# daris:dicom-series
# ============================================================================
asset.doc.type.update :create yes :type daris:dicom-series \
    :label dicom-series \
    :description "The metadata for a DICOM series." \
    :definition < \
        :element -name acquisition-type -type enumeration -index true -min-occurs 0 -max-occurs 1 < \
            :description "MR acquisition type. Normally retrieved from DICOM element (0018,0023). Can be manually set if the DICOM element does not exist." \
            :restriction -base enumeration < :value 3D : value 2D > > \
        :element -name plane -type enumeration -index true -min-occurs 0 -max-occurs 1 < \
            :description "The plane of section. It is usually calculated based on the values of image orientation, DICOM element (0020,0037). Applies only to 2D series." \
            :restriction -base enumeration < :value sagittal :value axial :value coronal > > \
        :element -name label -type string -index true -min-occurs 0 -max-occurs infinity >
