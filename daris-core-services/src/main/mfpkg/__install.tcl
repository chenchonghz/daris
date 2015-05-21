# pssd framework package installer. 
# 
# It is assumed that the pssd and dicom asset namespaces exist and that they 
# were configured with stores as part of the Mediaflux installation process.
# 
# This installer assumes these namespaces in the installation of the trigger scripts
# Otherwise, pssd is the default asset namespace in which assets are created
#
#
# Supply arguments with 
#  package.install :arg -name <arg name> <value>
#
# argument: updateSCTemplate - Set to true to replace the existing shopping cart template, which will 
#           destroy all the existing shopping carts associated with the template.
#           Defaults to false
#  
#           model - Set to false to not make any changes to the object model (e.g. that might have been added
#           by other packages such as nig-pssd).  Defaults to true which causes pssd to overwrite the
#           data model and set model registration meta-data.
#
#           bypassPerms - Set to true to bypass the setting of the roles and permissions. Defaults to false.
#           domain - Set to true to activate the creation of some sample domain specific document 
#                     types, dictionaries (including study types) and Methods
# ============================================================================
# Include utils.tcl functions
# ============================================================================
source utils.tcl


#==================================================================================
# DaRIS Migration processes
#
# Check if the ACL migration script has been executed. It is required for V2.22.
# If there are no PSSD projects then we assume this is a fresh install (or if not
# it does not matter as there is nothing to migrate)
#==================================================================================
if { [xvalue value [asset.query :where model='om.pssd.project' :action count]] > 0 } {
   checkAppProperty daris object-acl-migration-1 "ACL migration script: NIGTK/mf-scripts/update-acls-20141111.tcl needs to executed prior to the installation."
}
# If we passed the check, set the property anyway (in the case of a fresh install or no assets to migrate)
application.property.create :ifexists ignore :property -app daris -name object-acl-migration-1 true < :type -type boolean :value true :description "The property to determine if the acl migration script (Nov 2014) has been executed." >

# DaRIS underwent a namespaces migration (stable-2-27).   Document, dictionary and role namespaces
# were all migrated out of the global namespace.   The migration must have been executed
# before installing this version (unless it's a fresh install of the current version or later).
if { [isDaRISInstalled] == "true" } {
   checkAppProperty daris daris-namespaces-migrate-1 "DaRIS namespaces migration has not been done. You must undertake this migration first by installing stable-2-27 and undertaking the migration."
} else {
# Set the property now  in the case it was a fresh install (nothing to migrate)
   application.property.create :ifexists ignore  :property -app daris -name daris-namespaces-migrate-1 < :value "true" >
}
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
# Install Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        pssd-plugin.zip
set plugin_jar        pssd-plugin.jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
set plugin_libs       { nig-commons.jar dcmtools.jar }
loadPlugin  $plugin_namespace $plugin_zip $plugin_jar $module_class $plugin_label $plugin_libs


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

install_fcp /pssd/fcp pssd.dicom.ingest.fcp "Import local DICOM files"
install_fcp /pssd/fcp pssd.import.fcp "Import local files"


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

# ============================================================================
# Setup Roles and Permissions
# ============================================================================
if { [info exists bypassPerms] } {
    if { $bypassPerms == "true" } {
# Do nothing
    } else {
        source roleperms.tcl
    }
} else {
    source roleperms.tcl
}

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
   source DomainSpecific/install.tcl
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

if { [xvalue exists [http.processor.exists :url ${url}]] == "true" } {
    http.processor.destroy :url ${url}
}
http.processor.create :url ${url} \
                      :app daris \
                      :type asset \
                      :translate ${namespace} \
                      :authentication < :domain $domain :user $user > \
                      :entry-point ${entry_point}


# grant perms for the servlet user
actor.grant :type user :name ${domain}:${user} :perm < :resource -type service transcode.describe :access ACCESS >

#=============================================================================
# Install servlets
#=============================================================================
http.servlets.set :url ${url} \
                  :servlet -path main.mfjp -default false daris.main \
                  :servlet -path object.mfjp -default false daris.object \
                  :servlet -path shoppingcart.mfjp -default false daris.shoppingcart