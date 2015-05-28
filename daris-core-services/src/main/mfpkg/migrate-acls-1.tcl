##
## This script is to adapt the acl permission model change proposed in Nov 2014.
## More detail about the acl permission model change see: http://nsp.nectar.org.au/wiki-its-r/doku.php?id=data_management:daris:admin:daris_object_permission_model
## 
## This script is included in the daris-core-series package. To execute it during the 
## package installation: 
##    > package.install :int file:/path/to/mfpkg-daris-core-services-x.x.x.zip :arg -name migrateAcls1 true
## 
## It can also be executed manually in aterm:
##    > set migrateAcls1 true 
##    > source /path/to/migrate-acls-1.tcl
##


set APP "daris"
set PROPERTY_ACL_MIGRATION_1 "object-acl-migration-1"

##
## update project member acl to read-write on the given asset/object.
## 
proc update_member_acl { id project_cid } {
    set role "pssd.project.member.${project_cid}"
    set index [lsearch [xvalues asset/acl/actor [asset.acl.describe :id $id]] $role]
    if { $index >= 0 } {
        set metadata [lindex [xvalues asset/acl/metadata [asset.acl.describe :id $id]] $index]
       #set content  [lindex [xvalues asset/acl/content  [asset.acl.describe :id $id]] $index]
        if { $metadata == "read" } {
            asset.acl.grant :id $id :acl < :actor -type role $role :propagate true :access "read-write" >
            puts "updated acl of asset ${id} to read-write for ${role}."
        }
    }
}

##
## update subject admin acl to read-write on the given asset/object.
## 
proc update_subject_admin_acl { id project_cid } {
    set role "pssd.project.subject.admin.${project_cid}"
    set index [lsearch [xvalues asset/acl/actor [asset.acl.describe :id $id]] $role]
    if { $index >= 0 } {
        set metadata [lindex [xvalues asset/acl/metadata [asset.acl.describe :id $id]] $index]
       #set content  [lindex [xvalues asset/acl/content  [asset.acl.describe :id $id]] $index]
        if { $metadata == "read" } {
            asset.acl.grant :id $id :acl < :actor -type role $role :propagate true :access "read-write" >
            puts "updated acl of asset ${id} to read-write for ${role}."
        }
    }
}

##
## update the acls of all the ex-methods, studies and datasets for project member role (to read-write).
##
proc update_acls {  } {
    upvar APP APP
    upvar PROPERTY_ACL_MIGRATION_1 PROPERTY_ACL_MIGRATION_1
    if { [xvalue exists [application.property.exists :property -app ${APP} ${PROPERTY_ACL_MIGRATION_1}]] == "true" } {
        puts "Application property: ${PROPERTY_ACL_MIGRATION_1} exists. Already migrated. Nothing to do."
        return
    }
    foreach project_cid [xvalues cid [asset.query :where model='om.pssd.project' :size infinity :action get-cid]] {
        puts "updating acls of the ex-methods, studies and data sets in project ${project_cid}..."
        foreach exmethod_cid [xvalues cid [asset.query :where model='om.pssd.ex-method' and cid starts with '${project_cid}' :size infinity :action get-cid]] {
            puts "updating acls of the studies and data sets in ex-method ${exmethod_cid}..."
            set exmethod_id [xvalue asset/@id [asset.get :cid ${exmethod_cid}]]
            update_subject_admin_acl ${exmethod_id} ${project_cid}
            update_member_acl ${exmethod_id} ${project_cid}
            foreach study_cid [xvalues cid [asset.query :where cid in '${exmethod_cid}' :size infinity :action get-cid]] {
                puts "updating acls of the data sets in study ${study_cid}..."
                set study_id [xvalue asset/@id [asset.get :cid ${study_cid}]]
                update_member_acl ${study_id} ${project_cid}
                foreach dataset_id [xvalue id [asset.query :where cid in '${study_cid}' :size infinity :action get-id]] {
                    update_member_acl ${dataset_id} ${project_cid}
                }
            }
        }
    }
    application.property.create \
                :ifexists ignore \
                :property -app ${APP} -name ${PROPERTY_ACL_MIGRATION_1} < \
                    :type -type boolean \
                    :value true \
                    :description "The property to determine if the ACL migration(2014-11-11) has been done." >
}


set HAS_DARIS_DATA [expr [xvalue value [asset.query :where model='om.pssd.project' :action count]] > 0]
set ACLS1_MIGRATED [expr { [xvalue exists [application.property.exists :property -app $APP ${PROPERTY_ACL_MIGRATION_1}]] == "true" }]
set MIGRATE_ACLS1  0
if { [info exists migrateAcls1] } {
    set MIGRATE_ACLS1 [ expr { $migrateAcls1=="true" } ]
}

if { $HAS_DARIS_DATA } {
    # found daris data
    puts "Found daris data."
    if { $ACLS1_MIGRATED } {
        # already migrated
        puts "Daris data has already been migrated."
    } else {
        if { $MIGRATE_ACLS1 } {
            # do migration
            update_acls
        } else {
            puts "ACL migrateion is required. To start the ACL migration, install the package with additional argument: migrateAcls1."
            puts "For example, 'package.install :in file:/path/to/mfpkg-daris-core-services-x.x.x.zip :arg -name migrateAcls1 true'"
            puts "It may take a few minutes depends on the number of daris assets."
            error "ACL migration(2014-11-11) is required. Install with arg migrateAcls1 set to true."
        }
    }
} else {
    puts "No daris data found."
    puts "Setting application property: ${PROPERTY_ACL_MIGRATION_1} to true."
    application.property.create \
            :ifexists ignore \
            :property -app ${APP} -name ${PROPERTY_ACL_MIGRATION_1} < \
                :type -type boolean \
                :value true >
}
