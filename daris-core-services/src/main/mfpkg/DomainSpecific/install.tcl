# Create document and dictionary namespaces
dictionary.namespace.create :description "Namespace for DaRIS domain-specific (exemplar) dictionaries" :namespace daris-dmn :ifexists ignore
set exists [xvalue exists [asset.doc.namespace.exists :namespace "daris-dmn"]]
if { $exists == "false" } {
    asset.doc.namespace.create :description "Namespace for DaRIS domain-specific (exemplar) document types" :namespace daris-dmn
}
    
# Grant access to document namespace
actor.grant :name  daris:pssd.model.doc.user :type role :perm < :resource -type document:namespace daris-dmn :access ACCESS >
actor.grant :name  daris:pssd.administrator :type role :perm < :resource -type document:namespace daris-dmn :access ADMINISTER >


# Grant access to administer dictionary namespace. Other users have read access by default
actor.grant :name daris:pssd.administrator :type role :perm < :access ADMINISTER :resource -type dictionary:namespace daris-dmn >
  
# Create dictionaries  
source DomainSpecific/dictionaries.tcl
createDomainDicts daris-dmn

# Create document types
source DomainSpecific/doctypes.tcl
createDomainDocTypes daris-dmn
grantRoleReadWriteAccessDocTypes daris:pssd.model.doc.user \
    { "daris-dmn:pssd-animal-subject" "daris-dmn:pssd-dmn-human-identity" \
      "daris-dmn:pssd-human-subject" "daris-dmn:pssd-identity"  "daris-dmn:pssd-subject" }

# Create some Study types. These can all be destroyed by the service
# om.pssd.study.type.destroy.all and you can replace in your own
# package  (these are actually delivered as dictionary entries but the dictionary
# is the framework dictionary pssd.study.types).
source DomainSpecific/StudyTypes.tcl
create_DomainStudyTypes

# Create a sandbox cietbale ID root for these Methods (so we don't pollute
# the standard CID space named "pssd.method"
citeable.named.id.create :name "pssd.method.dmn"

# Create a very generic Method as an exemplar 
source DomainSpecific/method-generic.tcl
createMethod 1 1

# Create a generic human Method as an exemplar 
source DomainSpecific/method-human-unspecified.tcl
createMethod_human_unspecified 1 1
   
# Create an animal multi-mode imaging Method
source DomainSpecific/method-animal-multi-mode.tcl
createMethod_animal_multimode 1 1

