# Migrates
# document types, dictionaries and roles to 'daris' namespaces
#
# Process
# 1. Source this script with aterm and enter > migrate_namespaces
# 2. Install all the new packages
# 3. Run om.pssd.doctype.rename :templates true
# 
# In your own package, find all references to DaRIS document types, dictionaries and roles that have
# changed and migrate them in your code.  Then install the new package
#  - Document types : update their definitions
#  - Dictionaries : if you refer to these update these references in your code (and doc type definitions)
#  - Roles 
# You must also find and update
#  - Workflows
#  - Scripts
#  - Saved queries : are not handled. You must destroy (they are in children of namespace pssd-users) and re-create.
#
proc requireServerVersion { version } {
    set server_version [xvalue version [server.version]]
    set sv [string map { . "" } $server_version]
    set rv [string map { . "" } $version]
    if { $sv<$rv } {
        error "The server version (${server_version}) is less than the required version (${version})."
    }
}

proc migrateStaticRoles {} {

# essentials
   authorization.role.modify :name daris:basic-user :role basic-user
   authorization.role.modify :name daris:federation-user :role federation-user
   authorization.role.modify :name daris:essentials.administrator :role nig.essentials.administrator

# pssd
   authorization.role.modify :name daris:pssd.administrator :role pssd.administrator
   authorization.role.modify :name daris:pssd.dicom-ingest :role pssd.dicom-ingest
   authorization.role.modify :name daris:pssd.model.doc.user :role pssd.model.doc.user
   authorization.role.modify :name daris:pssd.model.power.user :role pssd.model.power.user
   authorization.role.modify :name daris:pssd.model.user :role pssd.model.user
   authorization.role.modify :name daris:pssd.object.admin :role pssd.object.admin
   authorization.role.modify :name daris:pssd.object.guest :role pssd.object.guest
   authorization.role.modify :name daris:pssd.r-subject.admin :role pssd.r-subject.admin
   authorization.role.modify :name daris:pssd.r-subject.guest :role pssd.r-subject.guest
   authorization.role.modify :name daris:pssd.project.create :role pssd.project.create
   authorization.role.modify :name daris:pssd.subject.create :role pssd.subject.create
}

proc modifyRole { newName oldName } {
   if { [xvalue exists [authorization.role.exists :role $oldName]] != "true" } {
      return
   }
   authorization.role.modify :name $newName :role $oldName
}

proc migrateProjectRoles { project } {
    set old_name "pssd.project.admin.${project}"
    set new_name "daris:pssd.project.admin.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.subject.admin.${project}"
    set new_name "daris:pssd.project.subject.admin.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.member.${project}"
    set new_name "daris:pssd.project.member.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.guest.${project}"
    set new_name "daris:pssd.project.guest.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.subject.use.specific.${project}"
    set new_name "daris:pssd.project.subject.use.specific.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.subject.use.extended.${project}"
    set new_name "daris:pssd.project.subject.use.extended.${project}"
    modifyRole $new_name $old_name
    #
    set old_name "pssd.project.subject.use.unspecified.${project}"
    set new_name "daris:pssd.project.subject.use.unspecified.${project}"
    modifyRole $new_name $old_name
}

proc migrateProjectsRoles {} {
   foreach project [xvalues cid [asset.query :where "model='om.pssd.project'" :action get-cid :size infinity]] {
        puts "   Project $project"
        migrateProjectRoles $project
    }
}

proc checkVersion { version } {
  set v2 [string trimleft [lindex [split $version . ] 1] 0]
  set refVer 22
  	if { $v2 < $refVer } {
		error "The PSSD version (${version}) is less than the required version (2.22)."
	}
}


proc revokePerm { type name perm } {

	# retrieve the resource type, name and access from the list
	set rsType     [lindex ${perm} 0]
	set rsName     [lindex ${perm} 1]
	set rsAccess   [lindex ${perm} 2]
	# grant the perm to the actor
	actor.revoke :type $type :name $name \
		:perm < :resource -type ${rsType} ${rsName} :access ${rsAccess} >

}

proc revokeRolePerm { role perm } {
	
	revokePerm role $role $perm
	
}

proc revokeRoleReadAccessDocType { role docType } {
    if { [xvalue exists [asset.doc.type.exists :type $docType]] == "true" } {
        revokeRolePerm $role [list document $docType ACCESS]
    }
}

proc revokeRoleWriteAccessDocType { role docType } {
    if { [xvalue exists [asset.doc.type.exists :type $docType]] == "true" } {
        revokeRolePerm $role [list document $docType PUBLISH]
    }
}
	
proc revokeRoleReadWriteAccessDocType { role docType } {
	
	revokeRoleReadAccessDocType $role $docType
	revokeRoleWriteAccessDocType $role $docType	
}

proc revokeRoleReadWriteAccessDocTypes { role docTypes } {
	
	foreach docType $docTypes {
        if { [xvalue exists [asset.doc.type.exists :type $docType]] == "true" } {
		    revokeRoleReadWriteAccessDocType $role $docType
        }
	}
	
}


# A function to rename a existing dictionary (by exporting the entries to local file then importing them into the newly created dictionary).
#
proc rename_dict { old_name new_name } {
    # check if the old/src dictionary exists
    if { [xvalue exists [dictionary.exists :name ${old_name} ]] != "true" } {
#       puts "dictionary: ${old_name} does not exist."
        return
    }
    # check if the new/dst dictionary exists
    if { [xvalue exists [dictionary.exists :name ${new_name} ]] == "true" } {
#       puts "dictionary: ${new_name} already exists."
        return
    }
    
    # rename
    dictionary.rename :name ${old_name} :new-name ${new_name}
}

#
# A function to rename/migrate global pssd/daris dictionaries with a namespace.
# 
proc rename_global_tag_dicts {} {
    set types { "project" "subject" "ex-method" "study" "dataset" "data-object" }
    foreach type $types {
        set old_name "pssd.${type}.tags"
        set new_name "daris-tags:${old_name}"
        rename_dict ${old_name} ${new_name}
    }
}

#
# A function to rename/migrate project specific dictionaries.
#
proc rename_project_tag_dicts { project } {
    set types { "project" "subject" "ex-method" "study" "dataset" "data-object" }
    foreach type $types {
        set old_name "pssd.${type}.tags.${project}"
        set new_name "daris-tags:${old_name}"
        rename_dict ${old_name} ${new_name}
    }
}

#
# A function to rename/migrate all pssd/daris dictionaries.
#
proc rename_all_pssd_tag_dicts {} {
    # rename global dictionaries
    puts "   Renaming global tag dictionaries"
    rename_global_tag_dicts 
    
    # rename all project specific dictionaries
    puts "Renaming project-based tag dictionaries"
    foreach project [xvalues cid [asset.query :where "model='om.pssd.project'" :action get-cid :size infinity]] {
        rename_project_tag_dicts $project
    }
}


proc revokeDocumentTypePerms {} {

# Analyzer
   puts "   Revoke doc type permissions for analyzer package"
   revokeRoleReadWriteAccessDocTypes basic-user { nig-nifti-1 }

# Essentials
   puts "   Revoke doc type permissions for essentials package"
   revokeRoleReadWriteAccessDocTypes basic-user { hfi-bruker-study hfi-bruker-series \
                                              nig-siemens-raw-petct-study nig-siemens-raw-petct-series \
                                              nig-siemens-raw-mr-study nig-dicom-dataset nig-dicom-series }
# PSSD
   puts "   Revoke doc type permissions for PSSD package"
   revokeRoleReadWriteAccessDocTypes pssd.model.doc.user \
    { pssd-object   pssd-filename  pssd-project        pssd-subject \
      pssd-ex-method pssd-study          pssd-dataset \
      pssd-transform pssd-acquisition    pssd-derivation \
      pssd-method    pssd-method-subject pssd-method-rsubject \
      pssd-notification pssd-project-harvest pssd-project-owner \
      pssd-project-governance pssd-project-research-category \
      pssd-publications pssd-related-services \
      pssd-role-member-registry \
      pssd-dicom-server-registry \
      pssd-shoppingcart-layout-pattern pssd-dicom-ingest }

    if { [xvalue exists [asset.doc.type.exists :type pssd-role-member-registry]] == "true" } {
        revokeRolePerm pssd.dicom-ingest { document pssd-role-member-registry ACCESS }
    }
}



# =====================================================================================
# Here the work begins
# =====================================================================================
# We are migrating
# document types into namespaces daris:
# dictionaries into namespace daris:
# roles into namespace daris:

proc migrate_namespaces {} {
# Check property to see if we have done this already
set prop daris-namespaces-migrate-1
set app daris
set exists [xvalue exists [application.property.exists :property -app $app $prop]]
if { $exists == "true" } {
    set pv [xvalue property [application.property.get :property -app $app $prop]]
    if { $pv == "true" } {
      puts "DaRIS namespace migration already done - skipping"
      return
    } 
} else {
   puts "Creating DaRIS application migration  property $prop"
   application.property.create :property -app $app -name $prop
}

# CHeck server version
requireServerVersion 4.0.058

# Check if the required service: om.pssd.doctype.rename service exists.
if { [xvalue exists [system.service.exists :service om.pssd.doctype.rename]] == "false" } {
    puts "The required service om.pssd.doctype.rename does not exist. Please install PSSD V2.22, which includes the required service, from the old release stable-2-26: https://daris-1.cloud.unimelb.edu.au/daris/old-releases/stable-2-26/mfpkg-pssd-2.22-mf4.0.030.zip."
    error "The required service om.pssd.doctype.rename does not exist."
}

# Create meta-data  namespace
set exists [xvalue exists [asset.doc.namespace.exists :namespace "daris"]]
puts "meta namespace exists $exists"
if { $exists == "false" } {
   puts "Creating daris document type namespace"
   asset.doc.namespace.create :description "Namespace for DaRIS framework document types" :namespace daris
} else {
   puts "DaRIS Document name space already exists"
}


# First revoke all the old permissions for the old document types.  The doc type renamer
# does not take care of this.  I think it should... We must revoke now, because after we rename
# they don't exist and can't be revoked !
puts "Revoking all old document type permissions"
revokeDocumentTypePerms
 
# Migrate doc types in standard assets.  Skips those already renamed  types that don't exist  so can be restarted 
# and has no impact once completed.  Packages included here are analyzer, essentials and pssd

# Analyzer package
puts "Renaming Analyzer package document types"
if { [xvalue exists [asset.doc.type.exists :type nig-nifti-1]] == "true" } {
    om.pssd.doctype.rename :templates false  :type  < :old nig-nifti-1 :new daris:nifti-1 >
}
# Essentials
puts "Renaming Essentials package document types"
om.pssd.doctype.rename :templates false \
    :type < :old hfi-bruker-study :new daris:bruker-study  > \
    :type < :old hfi-bruker-series :new daris:bruker-series > \
    :type < :old nig-dicom-series :new daris:dicom-series > \
    :type < :old nig-dicom-dataset :new daris:dicom-dataset > \
    :type < :old nig-siemens-raw-petct-study :new daris:siemens-raw-petct-study > \
    :type < :old nig-siemens-raw-petct-series :new daris:siemens-raw-petct-series  > \
    :type < :old nig-siemens-raw-mr-study :new daris:siemens-raw-mr-study >

# PSSD
puts "Renaming PSSD package document types"
puts "   Chunk 1 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-object :new daris:pssd-object > \
    :type < :old pssd-project :new daris:pssd-project >
 
 
puts "   Chunk 2 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-subject :new daris:pssd-subject > \
    :type < :old pssd-ex-method :new daris:pssd-ex-method >
    
puts "   Chunk 3 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-study :new daris:pssd-study > \
    :type < :old pssd-dataset :new daris:pssd-dataset > \
    :type < :old pssd-transform :new daris:pssd-transform > \
    :type < :old pssd-acquisition :new daris:pssd-acquisition > \
    :type < :old pssd-derivation :new daris:pssd-derivation >
    
puts "   Chunk 4 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-method :new daris:pssd-method > \
    :type < :old pssd-method-subject :new daris:pssd-method-subject > \
    :type < :old pssd-method-rsubject :new daris:pssd-method-rsubject > \
    :type < :old pssd-state :new daris:pssd-state >
    
puts "   Chunk 5 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-role-member-registry :new daris:pssd-role-member-registry > \
    :type < :old pssd-notification :new daris:pssd-notification > \
    :type < :old pssd-repository-description :new daris:pssd-repository-description > \
    :type < :old pssd-project-owner :new daris:pssd-project-owner > \
    :type < :old pssd-publications :new daris:pssd-publications > \
    :type < :old pssd-related-services :new daris:pssd-related-services >
    
puts "   Chunk 6 (of 6)"
om.pssd.doctype.rename :templates false \
    :type < :old pssd-project-governance :new daris:pssd-project-governance > \
    :type < :old pssd-project-research-category :new daris:pssd-project-research-category > \
    :type < :old pssd-shoppingcart-layout-pattern :new daris:pssd-shoppingcart-layout-pattern > \
    :type < :old pssd-filename :new daris:pssd-filename > \
    :type < :old pssd-system-announcement :new daris:pssd-system-announcement > \
    :type < :old pssd-dicom-ingest :new daris:pssd-dicom-ingest > \
    :type < :old pssd-dicom-server-registry :new daris:pssd-dicom-server-registry > \
    :type < :old pssd-project-harvest :new daris:pssd-project-harvest >

# Create dictionary meta-data  namespace
set exists [xvalue exists [dictionary.namespace.exists :namespace "daris"]]
if { $exists == "false" } {
   puts "Creating daris dictionary type namespace"
   dictionary.namespace.create :description "Namespace for DaRIS framework dictionaries" :namespace daris
}

# Now rename static PSSD dictionaries
puts "Renaming static dictionaries to daris namespace"
if { [xvalue exists [dictionary.exists :name pssd.study.types]]  == "true" } {
   dictionary.rename :name pssd.study.types :new-name daris:pssd.study.types
}
if { [xvalue exists [dictionary.exists :name pssd.ANZSRC.Division-11.field-of-research]]  == "true" } {
   dictionary.rename :name pssd.ANZSRC.Division-11.field-of-research :new-name daris:pssd.ANZSRC.Division-11.field-of-research
}
if { [xvalue exists [dictionary.exists :name pssd.research.organization]]  == "true" } {
   dictionary.rename :name pssd.research.organization :new-name daris:pssd.research.organization
}
if { [xvalue exists [dictionary.exists :name pssd.dicom.modality]]  == "true" } {
   dictionary.rename :name pssd.dicom.modality :new-name daris:pssd.dicom.modality
}
if { [xvalue exists [dictionary.exists :name pssd.human.name.prefix]]  == "true" } {
   dictionary.rename :name pssd.human.name.prefix :new-name daris:pssd.human.name.prefix
}
if { [xvalue exists [dictionary.exists :name pssd.publication.identifier.type]]  == "true" } {
   dictionary.rename :name pssd.publication.identifier.type  :new-name daris:pssd.publication.identifier.type
}
if { [xvalue exists [dictionary.exists :name pssd.funding.organization]]  == "true" } {
   dictionary.rename :name pssd.funding.organization :new-name daris:pssd.funding.organization
} 
if { [xvalue exists [dictionary.exists :name pssd.ethics.organization]]  == "true" } {
   dictionary.rename :name pssd.ethics.organization :new-name daris:pssd.ethics.organization
}
if { [xvalue exists [dictionary.exists :name pssd.project.asset.namespaces]]  == "true" } {
   dictionary.rename :name pssd.project.asset.namespaces :new-name daris:pssd.project.asset.namespaces
}
if { [xvalue exists [dictionary.exists :name pssd.project.cid.rootnames]]  == "true" } {
   dictionary.rename :name pssd.project.cid.rootnames :new-name daris:pssd.project.cid.rootnames
}

# Now migrate all the tag-library dictionaries. This will correctly update references to dictionares in TAG meta-data
# Create dictionary meta-data  namespace
set exists [xvalue exists [dictionary.namespace.exists :namespace "daris-tags"]]
if { $exists == "false" } {
   puts "Creating daris tag library dictionary namespace"
   dictionary.namespace.create :description "Namespace for DaRIS framework tag library dictionaries" :namespace daris-tags
}
puts "Renaming tag library dictionaries"
rename_all_pssd_tag_dicts

# Now handle all the PSSD roles which are also moving to their own daris namespace
puts "Creating daris role namespace"
authorization.role.namespace.create :namespace daris :ifexists ignore :description "Namespace for daris framework roles"
puts "Migrating static PSSD roles"
migrateStaticRoles
puts "Migrating project-based roles"
migrateProjectsRoles

# Finally fix any saved queries (or destroy them or note them)
###

# Set persistent property saying we have done this
puts "Setting application property"
application.property.set  :property -app $app -name $prop true
puts "Completed. Now you can install the packages as per usual and then migrate the templates with om.pssd.doctype.rename :templates true"
}

