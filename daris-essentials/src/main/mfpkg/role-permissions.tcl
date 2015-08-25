proc grant_role_doc_access { role docs { accesses { ACCESS } } } {
    foreach doc $docs {
        foreach access $accesses {
            actor.grant :type role :name "$role" :perm < :resource -type document $doc :access $access >
        }
    }
}

proc grant_role_service_access { role services { access ACCESS } } {
    foreach service $services {
        actor.grant :type role :name "$role" :perm < :resource -type service $service :access $access >
    }
}

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

authorization.role.create :ifexists ignore :role daris:basic-user

# doc permissions for role daris:basic-user
grant_role_doc_access daris:basic-user { \
    mf-name \
    mf-note \
    mf-user \
    mf-document \
    mf-image \
    mf-dicom-patient \
    mf-dicom-subject \
    mf-dicom-prefs \
    mf-dicom-project \
    mf-dicom-study \
    mf-dicom-series \
    daris:bruker-study \
    daris:bruker-series \
    daris:siemens-raw-petct-study \
    daris:siemens-raw-petct-series \
    daris:siemens-raw-mr-study \
    daris:dicom-dataset \
    daris:dicom-series } { ACCESS PUBLISH }

grant_role_doc_access daris:basic-user { \
    mf-revision-history } { ACCESS }

# service permissions for role daris:basic-user
grant_role_service_access daris:basic-user { \
    actor.self.describe \
    asset.doc.type.describe \
    asset.get \
    asset.namespace.list \
    asset.namespace.exists \
    asset.query \
    asset.transcode \
    citeable.root.get \
    citeable.id.exists \
    dictionary.entries.list \
    dictionary.entries.describe \
    network.self.describe \
    server.peer.exists \
    server.identity \
    server.uuid \
    sink.list \
    system.session.task.abort \
    transcode.describe \
    user.self.get \
    user.self.settings.get \
    dicom.* \
    nig.* \
    daris.* } ACCESS
 
grant_role_service_access daris:basic-user { \
    citeable.id.import \
    user.self.password.set \
    dicom.* \
    nig.* \
    daris.* } MODIFY 

# ============================================================================
# role: daris:federation-user
#     We may wish to federate MF servers but independently of the PSSD data 
#     model. So we create a federation role at this lowest level and grant it 
#     to the pssd.model.user in the PSSD package
# ============================================================================
authorization.role.create :ifexists ignore :role daris:federation-user

# service permissions for daris:federation-user
grant_role_service_access daris:federation-user { \
    system.session.federate \
    server.peer.status } MODIFY

grant_role_service_access daris:federation-user {
    system.session.self.describe \
    server.peer.describe \
    server.peer.tag.list \
    asset.exists } ACCESS

actor.grant :type role :name daris:federation-user :perm < :resource -type peer * :access ACCESS >

# ============================================================================
# role: daris:essentials.administrator 
#     Holders of this role should be able to undertake essentials admin 
#     activities without the full power of system:administrator.  Admin 
#     services require permission ADMINISTER to operate. The PSSD package 
#     will further grant this role to the pssd.administration role
# ============================================================================
authorization.role.create :ifexists ignore :role daris:essentials.administrator

# grant daris:basic-user role to daris:essentials.administrator
actor.grant :name daris:essentials.administrator :type role :role -type role daris:basic-user

# grant access to (administrative) services
grant_role_service_access daris:essentials.administrator { dicom.* nig.* } ADMINISTER 

# ============================================================================
# role: dicom-ingest
#     This role is created by the Mediaflux server (on install) and comes 
#     with some default permissions so don't destroy it. Then we add some 
#     new permissions. This role enables access for the default (non-PSSD) 
#     DICOM server. Like basic-user, it's really a layer undeneath PSSD and 
#     could go in its own package if we needed to.
# ============================================================================
grant_role_doc_access dicom-ingest { mf-dicom-patient mf-dicom-series } { ACCESS }
grant_role_doc_access dicom-ingest { mf-dicom-subject } { PUBLISH }
grant_role_service_access dicom-ingest { asset.exists asset.get asset.doc.type.exists server.uuid } ACCESS 
grant_role_service_access dicom-ingest { mail.send notification.generate } MODIFY 
