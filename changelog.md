
## Change Log:

### daris-core-services
1. Bug fix: asset.import tasks require MODIFY access to server.task.named.begin service. It is now granted (to daris:pssd.model.user)
2. Bug fix: object.attachment.* services do not rely on inverse relationship any more to find attachments (because of the bug in mediaflux server 4.2.033). Also, they now handle non-exist/deleted attachment correctly. 
3. om.pssd.object.attachment.remove/clear services now call asset.hard.destroy instead of asset.destroy.

### daris-portal:
1. Upgrade mfclientgwt.jar, mfclientguigwt.jar and adesktopplugingwt.jar to 4.2.043. 

### daris-client-pvupload (Bruker client)
1. Enhanced to a) allow auto-subject creation under a parent Project and b) find pre-existing (for NIG meta-data)

### daris-transcoders
1. Bug fix: unclosed file streams when checking for Siemens Microspectroscopy Dicom file in the (DICOM->Siemens RDA) transcoder.
2. version increase to 1.0.1

