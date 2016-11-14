#
# daris-core-services (originally pssd)
#
# NOTE: It is assumed that the /daris (or /pssd) asset store exists and that they were 
#       configured with stores as part of the Mediaflux installation process.
# 
#       This installer assumes these namespaces in the installation of the trigger scripts
#       Otherwise, pssd is the default asset namespace in which assets are created.
#
#
# arg:           host
#   type:        string
#   description: specifies the schema member host. It is required when installing the package
#                within non-default schema.

# arg:           domain
#   type:        boolean
#   default:     false
#   description: set to true to activate the creation of some sample domain specific 
#                document types, dictionaries (including study types) and methods. Defaults
#                to false.

# arg:           migrateAcls1
#   type:        boolean
#   default:     false
#   description: run the script to update acls according to the permission model change in
#                November 2014. Defaults to false, which means if the script detects that 
#                the system needs to be migrated it will stop the installation. If set to 
#                true, if the system need to be migrated, it call the migration script, 
#                which will take a few minutes to finish.

# arg:           migrateNamespaces1
#   type:        boolean
#   default:     false
#   description: set to true to run the script to migrate the document types, dictionaries and roles to
#                namespace: daris. 
#                NOTE: this arg is not implemented.

# arg:           model
#   type:        boolean
#   default:     true
#   description: set to false to not make any changes to the object model. (e.g. that 
#                might have been added by other packages such as daris-nig.) Defaults to 
#                true, which causes to overwrite the data model and set model registration 
#                meta-data.

# arg:           updateSCTemplate
#   type:        boolean
#   default:     false
#   description: set to true to replace the existing shopping cart template, which will 
#                need to destroy all the existing shopping carts associated with the 
#                template.


## acl migration(1) stable-2-26 (Nov-2014):
## Check if the ACL migration script has been executed. If not, and the migrateAcls1 arg is set to true.
## It also checks if there is any daris objects in the system (if not, it does not matter as there is 
## nothing to migrate).
source migrate-acls-1.tcl

## namespace migration(1) stable-2-27 (Feb-2015): 
## DaRIS underwent a namespaces migration (stable-2-27).   Document, dictionary and role namespaces
## were all migrated out of the global namespace.   The migration must have been executed
## before installing this version (unless it's a fresh install of the current version or later).
## Just check if the migration is done. If not, throw exception and quit the installation.
source migrate-namespaces-1-check.tcl

## project dictionary namespace(1) (Nov-2016)
source migrate-project-dictionary-namespace-1.tcl


##
## Remove the predecessor, PSSD package if it exists
##
source old-release-cleanup.tcl

# include utility functions in utils.tcl
source utils.tcl

#============================================================================================
# Create dictionary namespaces
dictionary.namespace.create :description "Namespace for DaRIS framework dictionaries" :namespace daris :ifexists ignore
dictionary.namespace.create :description "Namespace for DaRIS framework project-based tag library dictionaries" :namespace daris-tags :ifexists ignore

# ============================================================================
# Create Study types dictionary holder
# ============================================================================
createDictionary daris:pssd.study.types

# Add a generic study type to this dictionary
dictionary.entry.add :dictionary daris:pssd.study.types :term Unspecified

# ============================================================================
# Create Relationship Types
# ============================================================================
createRelationshipType pssd-private pssd-private-of

# ============================================================================
# Install the plugin module
# ============================================================================
source plugin-module-add.tcl

#
# ============================================================================
# Create standard pssd and dicom namespaces.  Exception if matching pssd 
# and dicom stores do not exist.
# ============================================================================
source pssd-namespaces.tcl

# ============================================================================
# Install DICOM ingest FCP file
# ============================================================================
source fcp.tcl
set daris_fcp_namespace [get_daris_fcp_namespace]
install_fcp ${daris_fcp_namespace} pssd.dicom.ingest.fcp "Import local DICOM files"
install_fcp ${daris_fcp_namespace} pssd.import.fcp "Import local files"


#
# ============================================================================
# Create the standard 'pssd.project' pssd.method' CID roots. These can be made 
# on demand but it's clearer to put then here 
# ============================================================================
citeable.named.id.create :name "pssd.project"
citeable.named.id.create :name "pssd.method"

# ============================================================================
# Create dictionaries
# ============================================================================
source dictionaries.tcl
createUpdatePSSDDicts
#
source dictionaries-ANZSRC.tcl
createUpdateANZSRCPSSDDicts


# ============================================================================
# Mime types for registries
createMimeType pssd-dicom-server-registry "DICOM server registries"
createMimeType pssd-role-member-registry "Project Role-member registry"

#
# =============================================================================
# Asset relationships
if { [xvalue exists [asset.relationship.type.exists :type thumbnailed-to]] == "true" } {
   asset.relationship.type.destroy :type thumbnail
}
if { [xvalue exists [asset.relationship.type.exists :type thumbnail]] == "false" } {
   asset.relationship.type.create \
       :type -maximum 1 -container true -ondestroy propagate thumbnail \
       :inverse -maximum 1 thumbnail-of
}

# ============================================================================
# Create Doc Types
# ============================================================================
set exists [xvalue exists [asset.doc.namespace.exists :namespace "daris"]]
if { $exists == "false" } {
   asset.doc.namespace.create :description "Namespace for DaRIS framework document types" :namespace daris
}
source doctypes.tcl
source doctypes-dicom.tcl
source doctypes-harvest.tcl
source doctypes-mytardis.tcl

# ============================================================================
# Setup Roles and Permissions
# ============================================================================
source role-permissions.tcl
source service-permissions.tcl

# ============================================================================
# Declare the "standard" object models
# Pass "model" from package install: package.install :arg -name model true/false
# ============================================================================
source models.tcl
set addModel 1
if { [info exists model] } {
    if { $model == "false" } {
        set addModel 0
    }
}
if { $addModel == 1 } {
    addPSSDModels
    registerModelMetaData
}

# ============================================================================
# Install the trigger for /dicom namespace, which monitoring the arrivals of
# NON-PSSD style DICOM data and send notifications. The install script
# will first uninstall any existing triggers
# ============================================================================
source triggers-install.tcl

# ============================================================================
# Create/update the default shopping cart template (name: pssd)
# ============================================================================
set replaceSCT "false"
if { [info exists updateSCTemplate] } {
    if { $updateSCTemplate == "true" } {
		set replaceSCT "true"
    }
}
om.pssd.shoppingcart.template.create :replace $replaceSCT

# ============================================================================
# Install the secondary shopping cart layout-pattern: pssd-filename-preserved
# ============================================================================
if { [xexists layout-pattern\[@name='pssd-filename-preserved'\] [om.pssd.shoppingcart.layout-pattern.list]] == 0 } {
    try {
        om.pssd.shoppingcart.layout-pattern.add \
            :name pssd-filename-preserved \
            :description pssd-filename-preserved \
            :pattern "cid(-7,-5)/cid(-7,-4)/cid(-7,-3)/cid(-7,-2)/replace(if-null(variable(tx-to-type),xpath(asset/type)),'/','_')/if-null(xpath(daris:pssd-filename/original), cid(-1), xpath(daris:pssd-filename/original))if-null(xpath(daris:pssd-filename/original), if-null(xpath(daris:pssd-object/name),'','_'),'')if-null(xpath(daris:pssd-filename/original),xpath(daris:pssd-object/name),'')" 
    } catch { Throwable t } {
    }
}

#=============================================================================
# Set up ANDS XSLT project meta-data harvesting asset
#=============================================================================
source ANDS-XSLT-asset-create.tcl

#=============================================================================
# Domain specific examples
#=============================================================================
set dmn 0
if { [info exists domain] } {
    if { $domain == "true" } {
        set dmn 1
    }
}
if { $dmn == 1 } {
   source domain-specific/dmn-install.tcl
}


#=============================================================================
# Create http processor
#=============================================================================
set url         /daris
set domain      mflux
set user        public
set entry_point DaRIS.html
set namespace /www/adesktop/Applications/daris
if { [xvalue exists [asset.namespace.exists :namespace ${namespace}]] == "false" } {
    asset.namespace.create :namespace -all true ${namespace} :description "the namespace for daris web application"
}

if { [info exists host] == 0 } {
    # host is required if installing into non-default schema
    set host ""
}
if { [xexists schema/name [schema.self.describe]] == 0 } {
    # in default schema. Ignore host arg.
    set host ""
}
source generic-utils.tcl
create_or_replace_http_processor $host $url "daris" $namespace $entry_point $domain $user

# grant perms for the servlet user
actor.grant :type user :name ${domain}:${user} :perm < :resource -type service transcode.describe :access ACCESS >

#=============================================================================
# Install servlets
#=============================================================================
set_http_servlets $host $url { { "main.mfjp"         "false" "daris.main" } \
                               { "object.mfjp"       "false" "daris.object" } \
                               { "shoppingcart.mfjp" "false" "daris.shoppingcart" } \
                               { "dicom.mfjp"        "false" "daris.dicom" } \
                               { "nifti.mfjp"        "false" "daris.nifti" } \
                               { "archive.mfjp"      "false" "daris.archive" } }

#=============================================================================
# Install papaya.js (DICOM/NIFTI viewer). Required by dicom.mfjp and nifti.mfjp
#=============================================================================
set label      [string toupper PACKAGE_$package]
asset.import :url archive:javascript.zip \
    :namespace -create yes www/js \
    :label -create yes ${label} :label PUBLISHED \
    :update true
