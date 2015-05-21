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
    if { [xvalue exists [application.property.exists :property -app daris object-acl-migration-1]] == "true" } {
        puts "Application property: object-acl-migration-1 exists. No action to take."
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
    application.property.create :ifexists ignore :property -app daris -name object-acl-migration-1 true < :type -type boolean :value true :description "The property to determine if the acl migration script (Nov 2014) has been executed." >
}


##
## Migration the acls on the existing daris objects.
##
update_acls

