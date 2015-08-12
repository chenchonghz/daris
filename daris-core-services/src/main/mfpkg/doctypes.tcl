# ======================================
# Type: pssd-object
# ======================================

asset.doc.type.update :create true :type daris:pssd-object \
	:description "Identifying information for all types of PSSD objects." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -index true < \
			:description "The PSSD type of the object." \
			:restriction -base enumeration < \
				:value "project" \
				:value "subject" \
				:value "method" \
				:value "ex-method" \
				:value "study" \
				:value "dataset" \
				:value "data-object" \
				:value "r-subject" \
			> \
		> \
		:element -name name -type string -min-occurs 0 -max-occurs 1 < \
			:description "Arbitrary name for the object." \
		> \
		:element -name description -type string -min-occurs 0 -max-occurs 1 < \
			:description "Arbitrary description, if any, for the object." \
		> \
	>


# =====================================
# Type: pssd-project
# ======================================

asset.doc.type.update :create true :type daris:pssd-project \
    :description "Metadata for a PSSD project." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name method -type document -min-occurs 0 < \
            :element -name id -type citeable-id \
		    :element -name notes -type string -min-occurs 0 \
		> \
 	    :element -name data-use -type enumeration -index true -min-occurs 1 -max-occurs 1 < \
            :description "Specifies the type of consent for the use of data: 1) 'specific' means use the data only for the original specific intent, 2) 'extended' means use the data for related projects and 3) 'unspecified' means use the data for any research" \
   	    	:restriction -base enumeration < \
	    		:value "specific" \
	    		:value "extended" \
	    		:value "unspecified" \
	    	> \
         > \
         :element -name fill-in-cid -type boolean -default false -index true -min-occurs 0 -max-occurs 1 < \
             :description "Controls whether or not reuse (fill in) the citeable id when creating subjects within this project." \
         > \
    >


# =====================================
# Type: pssd-subject
# ======================================

asset.doc.type.update :create true :type daris:pssd-subject \
    :description "Metadata for a PSSD subject." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name virtual -type boolean -min-occurs 0 -max-occurs 1 < \
            :description "Set to true, if the subject is a virtual subject." \
        > \
        :element -name method -type citeable-id < \
            :description "The method of research being used for this subject." \
        > \
        :element -name r-subject -type citeable-id -min-occurs 0 -max-occurs 1 < \
            :description "The R-Subject, if any, that this subject represents." \
        > \
        :element -name data-use -type enumeration -min-occurs 0 -max-occurs 1 < \
        	:description "Specify how the data can be (re)used for this subject" \
	    	:restriction -base enumeration < \
	    		:value "specific" \
	    		:value "extended" \
	    		:value "unspecified" \
	    	> \
	    > \
    >


# =====================================
# Type: pssd-ex-method
# ======================================

asset.doc.type.update :create true :type daris:pssd-ex-method \
    :description "Metadata for a PSSD method execution." \
    :tag pssd \
    :generated-by application \
    :definition < \
	:element -name method -type document -max-occurs 1 < \
        	:element -name id -type citeable-id -max-occurs 1 < \
	            :description "The top level method (identifier) being executed." \
        	> \
        	:element -name name -type string -max-occurs 1 < \
	            :description "The name of the sourced method." \
        	> \
        	:element -name description -type string -max-occurs 1 < \
	            :description "A description, if any, of the sourced methodd." \
        	> \
        	:element -name author -type citeable-id -min-occurs 0 < \
	            :description "The author of the sourced method." \
        	> \
	> \
        :element -type enumeration -name state -default incomplete -min-occurs 0  -max-occurs 1 < \
            :description "Overall state of execution." \
            :restriction -base enumeration < \
                :value "incomplete" \
                :value "waiting" \
                :value "complete" \
                :value "abandoned" \
            > \
	> \
	:element -name notes -type string -min-occurs 0 \
        :element -name step -type document -min-occurs 0 < \
            :attribute -name path -type citeable-id \
            :element -type enumeration -name state -default incomplete -min-occurs 0  -max-occurs 1 < \
                :description "State of execution." \
                :restriction -base enumeration < \
                    :value "incomplete" \
                    :value "waiting" \
                    :value "complete" \
                    :value "abandoned" \
                > \
	    > \
            :element -name notes -type string -min-occurs 0 -max-occurs 1 \
        > \
    >


# ======================================
# Type: pssd-study
# ======================================

asset.doc.type.update :create true :type daris:pssd-study \
	:description "The prescribed metadata for a PSSD Study." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -max-occurs 1 < \
			:description "The general classification/type of study." \
			:restriction -base enumeration < \
				:dictionary daris:pssd.study.types \
			> \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this study." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this study." \
			> \
		> \
	    :element -name "processed" -type "boolean" -min-occurs "0" -max-occurs "1" < \
           :description "Set to true, if the Study is a container for processed data only." \
        > \
	>


# ======================================
# Type: pssd-dataset
# ======================================

asset.doc.type.update :create true :type daris:pssd-dataset \
	:description "The prescribed metadata for a PSSD Data Set." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -max-occurs 1 < \
			:description "There are two classes of data sets." \
			:restriction -base enumeration < \
				:value "primary" \
				:value "derivation" \
			> \
		> \
	>


# ======================================
# Type: pssd-transform
# ======================================

asset.doc.type.update :create true :type daris:pssd-transform \
	:description "A transformation applied to the data set or data object. Can be used for acquisitions or derivations." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name tuid -type long -min-occurs 0 -max-occurs 1 < \
	        :description "The unique id of the transform, if it is through the daris-transform(Transform Framework)." \
	    > \
		:element -name mid -type citeable-id -min-occurs 0 -max-occurs 1 < \
                :description "The identity of a common transformation method/protocol." \
        > \
		:element -name software -type document -min-occurs 0 -max-occurs infinity < \
            :element -name name -type string -index true -min-occurs 1 -max-occurs 1 \
            :element -name version -type string -index true -min-occurs 0 -max-occurs 1 \
            :element -name command -type document -min-occurs 0 -max-occurs infinity < \
                :element -name name -type string -min-occurs 1 -max-occurs 1 \
                :element -name argument -type document -min-occurs 0 -max-occurs infinity < \
                    :element -name name -type string -index true -min-occurs 1 -max-occurs 1 \
                    :element -name value -type string -index true -min-occurs 0 -max-occurs 1 \
                > \
            > \
        > \
		:element -name notes -type string -min-occurs 0 -max-occurs 1 < \
			:description "Description, if any, of the transformation." \
		> \
	>


# =====================================
# Type: pssd-acquisition
# ======================================

asset.doc.type.update :create true :type daris:pssd-acquisition \
	:description "Acquisition from a subject." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name subject -type citeable-id -max-occurs 1 < \
			:description "The identity of the subject from which this data set was acquired." \
                    :attribute -name state -type integer < \
			:description "The identity of the state the subject was in at the time of acquisition." \
			:restriction -base integer < \
				:minimum 1 \
			> \
		    > \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this data set." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this data set." \
			> \
		> \
	>


# =====================================
# Type: pssd-derivation
# ======================================

asset.doc.type.update :create true :type daris:pssd-derivation \
	:description "Derivation from one or more other data sets." \
	:tag pssd \
        :generated-by application \
	:definition < \
	    :element -name processed -type boolean -min-occurs 0 -max-occurs 1 < \
	        :description "Set to true, if the data is processed data rather than raw data." \
	    > \
		:element -name input -type citeable-id -min-occurs 0 -max-occurs infinity < \
			:description "The identity of a Data Set from which this set was derived." \
			:attribute -name vid -type string < \
				:description "The version/state of the data set." \
			> \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this data set." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this data set." \
			> \
		> \
		:element -name anonymized -type boolean -min-occurs 0 -max-occurs 1 < \
          :description "Indicates this dataset (the actual content) has been anonymized in some way. E.g. for a DICOM DataSet the patient name will have been set to null." \
      > \
	>

# =====================================
# Type: pssd-method
# ======================================

asset.doc.type.update :create true :type daris:pssd-method \
    :description "Research method. Defines the metadata to be presented at any step." \
    :tag pssd \
    :generated-by application \
    :definition < \
    	:element -name version -type string -min-occurs 0 -max-occurs 1 < :description "A version string identifying the Method object structure. If absent, implies version 1.0" > \
        :element -name author -type citeable-id -min-occurs 0 \
        :element -name step -type document -min-occurs 0 < \
            :attribute -name id -type integer \
            :element -name name -type string -min-occurs 1 -max-occurs 1 \
            :element -name description -type string -min-occurs 0 -max-occurs 1 \
            :element -name subject -type document -min-occurs 0 -max-occurs 1 < \
                :attribute -name part -type enumeration -enumerated-values "p,r" -min-occurs 0 -default p \
                :element -name metadata -type document -min-occurs 0 -max-occurs infinity < \
                    :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                        :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                    > \
                    :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
                > \
	    > \
            :element -name study -type document -min-occurs 0 -max-occurs 1 < \
               :element -name type -type enumeration < \
                    :restriction -base enumeration < \
                        :dictionary daris:pssd.study.types \
                    > \
               > \
	       :element -name "dicom" -type "document" -index "true" -min-occurs "0" -max-occurs "1" < \
	          :description "This element describes restrictions on the kinds of DICOM data that are compatible with this Study type." \
	          :element -name "modality" -type "enumeration" -index "true" -min-occurs "0" < \
		    :description "DICOM modality string code.  This code is found in DICOM element (0008,0060)." \
		    :restriction -base "enumeration" < \
		      :dictionary "daris:pssd.dicom.modality" \
		     > \
	           > \
	        > \
               :element -name metadata -type document -min-occurs 0 -max-occurs infinity < \
                   :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                       :attribute -name group -type string -min-occurs 0 \
                       :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                   > \
                   :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
               > \
            > \
            :element -name method -type document -min-occurs 0 -max-occurs 1 < \
            	:element -name id -type citeable-id -min-occurs 1 -max-occurs 1 \
            	:element -name name -type string -min-occurs 0 -max-occurs 1 \
            	:element -name description -type string -min-occurs 0 -max-occurs 1 \
	        :reference -type element -name step -min-occurs 0 < :value ../.. > \
	    > \
            :element -name branch -type document -min-occurs 0 -max-occurs 1 < \
                :attribute -name type -type enumeration < \
                    :restriction -base enumeration < \
                        :value or \
                        :value and \
                    > \
                > \
                :element -name method -type document -max-occurs infinity < \
            	    :element -name id -type citeable-id -min-occurs 1 -max-occurs 1 \
             	    :element -name name -type string -min-occurs 0 -max-occurs 1 \
              	    :element -name description -type string -min-occurs 0 -max-occurs 1 \
	            :reference -type element -name step -min-occurs 0 < :value ../../.. > \
                > \
            > \
            :element -name transform -type document -min-occurs 0 -max-occurs 1 < \
                :description "Transform need to be performed in the step." \
                :element -name definition -type long -min-occurs 1 -max-occurs 1 < \
                    :description "The unique id of the transform definition." \
                    :attribute -name version -type integer -min-occurs 1 < \
                        :description "The version of the transform definition." \
                    > \
                > \
                :element -name iterator -type document -min-occurs 0 -max-occurs 1 < \
                    :element -name scope -type enumeration -min-occurs 1 -max-occurs 1 < \
                        :restriction -base enumeration < \
                            :value ex-method :value subject :value project \
                        > \
                    > \
                    :element -name query -type string -min-occurs 1 -max-occurs 1 \
                    :element -name type -type enumeration -min-occurs 1 -max-occurs 1 < \
                        :restriction -base enumeration < \
                            :value citeable-id :value asset-id \
                        > \
                    > \
                    :element -name parameter -type string -min-occurs 1 -max-occurs 1 \
                > \
                :element -name parameter -type string -min-occurs 0 -max-occurs infinity < \
                    :description "The parameter of the transform." \
                    :attribute -name name -type string -min-occurs 1 < \
                        :description "The name of the parameter. The parameter must be defined in the transform definition." \
                    > \
                > \
            > \
        > \
     >


# =====================================
# Type: pssd-method-subject
# ======================================

asset.doc.type.update :create true :type daris:pssd-method-subject \
    :description "Metadata for the subject of a method. Applies for subject specific methods." \
    :tag pssd \
    :generated-by application \
    :definition < \
    	:element -name human -type boolean -min-occurs 0 -max-occurs 1 < \
            :description "Indicates if the Method is intended for use with Human subjects. Set to true if for human, false if not, and leave unset if unknown." \
        > \
        :element -name public -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name private -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
     >


# =====================================
# Type: pssd-method-rsubject
# ======================================

asset.doc.type.update :create true :type daris:pssd-method-rsubject \
    :description "Metadata for the r-subject of a method. Applies for subject specific methods." \
    :tag pssd \
    :generated-by application \
    :definition < \
      	:element -name human -type boolean -min-occurs 0 -max-occurs 1 < \
            :description "Indicates if the Method is intended for use with Human subjects." \
        > \
        :element -name identity -type document -min-occurs 1 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                 :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                     :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                 > \
                 :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name public -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name private -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
     >

# =====================================
# Type: pssd-state
# ======================================

#asset.doc.type.update :create true :type daris:pssd-state \
#	:description "The state of a subject." \
#	:tag pssd \
#        :generated-by application \
#	:definition < \
#		:element -name state -type document -max-occurs infinity < \
#			:description "A unique state." \
#			:attribute -name id -type integer < \
#				:description "The identity of the state." \
#                                :restriction -base integer < \
#                                  :minimum 1 \
#                                > \
# 			> \
#			:element -name ex-method -type citeable-id -min-occurs 0 -max-occurs 1 < \
#                            :description "The executing method instance that caused this state change." \
#                        > \
#                        :element -name method -type citeable-id -min-occurs 0 -max-occurs 1 -recurse true < \
#                            :description "The method and step that resulted in this state. May contain other sub-methods." \
#                            :attribute -name step -type integer \
#                        > \
#			:element -name workflow -type document -min-occurs 0 -max-occurs 1 < \
#	  			:element -name status -type enumeration < \
#					:description "Status of the state." \
#					:restriction -base enumeration < \
#                        	     		:value "incomplete" \
#                        	     		:value "waiting" \
#                             			:value "complete" \
#                             			:value "abandoned" \
#					> \
#				> \
#				:element -name notes -type string -min-occurs 0 \
#			> \
#			:element -name meta -type document -ignore-descendants true -min-occurs 0 < \
#				:description "Arbitrary metadata for the state" \
#			> \
#		> \
#	>

#=====================================
# Type: pssd-role-member-registry
# One document per item
#======================================

asset.doc.type.update \
   :create true :type daris:pssd-role-member-registry \
   :label "Role member registry" \
   :description "Document type for registering roles as project role-members" \
   :definition < \
       :element -name role -min-occurs 1 -max-occurs 1 -type string -index true  < \
             :description "A role that can be supplied as a role-member when creating PSSD projects" \
             :attribute -name id  -type string  -min-occurs 1  \
        > \
   >

   
# =================================================================
# Type: pssd-notification
#   
# Instead of "data-upload" and other category elements, we could use
# a category attribute on each member. In that way, only the
# category enum would grow with new categories. Perhaps less
# obvious for users.
# ==================================================================

asset.doc.type.update :create true :type daris:pssd-notification \
    :description "Metadata to describe how notifications can be made to project members." \
    :tag pssd \
    :generated-by user \
    :definition < \
	:element -name data-upload -index true -type document -min-occurs 0 -max-occurs 1 < \
 	    :description "Indicates who will receive an email when data are uploaded." \
            :element -name project-role -type enumeration  -min-occurs 0 -max-occurs infinity < \
                :description "Specifies users who hold a specific project role to receive an email." \
                :attribute -name explicit -type boolean -min-occurs 1 < \
                	:description "If true, users who explicitly hold the given role receive an email. If false, users who implicitly hold the role (e.g. an admin is also a member) receive email." \
           	 > \
           	 :restriction -base enumeration < \
                    :value "project-administrator" \
                    :value "subject-administrator" \
                    :value "member" \
                    :value "guest" \
    		 > \
            > \
            :element -name member -index true -type document -min-occurs 0 -max-occurs infinity < \
            	:description "Specifies a user member to receive email." \
	    	:element -name authority -type string -min-occurs 0 -max-occurs 1 < \
		    :attribute -name protocol -type string -min-occurs 0  \
		 > \
     	        :element -name domain -type string -min-occurs 1 -max-occurs 1 \
       	        :element -name user -type string -min-occurs 1 -max-occurs 1 \
     	    > \
            :element -name role-member -index true -type string -min-occurs 0 -max-occurs infinity < \
                :description "Specifies a project role member to receive email." \
	    > \
	    :element -name email -index true -type string -min-occurs 0 -max-occurs infinity < \
	        :description "Specifies a direct email address, whether project team member or not, to receive a message." \
	    > \
        > \
 	:element -name dicom-upload -index true -type document -min-occurs 0 -max-occurs 1 < \
 	    :description "Indicates who will receive an email when DICOM data are uploaded and certain validation checks fail." \
            :element -name project-role -type enumeration  -min-occurs 0 -max-occurs infinity < \
                :description "Specifies users who hold a specific project role to receive an email." \
                :attribute -name explicit -type boolean -min-occurs 1 < \
                	:description "If true, users who explicitly hold the given role receive an email. If false, users who implicitly hold the role (e.g. an admin is also a member) receive email." \
           	 > \
           	 :restriction -base enumeration < \
                    :value "project-administrator" \
                    :value "subject-administrator" \
                    :value "member" \
                    :value "guest" \
    		 > \
            > \
            :element -name member -index true -type document -min-occurs 0 -max-occurs infinity < \
            	:description "Specifies a user member to receive email." \
	    	:element -name authority -type string -min-occurs 0 -max-occurs 1 < \
		    :attribute -name protocol -type string -min-occurs 0  \
		 > \
     	        :element -name domain -type string -min-occurs 1 -max-occurs 1 \
       	        :element -name user -type string -min-occurs 1 -max-occurs 1 \
     	    > \
            :element -name role-member -index true -type string -min-occurs 0 -max-occurs infinity < \
                :description "Specifies a project role member to receive email." \
	    > \
	    :element -name email -index true -type string -min-occurs 0 -max-occurs infinity < \
	        :description "Specifies a direct email address, whether project team member or not, to receive a message." \
	    > \
        > \
    >
    
    
# =================================================================
# Type: pssd-repository-description
#   
# This meta-data is for a static singleton object that describes
# attributes of the repository in the research context.
# ==================================================================

asset.doc.type.update :create yes :type daris:pssd-repository-description \
  :label "pssd-repository-description" \
  :description "Metadata to describe the repository in the research context." \
  :definition < \
    :element -name "name" -type "string" -index "true" -max-occurs "1" \
    < \
      :description "The name of the repository" \
      :attribute -name "acronym" -type "string" -min-occurs "0" \
      < \
        :description "Acronym for the repository name." \
      > \
    > \
    :element -name "custodian" -type "document" -index "true" -max-occurs "1" \
    < \
      :description "The person responsible for the management of the repository." \
      :element -name "email" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Specifies the custodian's email address." \
      > \
      :element -name "prefix" -type "string" -min-occurs "0" -max-occurs "1" \
      < \
        :restriction -base "string" \
        < \
          :max-length "20" \
        > \
      > \
      :element -name "first" -type "string" -max-occurs "1" \
      < \
        :restriction -base "string" \
        < \
          :max-length "40" \
        > \
      > \
      :element -name "middle" -type "string" -min-occurs "0" \
      < \
        :description "If there are several 'middle' names then put them in this field" \
        :restriction -base "string" \
        < \
          :max-length "100" \
        > \
      > \
      :element -name "last" -type "string" -max-occurs "1" \
      < \
        :restriction -base "string" \
        < \
          :max-length "40" \
        > \
      > \
      :element -name "address" -type "document" -max-occurs "1" \
      < \
        :element -name "department" -type "string" -max-occurs "1" \
        :element -name "institution" -type "string" -min-occurs "0" -max-occurs "1" \
        :element -name "physical-address" -type "string" -min-occurs "0" \
      > \
      :element -name "NLA-ID" -case-sensitive true -type "string" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Globally unique National Library of Australia Identifier" \
      > \
    > \
    :element -name "location" -type "document" -index "true" -max-occurs "1" \
    < \
      :description "The physical location of the repository." \
      :element -name "institution" -type "string" -index "true" -max-occurs "1" \
      :element -name "department" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      :element -name "building" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
      :element -name "precinct" -type "string" -index "true" -min-occurs "0" -max-occurs "1" \
    > \
    :element -name "rights" -type "document" -index "true" -max-occurs "1" \
    < \
      :description "A description of the rights process to gain access to collections in the repository." \
      :element -name "description" -type "string" -index "true" -max-occurs "1" \
    > \
    :element -name "data-holdings" -type "document" -index "true" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Describes broadly the data holdings in the repository." \
      :element -name "description" -type "string" -index "true" -max-occurs "1" \
      :element -name "start-date" -type "date" -index "true" -min-occurs "0" -max-occurs "1" \
      < \
        :description "The date on which the repository was activated and started managing data." \
      > \
    > \
    :element -name "originating-source" -type url -index true -min-occurs 0 -max-occurs 1 \
    < \
       :description "Describes the originating server for any meta-data harvested by the service om.pssd.project.metadata.harvest" \
    > \
   >

	
# =================================================================
# Type: pssd-project-owner
#
# Describes the owner of a project, independent of the people 
# that may be managing it in the reposiutory
#   
# ==================================================================
		
	
		asset.doc.type.update \
			:create true :type daris:pssd-project-owner \
			:label "Project Owner" \
			:description "Document type to specify who owns a project (e.g. Chief Investigator), independent of the users who happen to be managing it in the repository." \
			:definition < \
			  :element -name project-owner -min-occurs 1 -max-occurs infinity  -type document -index true \
			   < \
			     :description "The project owner (e.g. Chief Investigator) who has carriage of ethical/funding processes." \
				:element -name prefix -type enumeration -dictionary daris:pssd.human.name.prefix -min-occurs 0 -max-occurs 1 -label "Prefix" \
			     :element -name first  -type string -min-occurs 1 -max-occurs 1  -label "First" \
			     :element -name middle -type string -min-occurs 0 -max-occurs 1  -label "Middle" < \
				:description "If there are several 'middle' names then put them in this field" \
			      > \
			     :element -name last   -type string -min-occurs 1 -max-occurs 1  -label "Last" \
			     :element -name suffix -type string -min-occurs 0 -max-occurs 1  -label "Suffix" \
			     :element -name email -type string -min-occurs 0 -max-occurs 1 -label email \
			     :element -name URL -type string -min-occurs 0 -label URL \
			     :element -name institution -type document -min-occurs 0 -max-occurs infinity -index true \
				 < \
				    :description "Name of the institution that the project owner is a member of." \
		  	    :element -name name -type enumeration -dictionary daris:pssd.research.organization \
				         -min-occurs 0 -max-occurs 1  \
			     	:element -name department -type string -min-occurs 0 -max-occurs 1 \
			     > \
			    :element -name NLA-ID -case-sensitive true -type string -min-occurs 0 -max-occurs 1 \
			       < :description "Globally unique National Library of Australia Identifier" > \
			  > \
		   >


# =================================================================
# Type: pssd-project-publications
#
# Describes a list of publications associated with a project
#   
# ==================================================================

asset.doc.type.update :create yes :type daris:pssd-publications \
  :label "pssd-publications" \
  :description "Publication details associated with this project." \
  :definition < \
    :element -name "publication" -type "document" -min-occurs "0" \
    < \
      :element -name "title" -type "string" -index "true" -max-occurs "1" \
      < \
        :description "The full title of the publication" \
      > \
      :element -name "identifier" -type "string" -index "true" -min-occurs "0" \
      < \
        :description "A digital identifier for this publication" \
        :attribute -name "type" -type "enumeration" \
        < \
          :description "The type of digital identifier" \
          :restriction -base "enumeration" \
          < \
            :dictionary "daris:pssd.publication.identifier.type" \
          > \
        > \
      > \
      :element -name "citation" -type "string" -min-occurs "0" -max-occurs "1" \
      < \
        :description "If a digital identifier is not available, specify a traditional journal citation for this publication." \
      > \
    > \
   >

			  
			  
# =================================================================
# Type: pssd-services
#
# Describes a list of related services
#   
# ==================================================================

asset.doc.type.update :create yes :type daris:pssd-related-services \
  :label "pssd-related-services" \
  :description "Describes services that are related to this project" \
  :definition < \
    :element -name "service" -type "document" -min-occurs "0" \
    < \
      :description "Describes a related service" \
      :element -name "relation-type" -type "enumeration" -index "true" -max-occurs "1" \
      < \
        :description "How is the service related to the project" \
        :restriction -base "enumeration" \
        < \
          :value "supports" \
          :value "isAvailableThrough" \
          :value "isProducedBy" \
          :value "isPresentedBy" \
          :value "isOperatedOnBy" \
          :value "hasValueAddedBy" \
        > \
      > \
      :element -name "identifier" -type "string" -index "true" -max-occurs "1" \
      < \
        :description "An identifier for the service" \
      > \
    > \
   >

		
# ======================================================================	
# Type: pssd-project-governance 
# ======================================================================	

asset.doc.type.update :create yes :type daris:pssd-project-governance \
  :label "pssd-project-governance" \
  :description "Describes project governance processes (e.g. funding, ethics)" \
  :definition < \
    :element -name "funding-id" -type "string" -min-occurs "0" \
    < \
      :description "Funding identifier.  For ARC and NHMRC can be of the form  http://purl.org/au-research/arc/<id> or http://purl.org/au-research/nhmrc/<id>." \
      :attribute -name "type" -type "enumeration" -index "true" \
      < \
        :description "The funding organization" \
        :restriction -base "enumeration" \
        < \
          :dictionary "daris:pssd.funding.organization" \
        > \
      > \
      :attribute -name "type-other" -type "string" -min-occurs "0" \
      < \
        :description "If the type is 'Other' then enter the funding organization details here." \
      > \
    > \
    :element -name "ethics-id" -type "string" -index "true" -min-occurs "0" \
    < \
      :description "Ethics identifier" \
      :attribute -name "type" -type "enumeration" \
      < \
        :description "The ethics organization" \
        :restriction -base "enumeration" \
        < \
          :dictionary "daris:pssd.ethics.organization" \
        > \
      > \
      :attribute -name "type-other" -type "string" -min-occurs "0" \
      < \
        :description "If the type is 'Other' then enter the ethics organization details here." \
      > \
    > \
    :element -name "commercial-in-confidence" -type boolean -index true -min-occurs 0 -max-occurs 1 \
    < \
       :description "Were the data acquired with a commerical in confidence agreement in place?" \
    > \
    :element -name "data-retention" -type "document" -index true -min-occurs "0" -max-occurs "1" \
    < \
      :description "Describes any data retention policy" \
      :attribute -name "from" -type "date" -min-occurs "0" \
      < \
        :description "Date from which retention times are relative to. If none, application dependent (e.g. use creation time, upload time, publication time)." \
        :restriction -base "date" \
        < \
          :time "false" \
        > \
      > \
      :element -name "retain-min" -type "float" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Minimum time that data must be held for" \
        :attribute -name "units" -type "enumeration" \
        < \
          :description "Units of retention time" \
          :restriction -base "enumeration" \
          < \
            :value "year" \
            :case-sensitive "true" \
          > \
        > \
      > \
      :element -name "retain-max" -type "float" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Maximum time that data can  be held for" \
        :attribute -name "units" -type "enumeration" \
        < \
          :description "Units of retention time" \
          :restriction -base "enumeration" \
          < \
            :value "year" \
            :case-sensitive "true" \
          > \
        > \
      > \
    > \
    :element -name "facility-id" -type "string" -min-occurs "0" \
    < \
      :description "An alternative project identifier supplied by some other entity" \
      :attribute -name "type" -type "string" -index "true" \
      < \
        :description "The entity supplying the identifier (be consistent)." \
      > \
    > \
   >


# ======================================================================	
# Type: pssd-project-research-category
# ======================================================================	

  
  asset.doc.type.update :create yes :type daris:pssd-project-research-category \
  :label "pssd-project-research-category" \
  :description "Describes attributes about the field of research for this project." \
  :definition < \
     :element -name "keyword-d" -type "enumeration" -index "true" -min-occurs "0" \
    < \
      :description "A keyword populated from the dictionary daris:pssd.research.keyword" \
      :restriction -base "enumeration" \
      < \
        :dictionary "daris:pssd.research.keyword" \
      > \
    > \
     :element -name "keyword" -type "string" -index "true" -min-occurs "0" \
    < \
      :description "A standard keyword for the research undertaken by this project" \
    > \
   :element -name "ANZSRC-11" -type "enumeration" -index "true" -min-occurs "0" \
    < \
      :description "Standard ANZSRC Field of Research (Medical and Health Sciences; 11) classification code." \
      :restriction -base "enumeration" \
      < \
        :dictionary "daris:pssd.ANZSRC.Division-11.field-of-research" \
      > \
    > \
   >
  

			  
			  
# =================================================================
# Type: pssd-shoppingcart-layout-pattern
# =================================================================

asset.doc.type.update :type daris:pssd-shoppingcart-layout-pattern \
    :create true \
    :tag pssd \
    :generated-by user \
    :description "Shopping-cart layout pattern." \
    :definition < \
        :element -name pattern -type string -min-occurs 1 -max-occurs 1 -index true < \
            :description "the layout pattern (string)." \
        > \
    >
    
    
# =================================================================
# Type: pssd-filename    
# Used to store the original names of files uploaded to mediaflux
# =================================================================
asset.doc.type.update :type daris:pssd-filename \
   :create true \
   :tag pssd \
   :generated-by user \
   :description "Used to track original filenames when uploaded to assets" \
   :definition < \
       :element -name original -type string -min-occurs 0 -max-occurs 1 -index true < \
          :description "The original name on the file system before it is uploaded." \
          :attribute -name private -type boolean -min-occurs 0 < \
                	:description "If true, the element is private and is only visible to users holding subject or project administrator roles for the given Project. If false, element is public and all members of the project can access." \
          > \
       > \
   >


# =================================================================
# Type: pssd-system-announcement
# =================================================================
asset.doc.type.update :type daris:pssd-system-announcement \
    :create true \
    :tag pssd \
    :generated-by user \
    :description "DaRIS system announcement." \
    :definition < \
        :element -name uid -type long -min-occurs 1 -max-occurs 1 -index true \
        :element -name title -type string -min-occurs 1 -max-occurs 1 -index true \
        :element -name text -type string -min-occurs 1 -max-occurs 1 \
        :element -name created -type date -min-occurs 1 -max-occurs 1 -index true \
        :element -name expiry -type date -min-occurs 0 -max-occurs 1 -index true \
    >


  
