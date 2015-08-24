# ============================================================================
# role namespace: daris
#    The role namespace for daris.
# ============================================================================
authorization.role.namespace.create :namespace daris :ifexists ignore \
                                    :description "Namespace for daris roles"

# ============================================================================
# role: daris:basic-user
#     This role enables some basic access to Mediaflux functionality such as
#     an account, access to DICOM doc types and some functions,  and simple 
#     asset access.
# ============================================================================
createRole daris:basic-user
grantRoleReadWriteAccessDocTypes daris:basic-user \
    { mf-name mf-note mf-user mf-document mf-image }  
grantRoleReadAccessDocTypes daris:basic-user  { mf-revision-history }
#
grantRoleReadAccessServices  daris:basic-user \
   { actor.self.describe \
     asset.doc.type.describe \
     asset.get asset.query asset.namespace.list asset.namespace.exists asset.transcode \
     citeable.root.get citeable.id.exists \
     dictionary.entries.list  dictionary.entries.describe  \
     server.peer.exists server.identity server.uuid \
     sink.list transcode.describe   user.self.get  \
     user.self.settings.get network.self.describe \
     system.session.task.abort \
   }
#
grantRoleWriteAccessServices  daris:basic-user { user.self.password.set citeable.id.import }

# Used mainly by clients, but also some services.
grantRoleReadWriteAccessDocTypes daris:basic-user { daris:bruker-study daris:bruker-series \
                                              daris:siemens-raw-petct-study daris:siemens-raw-petct-series \
                                              daris:siemens-raw-mr-study \
                                              daris:dicom-dataset daris:dicom-series }

# ============================================================================
# Role: federation-user
# We may wish to federate MF servers but independently of the PSSD data model 
# So we create a federation role at this lowest level and grant it to the
# pssd.model.user in the PSSD package
# ============================================================================
set federation_perms        { { service system.session.federate MODIFY } \
			      { service system.session.self.describe ACCESS } \
			      { service server.peer.describe ACCESS } \
			      { service server.peer.tag.list ACCESS } \
			      { service server.peer.status MODIFY } \
			      { service asset.exists ACCESS } \
			      { peer * ACCESS } }
createRole daris:federation-user
grantRolePerms daris:federation-user ${federation_perms} 

# Grant access to plugin services
set svc_perms { { service nig.actors.have.role ACCESS } \
                { service nig.asset.cid.get ACCESS } \
	         	{ service nig.asset.dmget MODIFY } \
                { service nig.asset.dmput MODIFY } \
                { service nig.asset.id.get ACCESS } \
                { service nig.ip.resolve ACCESS } \
                { service nig.secure.wallet.key.generate ACCESS } }
grantRolePerms daris:basic-user $svc_perms

#
# Revoke these permissions that were once given to basic-user
# If they are not held it does not matter (on re-install)
#
revokeRolePerms daris:basic-user  \
    { { service nig.asset.doc.element.rename ADMINISTER } \
      { service nig.asset.doc.element.remove ADMINISTER } \
      { service nig.asset.doc.element.replace ADMINISTER } \
      { service nig.asset.doc.element.copy ADMINISTER } \
      { service nig.asset.doc.namespace.replace ADMINISTER } \
      { service nig.asset.doc.remove ADMINISTER } \
      { service nig.asset.doc.name.replace ADMINISTER } \
      { service nig.asset.doc.string.replace ADMINISTER } \
      { service nig.asset.doc.copy ADMINISTER } \
      { service nig.asset.doc.name.replace ADMINISTER } \
      { service nig.asset.pid.set ADMINISTER } \
      { service dicom.model.fix ADMINISTER } \
      { service nig.replicate.check ADMINISTER } \
      { service nig.replicate.synchronize ADMINISTER } }


# ============================================================================
# Role: nig.essentials.administrator 
#
# Holders of this role should be able to undertake essentials admin activities
# without the full power of system:administrator.  Admin services
# require permission ADMINISTER to operate. The PSSD package will further
# grant this role to the pssd.administration role
# ============================================================================
createRole daris:essentials.administrator
actor.grant :name daris:essentials.administrator :type role  \
      :role -type role daris:basic-user
      
# These services need ADMINISTER to be able to execute
grantRolePerms daris:essentials.administrator  \
    { { service nig.asset.doc.element.rename ADMINISTER } \
      { service nig.asset.doc.element.remove ADMINISTER } \
      { service nig.asset.doc.element.replace ADMINISTER } \
      { service nig.asset.doc.element.copy ADMINISTER } \
      { service nig.asset.doc.element.date.fix ADMINISTER } \
      { service nig.asset.doc.namespace.replace ADMINISTER } \
      { service nig.asset.doc.remove ADMINISTER } \
      { service nig.asset.doc.name.replace ADMINISTER } \
      { service nig.asset.doc.string.replace ADMINISTER } \
      { service nig.asset.doc.copy ADMINISTER } \
      { service nig.asset.doc.name.replace ADMINISTER } \
      { service nig.asset.pid.set ADMINISTER } \
      { service dicom.model.fix ADMINISTER } \
      { service nig.replicate.check ADMINISTER } \
      { service nig.replicate.synchronize ADMINISTER } }


# ============================================================================
# DICOM data model permissions.  Could pull out into own DICOM installable package
grantRoleReadWriteAccessDocTypes daris:basic-user \
    { mf-dicom-patient mf-dicom-subject mf-dicom-prefs mf-dicom-project\
      mf-dicom-study mf-dicom-series } 

# Native MF  services
grantRoleReadAccessServices  daris:basic-user  { dicom.image.get dicom.metadata.get }
grantRoleWriteAccessServices  daris:basic-user  { dicom.ingest }
 
# nig-essentials services
actor.grant :type plugin:service :name dicom.metadata.csv.export :role -type role service-user
actor.grant :type plugin:service :name dicom.metadata.populate :role -type role service-user
actor.grant :type plugin:service :name nig.secure.wallet.key.generate :role -type role service-user

grantRoleReadAccessServices  daris:basic-user { dicom.send dicom.study.find dicom.metadata.csv.export }
grantRoleWriteAccessServices daris:basic-user { dicom.model.fix dicom.metadata.grab dicom.header.edit dicom.metadata.populate }

# ============================================================================
# role: dicom-ingest
# This role is created by the Mediaflux server (on install) and comes with some
# default permissions so don't destroy it. Then we add some new permissions.
# This role enables access for the default (non-PSSD) DICOM server
# Like basic-user, it's really a layer undeneath PSSD and could go
# in its own package if we needed to
# ============================================================================
set dicom_ingest_perms  {     { service  asset.exists          ACCESS } \
			      { service  asset.get             ACCESS } \
			      { service  asset.doc.type.exists ACCESS } \
			      { service  server.uuid           ACCESS } \
			      { service  mail.send             MODIFY } \
			      { service  notification.generate MODIFY } \
			      { document mf-dicom-patient      ACCESS } \
			      { document mf-dicom-subject      PUBLISH } \
			      { document mf-dicom-series       ACCESS } }
grantRolePerms dicom-ingest $dicom_ingest_perms
      
