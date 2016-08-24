# create role namespace if needed
authorization.role.namespace.create :namespace daris :ifexists ignore :description "Namespace for daris framework roles"

# basic-user comes from essentials package
grantRoleReadAccessServices  daris:basic-user { system.session.self.describe system.session.output.get }
    
# ============================================================================
# Role: pssd.model.doc.user
# ============================================================================
createRole    daris:pssd.model.doc.user
grantRoleReadWriteAccessDocTypes daris:pssd.model.doc.user \
    { daris:pssd-object   daris:pssd-filename  daris:pssd-project        daris:pssd-subject \
      daris:pssd-ex-method daris:pssd-study          daris:pssd-dataset \
      daris:pssd-transform daris:pssd-acquisition    daris:pssd-derivation \
      daris:pssd-method    daris:pssd-method-subject daris:pssd-method-rsubject \
      daris:pssd-notification daris:pssd-project-harvest daris:pssd-project-owner \
      daris:pssd-project-governance daris:pssd-project-research-category \
      daris:pssd-publications daris:pssd-related-services \
      daris:pssd-role-member-registry \
      daris:pssd-dicom-server-registry \
      daris:pssd-shoppingcart-layout-pattern daris:pssd-dicom-ingest daris:dicom-dataset}

# Revoke excessively permissive access to all document namespaces from earlier versions.
actor.revoke :type role :name daris:pssd.model.doc.user :perm < :access ACCESS :resource -type document:namespace "*" >

# This gives access to the global document namespace.  Other domain-specific packages
# must supply access to their own document namespaces. 
# changed from all to global in stable-2-20 (Jan 2013)
actor.grant :type role :name daris:pssd.model.doc.user :perm < :access ACCESS :resource -type document:namespace ":" >

# Grant end users the right to access the daris document namespace
actor.grant :name  daris:pssd.model.doc.user :type role :perm < :resource -type document:namespace daris :access ACCESS >

# ============================================================================
# Role: pssd.model.user
# ============================================================================
createRole    daris:pssd.model.user

grantRoleReadAccessServices   daris:pssd.model.user \
    { daris.project.metadata.harvest \
      om.pssd.* \
      daris.* \
      nig.* \
      asset.model.* \
      actor.have \
      asset.acl.have \
      asset.content.get \
      asset.doc.namespace.list \
      asset.doc.type.describe \
      asset.doc.type.list \
      asset.doc.template.as.xml \
      asset.meta.transform.profile.describe \
      asset.namespace.get \
      asset.namespace.describe \
      asset.path.generate \
      asset.transcode.describe \
      authentication.domain.* \
      authentication.user.* \
      citeable.named.id.describe \
      citeable.name.list \
      dictionary.contains \
      user.exists user.describe user.self.describe \
      system.events.* \
      server.ping \
      system.logon \
      system.logoff \
      shopping.cart.* \
      asset.doc.type.exists \
      service.background.describe \
      server.version server.database.describe \
      package.describe package.list package.exists \
      type.list \
      type.describe \
      type.ext.types \
      user.self.* \
      secure.wallet.* \
      secure.shell.* \
      secure.identity.token.* \
	  sink.* \
      actor.self.* \
      dictionary.*  }
grantRoleWriteAccessServices  daris:pssd.model.user \
    { daris.project.metadata.harvest \
      om.pssd.*  \
      daris.*  \
      nig.* \
      asset.create \
      asset.destroy \
      asset.set \
      citeable.id.create \
	  service.background.abort \
	  shopping.cart.* \
	  server.io.job.create \
	  server.io.write \
	  server.io.write.finish \
	  server.log \
      server.task.named.begin \
      server.task.named.end \
      authorization.role.create \
      user.self.* \
      secure.wallet.* \
      secure.shell.* \
      secure.identity.token.* \
      actor.self.* \
      dictionary.* }
grantRoleAdminAccessService daris:pssd.model.user actor.revoke
grantRoleRole daris:pssd.model.user daris:pssd.model.doc.user
    
    
# Now grant the pssd.model.user access to  'basic-user' and 'federation-user'
# which are defined in the essentials package
# Although a user could be supplied the 'basic-user' role at the top level, and this
# would then give them this basic access, our only real context for delivering
# this role is via PSSD so we bundle it here.  
# The hierarchy is user : {nig.pssd.model.user, pssd.model.user : {basic user, federation user}} but could be
# user : {nig.pssd.model.user, pssd.model.user, basic-user}
grantRoleRole daris:pssd.model.user daris:basic-user
grantRoleRole daris:pssd.model.user daris:federation-user

# Grant pssd.model.user access to the "daris-tags" dictionary namespace. 
# Because any user could be a project admin (and hence manage a tag library)
# all users get ADMINISTER priviledge
actor.grant :name daris:pssd.model.user :type role :perm < :access ADMINISTER :resource -type dictionary:namespace daris-tags >

# Grant model user standard access to the daris dictionary namespace
actor.grant :name daris:pssd.model.user :type role :perm < :access ACCESS :resource -type dictionary:namespace daris >

##########################################################################
# These specialized services grant roles to other roles and users
# They need to have system-administrator role to do this

grantRole plugin:service om.pssd.user.role.grant system-administrator
grantRole plugin:service om.pssd.user.role.revoke system-administrator
grantRole plugin:service om.pssd.user.revoke system-administrator

grantRole plugin:service om.pssd.project.create system-administrator
grantRole plugin:service om.pssd.project.update system-administrator
grantRole plugin:service om.pssd.project.members.add system-administrator
grantRole plugin:service om.pssd.project.members.remove system-administrator
grantRole plugin:service om.pssd.project.members.replace system-administrator

# These are deprecated (we don't deal with R-Subjects any more)
grantRole plugin:service om.pssd.r-subject.admin.add system-administrator
grantRole plugin:service om.pssd.r-subject.admin.remove system-administrator
grantRole plugin:service om.pssd.r-subject.guest.add system-administrator
grantRole plugin:service om.pssd.r-subject.guest.remove system-administrator

# These services were coded, but are not currently deployed in the plugin module. Also deprecated.
#grantRole plugin:service om.pssd.subject.role.grant system-administrator
#grantRole plugin:service om.pssd.subject.role.revoke system-administrator
##########################################################################


# ============================================================================
# Role: pssd.model.power.user : pssd.model.user
#
# These people have some extra rights to enable them to explore more of
# the system from aterm. Should be granted directly to a user
# ============================================================================
createRole    daris:pssd.model.power.user
actor.grant :type role :name daris:pssd.model.power.user :role -type role daris:pssd.model.user
grantRolePerms   daris:pssd.model.power.user \
{ { service dictionary.add MODIFY } \
  { service dictionary.destroy  MODIFY } \
  { service dictionary.entry.add MODIFY } \
  { service dictionary.entry.remove MODIFY } \
  { service asset.doc.type.create MODIFY } \
  { service asset.doc.type.destroy MODIFY } \
  { service server.log.display ADMINISTER } \
}

# ============================================================================
# Role: pssd.project.create
# ============================================================================
createRole daris:pssd.project.create
grantRolePerms daris:pssd.project.create \
    { { service authentication.user.exists ACCESS } \
      { service citeable.named.id.create   MODIFY } \
      { service user.authority.grant       ADMINISTER } }

# ============================================================================
# Role: pssd.subject.create
# ============================================================================
createRole daris:pssd.subject.create
grantRolePerms daris:pssd.subject.create \
    { { service citeable.named.id.create MODIFY } }

# ============================================================================
# Role: pssd.r-subject.admin
# ============================================================================
createRole daris:pssd.r-subject.admin
grantRolePerms daris:pssd.r-subject.admin \
    { { service citeable.named.id.create MODIFY } }

# ============================================================================
# Role: pssd.r-subject.guest
# ============================================================================
createRole daris:pssd.r-subject.guest

# ============================================================================
# Role: pssd.object.guest
# ============================================================================
createRole daris:pssd.object.guest

# ============================================================================
# Role: pssd.object.admin
# ============================================================================
createRole daris:pssd.object.admin
# grant pssd.object.admin role to system-administrator role 
# (so that the system administrators can access the meta data).
actor.grant :type role :name system-administrator :role -type role daris:pssd.object.admin

# ============================================================================
# Role: pssd.administrator 
#
# Holders of this role should be able to undertake DaRIS admin activities
# without the full power of system:administrator.  Admin services
# require permission ADMINISTER to operate. Also grants the essentials
# package administrator role.
# ============================================================================
createRole daris:pssd.administrator

# Grant pssd.administrator access to the daris role namespace

# Grant other roles
actor.grant :name daris:pssd.administrator :type role :perm < :access ADMINISTER :resource -type role:namespace daris >
actor.grant :name daris:pssd.administrator :type role  \
      :role -type role daris:pssd.object.admin \
      :role -type role daris:pssd.model.power.user \
      :role -type role daris:pssd.project.create \
      :role -type role daris:essentials.administrator \
      :role -type role daris:pssd.subject.create
      
# These services need ADMINISTER to be able to execute
grantRolePerms daris:pssd.administrator  \
    { { service om.pssd.announcement.create ADMINISTER } \
      { service om.pssd.announcement.destroy ADMINISTER } \
      { service om.pssd.dataset.move ADMINISTER } \
      { service om.pssd.study.rename ADMINISTER } \
      { service om.pssd.dicom.study.retrofit ADMINISTER } \
      { service om.pssd.user.create ADMINISTER }\
      { service om.pssd.project.method.replace ADMINISTER } \
      { service om.pssd.subject.method.replace ADMINISTER } \
      { service om.pssd.ex-method.method.replace ADMINISTER } \
      { service om.pssd.study.method.template.replace ADMINISTER } \
      { service om.pssd.role-member-registry.add ADMINISTER } \
      { service om.pssd.role-member-registry.destroy ADMINISTER } \
      { service om.pssd.role-member-registry.remove ADMINISTER } \
      { service om.pssd.doctype.rename ADMINISTER } \
      { service om.pssd.method.create ADMINISTER } \
      { service om.pssd.method.destroy ADMINISTER } \
      { service om.pssd.method.update ADMINISTER } \
      { service om.pssd.replicate.check ADMINISTER } \
      { service om.pssd.replicate ADMINISTER } \
      { service om.pssd.study.type.create ADMINISTER } \
      { service om.pssd.study.type.destroy ADMINISTER } \
      { service om.pssd.study.type.destroy.all ADMINISTER } \
      { service daris.* ADMINISTER } \
    }
    
# Allow the pssd.administrator to administer the document namespace
actor.grant :name  daris:pssd.model.doc.user :type role :perm < :resource -type document:namespace daris :access ADMINISTER >

# Allow pssd.administrator to administer the daris dictionary namespace
# The daris-tags dictionary namespace is handled earlier
actor.grant :name daris:pssd.administrator :type role :perm < :access ADMINISTER :resource -type dictionary:namespace daris >

# Allow the pssd.administrator to administer the daris role namespace
#actor.grant :name daris:pssd.administrator :type role :perm < :access ADMINISTER :resource -type role:namespace daris >

    
# ============================================================================
# Role: pssd.dicom-ingest
# ============================================================================
createRole daris:pssd.dicom-ingest
grantRolePerms daris:pssd.dicom-ingest \
    { { service asset.get ACCESS } \
      { service asset.doc.type.describe            ACCESS } \
      { service asset.doc.template.as.xml          ACCESS } \
      { service asset.query                        ACCESS } \
      { service citeable.root.get                  ACCESS } \
      { service dicom.metadata.get                 ACCESS } \
      { service om.pssd.collection.member.list     ACCESS } \
      { service om.pssd.object.exists              ACCESS } \
      { service om.pssd.object.describe            ACCESS } \
      { service om.pssd.object.type                ACCESS } \
      { service om.pssd.object.destroy             MODIFY } \
      { service om.pssd.subject.create             MODIFY } \
      { service om.pssd.subject.clone              MODIFY } \
      { service om.pssd.subject.update             MODIFY } \
      { service om.pssd.subject.method.find        ACCESS } \
      { service om.pssd.ex-method.study.step.find  ACCESS } \
      { service om.pssd.ex-method.step.study.find  ACCESS } \
      { service om.pssd.study.create               MODIFY } \
      { service om.pssd.study.update               MODIFY } \
      { service om.pssd.dataset.primary.create     MODIFY } \
      { service om.pssd.dataset.primary.update     MODIFY } \
      { service om.pssd.dataset.derivation.create  MODIFY } \
      { service om.pssd.dataset.derivation.update  MODIFY } \
      { service om.pssd.ex-method.step.describe    ACCESS } \
      { service om.pssd.project.members.list       MODIFY } \
      { service om.pssd.role-member-registry.list  MODIFY } \
      { service user.describe                      ACCESS } \
      { service user.self.describe                 ACCESS } \
      { service server.log                         MODIFY } \
      { service om.pssd.project.mail.send          ACCESS } \
      { service actor.have 			               ACCESS } \
      { service actor.describe		         	   ACCESS } \
      { service citeable.id.exists                 ACCESS } \
      { service citeable.id.import                 MODIFY } \
      { service citeable.named.id.create           MODIFY } \
      { service system.session.self.describe       ACCESS } \
      { service server.identity                    ACCESS } \
      { service dicom.metadata.populate            MODIFY } \
      { document daris:pssd-role-member-registry   ACCESS } \
    }
    
        
# Give the DICOM server access to all the things users have access to.
grantRoleRole daris:pssd.dicom-ingest daris:pssd.model.doc.user

# THe DICOM server must have access to all PSSD objects
grantRoleRole daris:pssd.dicom-ingest daris:pssd.object.admin

# ============================================================================
# Parallel approach to user roles.  We will define this structure
# dicom-user : {nig.pssd.dicom-ingest, pssd.dicom-ingest : dicom-ingest} but could be
# user : {nig.pssd.dicom-ingest, pssd.dicom-ingest, dicom-ingest}
# dicom-ingest is created in the essentials package
grantRoleRole daris:pssd.dicom-ingest dicom-ingest

# ============================================================================
# Grant daris:pssd.dicom-ingest role ADMINISTER access to role namespace daris:
# This must be done. Otherwise user.describe called by dicom proxy user holding 
# daris:pssd.dicom-ingest role will not return the roles in namespace daris.
actor.grant :type role :name daris:pssd.dicom-ingest :perm < :resource -type role:namespace daris: :access ADMINISTER > 

# ============================================================================
# Detect if "Transform framework" is installed. If it is, set the roles...
# ============================================================================
source role-permissions-transform.tcl
