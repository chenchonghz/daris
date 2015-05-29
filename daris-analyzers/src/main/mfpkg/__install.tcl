source old-release-cleanup.tcl

source utils.tcl

# DaRIS underwent a namespaces migration (stable-2-27).   Document, dictionary and role namespaces
# were all migrated out of the global namespace.   The migration must have been executed
# before installing this version (unless it's a fresh install - no PSSD objects).
if { [isDaRISInstalled] == "true" } {
   checkAppProperty daris daris-namespaces-migrate-1 "DaRIS namespaces migration has not been done. You must undertake this migration first by installing stable-2-27 and undertaking the migration."
}  else {
# Set the property now  in the case it was a fresh install (nothing to migrate)
   application.property.create :ifexists ignore  :property -app daris -name daris-namespaces-migrate-1 < :value "true" >
}
# ============================================================================
# Create MIME Types; these are primarily made in the essentials package.
# but in case you use this package without it, they are made here too.
# ============================================================================
createMimeType nifti      "NIFTI-1 image"
createMimeType minc       "MINC image"


# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        daris-analyzers-plugin.zip
set plugin_jar        daris-analyzers-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      nig.mf.plugin.analyzer.AnalyzerPluginModule
set plugin_libs       { loni-image-io-plugins.jar }


# Import the archive
asset.import :url archive:///$plugin_zip \
    :namespace -create yes $plugin_namespace \
    :label -create yes $plugin_label :label PUBLISHED \
    :update true

# Remove the plugin module if it pre-exists
if { [xvalue exists [plugin.module.exists :path $plugin_path :class $module_class]] == "true" } {
        plugin.module.remove :path $plugin_path :class $module_class    
}

# Add plugin module
plugin.module.add :path $plugin_path \
                  :class $module_class  \
                  :lib libs/loni-image-io-plugins.jar

# Because the MF class loader does not work for loni-image-io-plugins.jar (the jar file contains SPI files, MF class loader 
# does not handle it properly), we have to put the loni-image-io-plugins.jar file into ${MF_HOME}/plugin/bin directory.
# Note: the server need to be restarted to load the jar files in ${MF_HOME}/plugin/bin/ directory.
asset.get :id path=/mflux/plugins/libs/loni-image-io-plugins.jar \
          :url file:[xvalue property\[@key='mf.home'\] [server.java.environment] ]/plugin/bin/loni-image-io-plugins.jar

system.service.reload

srefresh

# ============================================================================
# Document Types
# ============================================================================
source doctypes.tcl

# ============================================================================
# Permissions
# ============================================================================

source roleperms.tcl

