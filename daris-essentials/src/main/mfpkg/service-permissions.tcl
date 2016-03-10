actor.grant :type plugin:service :name dicom.metadata.csv.export :role -type role service-user
actor.grant :type plugin:service :name dicom.metadata.populate :role -type role service-user
actor.grant :type plugin:service :name nig.secure.wallet.key.generate :role -type role service-user
#actor.grant :type plugin:service :name daris.dicom.send :perm < :access ACCESS :resource -type service * >
actor.grant :type plugin:service :name daris.dicom.download :perm < :access ACCESS :resource -type service * >
