# ============================================================================
# 
# Args:                                                                       
#     debabelerJavaXmx    - maximum java heap size for debabeler process.
#                            defaults to 512M (512MB)
#     dicomNifti          - dicom to nifti transcode provider.
#     dicomAnalyzeNL      - dicom to analyze(NL) transcode provider.
#     dicomAnalyzeRL      - dicom to analyze(RL) transcode provider.
#     dicomMinc           - dicom to minc transcode provider.
#     dicomRda            - dicom to siemens rda transcode provider.
#     brukerAnalyzeNL     - bruker to analyze(NL) transcode provider.
#     brukerAnalyzeRL     - bruker to analyze(RL) transcode provider.
#     brukerMinc          - bruker to minc transcode provider.
# ============================================================================


# remove the old 'nig-transcode' package if it exists.
source old-release-cleanup.tcl


# register mime types. 
source mime-types.tcl

# install plugins
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        daris-transcoders-plugin.zip
set plugin_jar        daris-transcoders-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      daris.transcode.DarisTranscodePluginModule
set plugin_libs       { libs/daris-commons.jar libs/daris-dcmtools.jar libs/loni-debabeler.jar }

# import the archive
asset.import :url archive:///$plugin_zip \
    :namespace -create yes $plugin_namespace \
    :label -create yes $plugin_label :label PUBLISHED \
    :update true

# add plugin module
set config ""
if { [info exists debabelerJavaXmx] } {
    set config "${config} :config -name debabelerJavaXmx ${debabelerJavaXmx}"
} else {
    if { [xvalue uuid [server.uuid]] == "1004" } {
        # daris-1: 6GB
        set config "${config} :config -name debabelerJavaXmx 6000M"
    } else {
        # default: 2GB
        set config "${config} :config -name debabelerJavaXmx 2000M"
    }
}
if { [info exists dicomNifti] } {
    set config "${config} :config -name dicomNifti ${dicomNifti}"
}
if { [info exists dicomAnalyzeNL] } {
    set config "${config} :config -name dicomAnalyzeNL ${dicomAnalyzeNL}"
}
if { [info exists dicomAnalyzeRL] } {
    set config "${config} :config -name dicomAnalyzeRL ${dicomAnalyzeRL}"
}
if { [info exists dicomMinc] } {
    set config "${config} :config -name dicomMinc ${dicomMinc}"
}
if { [info exists dicomRda] } {
    set config "${config} :config -name dicomRda ${dicomRda}"
}
if { [info exists brukerAnalyzeNL] } {
    set config "${config} :config -name brukerAnalyzeNL ${brukerAnalyzeNL}"
}
if { [info exists brukerAnalyzeRL] } {
    set config "${config} :config -name brukerAnalyzeRL ${brukerAnalyzeRL}"
}
if { [info exists brukerMinc] } {
    set config "${config} :config -name brukerMinc ${brukerMinc}"
}

# remove the plugin module if it pre-exists
if { [xvalue exists [plugin.module.exists :path $plugin_path :class $module_class]] == "true" } {
    plugin.module.remove :path $plugin_path :class $module_class    
}

# add plugin module
set args ":path ${plugin_path} :class ${module_class} ${config}"
foreach lib ${plugin_libs} {
    set args "${args} :lib ${lib}"
}
plugin.module.add ${args}

# Because the MF class loader does not work for loni-debabler.jar (as the jar file contains SPI files, 
# MF class loader does not handle it properly), we have to put the loni-debabler.jar file into 
# ${MFLUX_HOME}/plugin/bin directory.
# Note: the server need to be restarted to load the jar files in ${MF_HOME}/plugin/bin/ directory.
asset.get :id path=/mflux/plugins/libs/loni-debabeler.jar \
          :url file:[xvalue property\[@key='mf.home'\] [server.java.environment] ]/plugin/bin/loni-debabeler.jar

# reload service list
system.service.reload

# set role permssions
source role-permissions.tcl

# set service permissions
source service-permissions.tcl
