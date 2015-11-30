
## Change Log:

### daris-core-services
1. Added service: daris.study.copy to copy/send study to another parent subject/ex-method.
2. Added servlets: to display/download dicom/nifti images
3. Added servlets: to view/download archive (image) entries.

### daris-portal:
1. Added form to send a study to another parent subject.
2. Added form to export collection member list as csv or xml file.
3. Set mime type to aar when uploading a local directory as dataset.
4. Fixed: shoppingcart archive type compressed-tar was wrongly fed the service as compressed_tar. 
5. Integrate with Papaya Viewer (in a iframe using the servlets) to display DICOM/NIFTI images.
6. Added URL sharing function to generate a download link for a specific object.

