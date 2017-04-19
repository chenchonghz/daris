#=====================================
# Type: daris:dicom-application-entity
#======================================
asset.doc.type.update :type daris:dicom-application-entity \
	:create true \
	:label "DICOM Application Entity" \
    :description "Document type for DICOM application entity." \
    :definition < \
    	:element -name title -type string  -min-occurs 1 -max-occurs 1 -index true < :description "The AE title." > \
    	:element -name host  -type string  -min-occurs 1 -max-occurs 1 -index true < :description "Host name or IP address." > \
	    :element -name port  -type integer -min-occurs 1 -max-occurs 1 -index true -default 104 -min 0 -max 65535 < :description "Port number." >  \
	    :element -name ssl   -type boolean -min-occurs 0 -max-occurs 1 -index true -default false < :description "secured with TLS/SSL?" > \
	    :element -name description -type string -min-occurs 0 -max-occurs 1 -index true < :description "Description about the AE." > \
	>



#=====================================
# Type: pssd-dicom-server-registry
# One document per Application Entity
#======================================
   
asset.doc.type.update \
      :create true :type daris:pssd-dicom-server-registry \
      :label "DICOM Application Entity server registry" \
      :description "Document type for registering DICOM Application Entities in the DICOM AE registry." \
      :definition < \
	  :element -name ae -min-occurs 1 -max-occurs 1 -type document -index true  < \
               :attribute -name name -type string -min-occurs 0 < :description "Convenience name" > \
	       :attribute -name type -type enumeration -enumerated-values "local,remote" -min-occurs 0 < :description "The type of this AE; local means this server" > \
	       :attribute -name access -type enumeration -enumerated-values "public,private" -min-occurs 0 < :description "The accessibility of this AE; 'public'  means acessible to all users, 'private' means accessible to the calling user only." > \
	       :element -name host -type string -min-occurs 1 -max-occurs 1 -index true < :description "Host name or IP address " > \
	       :element -name port -type integer -min-occurs 1 -max-occurs 1 -index true < :description "Port number" >  \
	       :element -name aet -type string -min-occurs 1 -max-occurs 1 -index true < :description "The AETitle of the AE" > \
	   > \
      >
   

  
# =================================================================
# Type: pssd-dicom-ingest
# =================================================================
#
 asset.doc.type.update :create yes :type daris:pssd-dicom-ingest \
  :label "pssd-dicom-ingest" \
  :description "Utilised by the DICOM server to control some aspects of its behaviour. Currently 1) establishes whether to find a Subject by name if finding by CID fails.  Must be located on a Project object." \
  :definition < \
    :element -name "subject" -type "document" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Controls some behaviours related to the DICOM server and PSSD Subject objects." \
      :element -name "find" -type "boolean" -max-occurs "1" \
      < \
        :description "When true, look for the Subject by name. The name is extracted from DICOM element (0010, 0010) and compared with the elements given by xpaths in the name element." \
      > \
      :element -name "name" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Specifies the xpaths (<doc namespace>:<doc type>/<xpath>).  Only specify the naming components that you want to match with (e.g.'first' and 'last' only )." \
        :element -name "prefix" -type "string" -min-occurs "0" -max-occurs "1" \
        < \
          :description "xpath for the name prefix" \
        > \
        :element -name "first" -type "string" -min-occurs "0" -max-occurs "1" \
        < \
          :description "xpath for the first name" \
        > \
        :element -name "middle" -type "string" -index "true" -min-occurs "0" \
        < \
          :description "xpath for the middle name(s)" \
        > \
        :element -name "last" -type "string" -min-occurs "0" -max-occurs "1" \
        < \
          :description "xpath for the last name" \
        > \
        :element -name "suffix" -type "string" -min-occurs "0" -max-occurs "1" \
        < \
          :description "xpath for a suffix to the name" \
        > \
      > \
      :element -name "aet" -type "string" -min-occurs "0" -max-occurs "infinity" \
      < \
        :description "AET of a calling DICOM client that is allowed to find Subjects by name in this Project" \
      > \
    > \
    :element -name "project" -type "document" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Specifies some Project-based DICOM upload behaviours" \
      :element -name "anonymize" -type "boolean" -min-occurs "0" -max-occurs "1" \
      < \
        :description "If true, indicates that the DICOM data for this project should be anonymized. Intended when the DICOM server has NOT been configured for direct anonymization (applies to all projects); use in conjuuction with om.pssd.dicom.anonymize" \
      > \
       :element -name "encrypt-patient" -type "boolean" -min-occurs "0" -max-occurs "1" \
      < \
        :description "If set, over-rides the DICOM server setting regarding whether it writes to mf-dicom-patient or mf-dicom-patient-encrypted (control nig.dicom.write.mf-dicom-patient)." \
      > \
     :element -name "find" -type "boolean" -min-occurs "0" -max-occurs "1" \
      < \
        :description "When true, and CID is extracted, look for the Project by configured name. The name is extracted from the specified DICOM element and compared with the given name." \
      > \
      :element -name "dicom" -type "string" -min-occurs "0" \
      < \
        :description "The value of the specified DICOM element." \
        :attribute -name "type" -type "enumeration" \
        < \
          :description "Which DICOM element to look in" \
          :restriction -base "enumeration" \
          < \
            :value "protocol_name" \
            :value "study_description" \
          > \
        > \
      > \
    > \
   >