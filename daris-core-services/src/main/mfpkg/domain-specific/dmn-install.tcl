##############################################################################
# (Examplar) Domain-specific Metadata Definition                             #
#                                                                            #
# NOTE: this script can only be executed after all the daris docs, roles,    #
#       permissions are set.                                                 #
##############################################################################

set DMN_DIR             "domain-specific"
set DMN_DICT_NS         "dmn.daris"
set DMN_DOC_NS          "dmn.daris"
set DMN_METHOD_CID_ROOT "pssd.method.dmn"

#============================================================================#
# dictionaries                                                               #
#============================================================================#

# create dictionary namespace
dictionary.namespace.create :namespace ${DMN_DICT_NS} :ifexists ignore \
    :description "Namespace for DaRIS domain-specific (exemplar) dictionaries"

# grant access to dictionary namespace. Other users have read access by default.
actor.grant :name daris:pssd.administrator :type role \
    :perm < :access ADMINISTER :resource -type dictionary:namespace ${DMN_DICT_NS} >

# create dictionaries
source ${DMN_DIR}/dmn-dictionaries.tcl
create_domain_dicts ${DMN_DICT_NS}

#============================================================================#
# document types                                                             #
#============================================================================#

# create document namespace
if { [xvalue exists [asset.doc.namespace.exists :namespace ${DMN_DOC_NS}]] == "false" } {
    asset.doc.namespace.create :namespace ${DMN_DOC_NS} \
        :description "Namespace for DaRIS domain-specific (exemplar) document types"
}

# grant access to document namespace
actor.grant :name  daris:pssd.model.doc.user :type role \
    :perm < :resource -type document:namespace ${DMN_DOC_NS} :access ACCESS >
actor.grant :name  daris:pssd.administrator :type role \
    :perm < :resource -type document:namespace ${DMN_DOC_NS} :access ADMINISTER >

# create document types
source ${DMN_DIR}/dmn-doc-types.tcl
create_domain_doc_types ${DMN_DOC_NS}

# grant access to the domain doc types
grant_role_access_to_domain_doc_types daris:pssd.model.doc.user ${DMN_DOC_NS} { ACCESS PUBLISH }


#============================================================================#
# study types                                                                #
#============================================================================#

# Create some Study types. These can all be destroyed by the service: 
#        om.pssd.study.type.destroy.all
# and you can replace in your own package.
# (These are actually delivered as dictionary entries but the dictionary is 
# the framework dictionary pssd.study.types).
source ${DMN_DIR}/dmn-study-types.tcl
create_domain_study_types

#============================================================================#
# methods                                                                    #
#============================================================================#

# Create a sandbox cietbale id root for these Methods (so we don't pollute
# the standard cid space named "pssd.method"
citeable.named.id.create :name ${DMN_METHOD_CID_ROOT}

# Create a very generic Method as an exemplar 
source ${DMN_DIR}/dmn-method-generic.tcl
create_domain_method_generic ${DMN_METHOD_CID_ROOT} ${DMN_DOC_NS} 1 1

# Create a generic human Method as an exemplar 
source ${DMN_DIR}/dmn-method-human-unspecified.tcl
create_domain_method_human_unspecified ${DMN_METHOD_CID_ROOT} ${DMN_DOC_NS} 1 1
   
# Create an animal multi-mode imaging Method
source ${DMN_DIR}/dmn-method-animal-multimode.tcl
create_domain_method_animal_multimode ${DMN_METHOD_CID_ROOT} ${DMN_DOC_NS} 1 1
