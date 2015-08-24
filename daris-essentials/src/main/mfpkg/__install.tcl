
# ============================================================================
# Include the util functions
# ============================================================================
source utils.tcl

# DaRIS underwent a namespaces migration (stable-2-27).   Document, dictionary and role namespaces
# were all migrated out of the global namespace.   The migration must have been executed
# before installing this version (unless it's a fresh install - no PSSD objects).
if { [isDaRISInstalled] == "true" } {
   checkAppProperty daris daris-namespaces-migrate-1 "DaRIS namespaces migration has not been done. You must undertake this migration first by installing stable-2-27 and undertaking the migration."
} else {
# Set the property now  in the case it was a fresh install (nothing to migrate)
   application.property.create :ifexists ignore  :property -app daris -name daris-namespaces-migrate-1 < :value "true" >
}


##
## Remove the predeccessor: nig-essentials
##
source old-release-cleanup.tcl

# ============================================================================
# Add plugin module
# ============================================================================
source plugin-module-add.tcl

# ============================================================================
# Define the mime types
# ============================================================================
source mime-types.tcl

# The DaRIS namespace is used by the essentials package also
set exists [xvalue exists [asset.doc.namespace.exists :namespace "daris"]]
if { $exists == "false" } {
   asset.doc.namespace.create :description "Namespace for DaRIS framework document types" :namespace daris
}

# These doc types are very generic and match their types definitions
# above
source doctypes-bruker.tcl
source doctypes-siemens-raw.tcl
source doctypes-dicom.tcl

# ============================================================================
# Define roles and service permissions
# ============================================================================
source roleperms.tcl
source service-permissions.tcl
