
## Change Log:

### daris-core-services
1. added service daris.collection.archive.create service for streamed downloads.
2. updated dicom asset engine to add adate to mf-dicom-series
3. enhanced ExMethod create/update interfaces to allow  generic meta-data to be set on the ExMethod. No further integration (with the Method) at this time.

### daris-portal:
1. enabled AAF support in the login dialog.
2. enabled streamed downloads.
3. enabled url-sharing (by generating secure identity token and calling daris.collection.archive.create service).
4. the class IDUtil has been reworked to fully contain all assumptions about CID depths to its internals. These are now exposed only by functions.  When DaRIS moves into its own schema, CID depths will grow by 1 (the schema ID will be included). This is in preparation to handle this situation correctly.

### daris-commons
1. The class nig.mf.pssd.CiteableIDUtil has been reworked to fully contain all assumptions about CID depths to its internals. These are now exposed only by functions.  When DaRIS moves into its own schema, CID depths will grow by 1 (the schema ID will be included). This is in preparation to handle this situation correctly.

