#=============================================================================
# This script creates  Document Types for ANDS meta-data harvesting
# The meta-data re not domain specific and so it is appropriate to be
# in the pssd package
#=============================================================================
#
# These meta-data should be placed on the Project object
proc create_harvest_doctypes {} {

asset.doc.type.update :create yes :type daris:pssd-project-harvest \
  :label "pssd-project-harvest" \
  :description "Document type to specify authorisation to harvest Project descriptions to Insitutional and ANDS registry." \
  :definition < \
    :element -name "allow-institutional" -type "boolean" -index "true" -max-occurs "1" \
    < \
      :description "Allow meta-data from this Project to be harvested to an Institutional registry." \
    > \
    :element -name "allow-ANDS" -type "boolean" -index "true" -max-occurs "1" \
    < \
      :description "Allow meta-data from this Project to be harvested to an ANDS registry." \
    > \
    :element -name "project-owner" -type "document" -index "true" -min-occurs 0 -max-occurs infinity \
    < \
      :description "The project owner (e.g. Chief Investigator) who has carriage of ethical/funding processes." \
      :element -name "prefix" -type "enumeration" -min-occurs "0" -max-occurs "1" \
      < \
        :restriction -base "enumeration" \
        < \
          :dictionary "daris:pssd.human.name.prefix" \
        > \
      > \
      :element -name "first" -type "string" -max-occurs "1" \
      :element -name "middle" -type "string" -min-occurs "0" -max-occurs "1" \
      < \
        :description "If there are several 'middle' names then put them in this field" \
      > \
      :element -name "last" -type "string" -max-occurs "1" \
      :element -name "suffix" -type "string" -min-occurs "0" -max-occurs "1" \
      :element -name "email" -type "string" -min-occurs "0" -max-occurs "1" \
      :element -name "URL" -type "string" -min-occurs "0" \
      :element -name "institution" -type "document" -index "true" -min-occurs "0" \
      < \
        :description "Name of the institution that the project owner is a member of." \
        :element -name "name" -type "enumeration" -min-occurs "0" -max-occurs "1" \
        < \
          :restriction -base "enumeration" \
          < \
            :dictionary "daris:pssd.research.organization" \
          > \
        > \
        :element -name "department" -type "string" -min-occurs "0" -max-occurs "1" \
      > \
      :element -name "NLA-ID" -type "string" -min-occurs "0" -max-occurs "1" \
      < \
        :description "Globally unique National Library of Australia Identifier" \
      > \
    > \
    :element -name "field-of-research" -type "enumeration" -min-occurs 0 -index "true" \
    < \
      :description "Standard ANZSRC Field of Research (Medical and Health Sciences) classification" \
      :restriction -base "enumeration" \
      < \
        :dictionary "daris:pssd.ANZSRC.Division-11.field-of-research" \
      > \
    > \
   >
}



#============================================================================#
proc destroy_harvest_doctypes { } {

	set doctypes { daris:pssd-project-harvest }

	foreach doctype $doctypes {
           destroyDocType $doctype "true"
	}

}

#============================================================================#

create_harvest_doctypes
