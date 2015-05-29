#
# args:
#  - java.xmx <memory in MB> for debabeler
#  - dicom.trans <debabeler, mrtrix>
#

# remove the nig-transcode package if it exists.
source old-release-cleanup.tcl

# 
source utils.tcl

# ============================================================================
# Create MIME Types; these are primarily made in the essentials package.
# but in case you use this package without it, they are made here too.
# ============================================================================
createMimeType dicom/series      "DICOM series"
createMimeType analyze/series/nl "Analyze(Neurological)"
createMimeType analyze/series/rl "Analyze(Radiological)"
createMimeType nifti/series      "NIFTI series"
createMimeType siemens/rda       "RDA(Siemens Spectrum)"
createMimeType bruker/series     "Bruker/Paravision image series"
createMimeType bruker/fid        "Bruker Free-Induction Decay data"

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        daris-transcoders-plugin.zip
set plugin_jar        daris-transcoders-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      nig.mf.plugin.transcode.TranscodePluginModule
set plugin_libs       { daris-commons.jar daris-dcmtools.jar loni-debabeler.jar }


# Import the archive
asset.import :url archive:///$plugin_zip \
    :namespace -create yes $plugin_namespace \
    :label -create yes $plugin_label :label PUBLISHED \
    :update true

# Add plugin module
if { ![info exists java.xmx] } {
    if { [xvalue uuid [server.uuid]] == "1004" } {
	# daris-1: 6GB
	set java.xmx "6144"
    } elseif { [xvalue uuid [server.uuid]] == "1035" } {
	# vera059: 4GB
	set java.xmx "4096"
    } else {
	# default: 2GB
	set java.xmx "2048"
    }
}

# Default dicom to nifti transcoder
if { ![info exists dicom.trans] } {
    set dicom.trans "debabeler"
} 

set JavaXmxOption "-Xmx${java.xmx}m"

# remove the plugin module if it pre-exists
if { [xvalue exists [plugin.module.exists :path $plugin_path :class $module_class]] == "true" } {
    plugin.module.remove :path $plugin_path :class $module_class    
}

# add plugin module
plugin.module.add :path $plugin_path \
                  :class $module_class \
                  :config -name DICOMTranscoder "\"${dicom.trans}\"" \
                  :config -name JavaXmxOption "\"${JavaXmxOption}\"" \
                  :lib libs/daris-commons.jar \
                  :lib libs/daris-dcmtools.jar \
                  :lib libs/loni-debabeler.jar

# Because the MF class loader does not work for loni-debabler.jar (the jar file contains SPI files, MF class loader 
# does not handle it properly), we have to put the loni-debabler.jar file into ${MF_HOME}/plugin/bin directory.
# Note: the server need to be restarted to load the jar files in ${MF_HOME}/plugin/bin/ directory.
asset.get :id path=/mflux/plugins/libs/loni-debabeler.jar \
          :url file:[xvalue property\[@key='mf.home'\] [server.java.environment] ]/plugin/bin/loni-debabeler.jar

system.service.reload

# ============================================================================
# Define roles and service permissions
# ============================================================================
source roleperms.tcl
