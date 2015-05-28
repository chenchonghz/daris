# Install the trigger script for /dicom namespace. The trigger script is triggered when NON-PSSD style
# DICOM data arrived in /dicom namespace. It sends notifications to system admins to notify the data
# arrivals in /dicom namespace.
set dicom_ns                   dicom
set dicom_trigger_script       trigger-dicom-ingest.tcl
set dicom_trigger_script_ns    system/triggers
set dicom_trigger_script_label [string toupper PACKAGE_$package]

if { [xvalue exists [asset.namespace.exists :namespace $dicom_ns]] == "true" } {
    
    # destroy the script asset if it pre-exists
    if { [xvalue exists [asset.exists :id path=${dicom_trigger_script_ns}/${dicom_trigger_script}]] == "true" } {
        asset.hard.destroy :id path=${dicom_trigger_script_ns}/${dicom_trigger_script}
    }

    # create the trigger script asset
    asset.create :url archive:///$dicom_trigger_script \
        :namespace -create yes $dicom_trigger_script_ns \
        :label -create yes $dicom_trigger_script_label :label PUBLISHED \
	    :name $dicom_trigger_script

    # remove all old triggers on the namespace
    asset.trigger.destroy :namespace $dicom_ns

    # create the triggers
    asset.trigger.post.create :namespace $dicom_ns :event create :script -type ref ${dicom_trigger_script_ns}/${dicom_trigger_script}
    asset.trigger.post.create :namespace $dicom_ns :event content-modify :script -type ref ${dicom_trigger_script_ns}/${dicom_trigger_script}
}
