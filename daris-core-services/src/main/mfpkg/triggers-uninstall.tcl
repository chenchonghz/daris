# Uninstall the triggers on namespace: /dicom
set dicom_ns                   dicom
set dicom_trigger_script       trigger-dicom-ingest.tcl
set dicom_trigger_script_ns    system/triggers

if { [xvalue exists [asset.namespace.exists :namespace $dicom_ns]] == "true" } {
    # remove all old triggers on the namespace
    asset.trigger.destroy :namespace $dicom_ns
}

if { [xvalue exists [asset.exists :id path=${dicom_trigger_script_ns}/${dicom_trigger_script}]] == "true" } {
    # destroy the trigger script asset
    asset.hard.destroy :id path=${dicom_trigger_script_ns}/${dicom_trigger_script}
}
