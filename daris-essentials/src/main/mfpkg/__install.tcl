# ============================================================================
# NOTE:
#     DaRIS underwent a namespaces migration (stable-2-27). Document types, 
# dictionaries and role namespaces were all migrated out of the global namespace.
# The migration must have been executed before installing this version unless 
# it's a fresh install - no PSSD objects exist in the system.
# ============================================================================
if { [xvalue exists [authorization.role.exists :role pssd.model.user]] == "true" || [xvalue exists [authorization.role.exists :role daris:pssd.model.user]] == "true" } {
    # daris has been installed before as the daris role exists
    if { [xvalue exists [application.property.exists :property -app daris daris-namespaces-migrate-1]] == "false" || [xvalue property [application.property.get :property -app daris daris-namespaces-migrate-1]] == "false" } {
        error "DaRIS namespaces migration has not been done. You must undertake this migration first by installing stable-2-27 and undertaking the migration."
    }
} else {
    # daris does not pre-exist. It is a fresh install so just set the application property.
    application.property.create :ifexists ignore \
        :property -app daris -name daris-namespaces-migrate-1 < :value "true" >
}

# ============================================================================
# Clean up old release (nig-essentials)
# ============================================================================
source old-release-cleanup.tcl

# ============================================================================
# Add plugin module
# ============================================================================
source plugin-module-add.tcl

# ============================================================================
# Define the mime types
# ============================================================================
source mime-types.tcl

# ============================================================================
# Create document namespace: daris
# ============================================================================
if { [xvalue exists [asset.doc.namespace.exists :namespace "daris"]] == "false" } {
   asset.doc.namespace.create :namespace daris \
       :description "Namespace for DaRIS document types"
}

# ============================================================================
# Create document types
# ============================================================================
source doc-types-bruker.tcl
source doc-types-siemens-raw.tcl
source doc-types-dicom.tcl

# ============================================================================
# Set roles and their permissions
# ============================================================================
source role-permissions.tcl

# ============================================================================
# Set service permissions
# ============================================================================
source service-permissions.tcl
