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
      daris:pssd-shoppingcart-layout-pattern daris:pssd-dicom-ingest }

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
      asset.meta.transform.profile.describe \
      asset.namespace.get \
      asset.namespace.describe \
      asset.path.generate \
      asset.transcode.describe \
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
      type.ext.types \
      user.self.* \
      secure.wallet.* \
      secure.shell.* \
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
      { service om.pssd.study.move ADMINISTER } \
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
# Service: om.pssd.project.create
# ============================================================================
grantServicePerms om.pssd.project.create \
    { { service actor.self.grant ADMINISTER } \
      { service actor.grant      ADMINISTER } \
      { service user.grant      ADMINISTER }}
grantServiceRole  om.pssd.project.create service-user

# ============================================================================
# Service: om.pssd.project.update
# ============================================================================
grantServicePerms om.pssd.project.update \
    { { service actor.grant ADMINISTER } \
      { service actor.revoke ADMINISTER }
      { service user.grant ADMINISTER }
      { service user.revoke ADMINISTER }}
grantServiceRole  om.pssd.project.update service-user

# ============================================================================
# Service: om.pssd.project.members.replace
# ============================================================================
grantServicePerms om.pssd.project.members.replace \
    { { service actor.grant ADMINISTER } \
      { service actor.revoke ADMINISTER } \
      { service user.grant ADMINISTER } \
      { service user.revoke ADMINISTER } }
grantServiceRole  om.pssd.project.members.replace service-user


# ============================================================================
# Service: om.pssd.project.members.remove
# ============================================================================
grantServicePerms om.pssd.project.members.remove \
    { { service actor.revoke ADMINISTER } \
      { service user.revoke ADMINISTER }}
grantServiceRole  om.pssd.project.members.remove service-user

# ============================================================================
# Service: om.pssd.project.members.add
# ============================================================================
grantServicePerms om.pssd.project.members.add \
    { { service actor.grant ADMINISTER } \
      { service user.grant ADMINISTER }}
grantServiceRole  om.pssd.project.members.add service-user

# ============================================================================
# Service: om.pssd.project.members.list
# ============================================================================
grantServicePerms om.pssd.project.members.list \
    { { service actor.describe ACCESS } \
      { service user.describe ACCESS }}
grantServiceRole  om.pssd.project.members.list service-user

# ============================================================================
# Service: om.pssd.project.mail.send
# ============================================================================
grantServiceRole  om.pssd.project.mail.send service-user

# ============================================================================
# Service: om.pssd.r-subject.create
# ============================================================================
grantServicePerms om.pssd.r-subject.create \
    { { service actor.grant ADMINISTER } }
grantServiceRole  om.pssd.r-subject.create service-user

# ============================================================================
# Service: om.pssd.subject.create
# ============================================================================
grantServiceRole  om.pssd.subject.create service-user

# ============================================================================
# Service: om.pssd.subject.update
# ============================================================================
grantServiceRole  om.pssd.subject.update service-user

# ============================================================================
# Service: om.pssd.study.create
# ============================================================================
grantServiceRole  om.pssd.study.create service-user

# ============================================================================
# Service: om.pssd.study.update
# ============================================================================
grantServiceRole  om.pssd.study.update service-user

# ============================================================================
# Service: om.pssd.dataset.primary.create
# ============================================================================
grantServiceRole  om.pssd.dataset.primary.create service-user

# ============================================================================
# Service: om.pssd.dataset.primary.update
# ============================================================================
grantServiceRole  om.pssd.dataset.primary.update service-user

# ============================================================================
# Service: om.pssd.dataset.derivation.create
# ============================================================================
grantServiceRole  om.pssd.dataset.derivation.create service-user

# ============================================================================
# Service: om.pssd.dataset.derivation.update
# ============================================================================
grantServiceRole  om.pssd.dataset.derivation.update service-user

# ============================================================================
# Service: om.pssd.dataset.derivation.find
# ============================================================================
grantServicePerms om.pssd.dataset.derivation.find { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.describe
# ============================================================================
grantServiceRole  om.pssd.object.describe daris:pssd.r-subject.guest
grantServicePerms om.pssd.object.describe { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.lock
# ============================================================================
grantServiceRole  om.pssd.object.lock service-user

# ============================================================================
# Service: om.pssd.object.unlock
# ============================================================================
grantServiceRole  om.pssd.object.unlock service-user

# ============================================================================
# Service: om.pssd.object.session.lock
# ============================================================================
grantServiceRole  om.pssd.object.session.lock service-user

# ============================================================================
# Service: om.pssd.object.session.unlock
# ============================================================================
grantServiceRole  om.pssd.object.session.unlock service-user

# ============================================================================
# Service: om.pssd.object.exists
# ============================================================================
grantServiceRole om.pssd.object.exists daris:pssd.object.admin

# ============================================================================
# Service: om.pssd.object.type
# ============================================================================
grantServicePerms om.pssd.object.type { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.attachment.add
# ============================================================================
grantServiceRole  om.pssd.object.attachment.add service-user

# ============================================================================
# Service: om.pssd.object.attachment.get
# ============================================================================
grantServicePerms om.pssd.object.attachment.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.attachment.list
# ============================================================================
grantServicePerms om.pssd.object.attachment.list { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.attachment.remove
# ============================================================================
grantServiceRole  om.pssd.object.attachment.remove service-user

# ============================================================================
# Service: om.pssd.object.thumbnail.set
# ============================================================================
grantServiceRole  om.pssd.object.thumbnail.set service-user

# ============================================================================
# Service: om.pssd.object.thumbnail.get
# ============================================================================
grantServicePerms om.pssd.object.thumbnail.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.thumbnail.image.get
# ============================================================================
grantServicePerms om.pssd.object.thumbnail.image.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.thumbnail.unset
# ============================================================================
grantServiceRole  om.pssd.object.thumbnail.unset service-user

# ============================================================================
# Service: om.pssd.object.update
# ============================================================================
grantServiceRole  om.pssd.object.update service-user

# ============================================================================
# Service: om.pssd.object.destroy
# ============================================================================
grantServiceRole  om.pssd.object.destroy service-user

# ============================================================================
# Service: om.pssd.object.find
# ============================================================================
grantServicePerms om.pssd.object.find { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.icon.get
# ============================================================================
grantServicePerms om.pssd.object.icon.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.user.describe
# ============================================================================
grantServicePerms om.pssd.user.describe { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.collection.members
# ============================================================================
grantServicePerms om.pssd.collection.members { { service * ACCESS } }



actor.grant :type plugin:service :role -type role service-user :name om.pssd.ex-method.step.transform.find
actor.grant :type plugin:service :role -type role service-user :name om.pssd.transform.find

# ============================================================================
#
# SHOPPINGCART SERVICES
#
# ============================================================================
grantServiceRole om.pssd.shoppingcart.content.add service-user
grantServiceRole om.pssd.shoppingcart.content.clear service-user
grantServiceRole om.pssd.shoppingcart.content.list  service-user
grantServiceRole om.pssd.shoppingcart.content.remove service-user
grantServiceRole om.pssd.shoppingcart.create service-user
grantServicePerms om.pssd.shoppingcart.create { { service application.property.* ADMINISTER } }
grantServiceRole om.pssd.shoppingcart.describe  service-user
grantServicePerms om.pssd.shoppingcart.describe { { service application.property.* ADMINISTER } }
grantServiceRole om.pssd.shoppingcart.destination.list service-user
grantServiceRole om.pssd.shoppingcart.destroy  service-user
grantServiceRole om.pssd.shoppingcart.exists  service-user

grantServiceRole om.pssd.shoppingcart.layout-pattern.add  service-user
grantServicePerms om.pssd.shoppingcart.layout-pattern.add { { service application.property.* ADMINISTER } }
grantServiceRole om.pssd.shoppingcart.layout-pattern.list  service-user
grantServicePerms om.pssd.shoppingcart.layout-pattern.list { { service application.property.* ADMINISTER } }
grantServiceRole om.pssd.shoppingcart.layout-pattern.remove  service-user
grantServicePerms om.pssd.shoppingcart.layout-pattern.remove { { service application.property.* ADMINISTER } }

grantServiceRole om.pssd.shoppingcart.order  service-user
grantServicePerms om.pssd.shoppingcart.order { { service application.property.* ADMINISTER } }
grantServiceRole om.pssd.shoppingcart.template.create   service-user
grantServiceRole om.pssd.shoppingcart.template.destroy  service-user


# ============================================================================
# Service: om.pssd.dicom.*
# ============================================================================
grantServiceRole  om.pssd.dicom.ae.list service-user
grantServiceRole  om.pssd.dicom.send service-user
grantServiceRole  om.pssd.dicom.dataset.count service-user
grantServiceRole  om.pssd.dicom.anonymize service-user

# ============================================================================
# Service: om.pssd.announcement.*
# ============================================================================
grantServiceRole  om.pssd.announcement.create service-user
grantServiceRole  om.pssd.announcement.describe service-user
grantServiceRole  om.pssd.announcement.destroy service-user
grantServiceRole  om.pssd.announcement.list service-user

# ============================================================================
# Service: om.pssd.object.tag.*
# ============================================================================
grantServiceRole  om.pssd.object.tag.add service-user
grantServiceRole  om.pssd.object.tag.describe service-user
grantServiceRole  om.pssd.object.tag.exists service-user
grantServiceRole  om.pssd.object.tag.list service-user
grantServiceRole  om.pssd.object.tag.remove service-user
grantServiceRole  om.pssd.object.tag.remove.all service-user
grantServiceRole  om.pssd.object.tag.dictionary.create service-user
grantServiceRole  om.pssd.object.tag.dictionary.destroy service-user
grantServiceRole  om.pssd.object.tag.dictionary.entry.add service-user
grantServiceRole  om.pssd.object.tag.dictionary.entry.list service-user
grantServiceRole  om.pssd.object.tag.dictionary.entry.remove service-user
grantServiceRole  om.pssd.object.tag.dictionary.get service-user
grantServiceRole  om.pssd.object.tag.dictionary.global.create service-user
grantServiceRole  om.pssd.object.tag.dictionary.global.destroy service-user
grantServiceRole  om.pssd.object.tag.dictionary.global.entry.add service-user
grantServiceRole  om.pssd.object.tag.dictionary.global.entry.list service-user
grantServiceRole  om.pssd.object.tag.dictionary.global.entry.remove service-user


# ============================================================================
# Service: om.pssd.type.metadata.list
# ============================================================================
grantServiceRole  om.pssd.type.metadata.list service-user


# ============================================================================
# Service: om.pssd.user.can.* 
# ============================================================================
grantServiceRole  om.pssd.user.can.access   service-user
grantServiceRole  om.pssd.user.can.create   service-user
grantServiceRole  om.pssd.user.can.destroy  service-user
grantServiceRole  om.pssd.user.can.modify   service-user

# ============================================================================
# Service: om.pssd.dataset.processed.*
# ============================================================================
grantServiceRole  om.pssd.dataset.processed.count     service-user
grantServiceRole  om.pssd.dataset.processed.destroy   service-user
grantServiceRole  om.pssd.dataset.processed.exists    service-user
grantServiceRole  om.pssd.dataset.processed.destroyable.count     service-user
grantServiceRole  om.pssd.dataset.processed.destroyable.exists    service-user
grantServiceRole  om.pssd.dataset.processed.input.list            service-user
grantServiceRole  om.pssd.dataset.unprocessed.list                service-user

# ============================================================================
# Detect if "Transform framework" is installed. If it is, set the roles...
# ============================================================================
source roleperms-transform-framework.tcl
