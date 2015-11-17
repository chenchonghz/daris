# service: daris.repository.description.set
actor.grant :role -type role service-user :type plugin:service :name daris.repository.description.set

# service: daris.repository.description.get
actor.grant :role -type role service-user :type plugin:service :name daris.repository.description.get

# service: daris.repository.describe
actor.grant :role -type role service-user :type plugin:service :name daris.repository.describe

# service: om.pssd.project.create
actor.grant :type plugin:service :name om.pssd.project.create :perm < :access ADMINISTER :resource -type service actor.self.grant >
actor.grant :type plugin:service :name om.pssd.project.create :perm < :access ADMINISTER :resource -type service actor.grant >
actor.grant :type plugin:service :name om.pssd.project.create :perm < :access ADMINISTER :resource -type service user.grant >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.create

# service: om.pssd.project.update
actor.grant :type plugin:service :name om.pssd.project.update :perm < :access ADMINISTER :resource -type service actor.grant >
actor.grant :type plugin:service :name om.pssd.project.update :perm < :access ADMINISTER :resource -type service actor.revoke >
actor.grant :type plugin:service :name om.pssd.project.update :perm < :access ADMINISTER :resource -type service user.grant >
actor.grant :type plugin:service :name om.pssd.project.update :perm < :access ADMINISTER :resource -type service user.revoke >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.update

# service: om.pssd.project.members.replace
actor.grant :type plugin:service :name om.pssd.project.members.replace :perm < :access ADMINISTER :resource -type service actor.grant >
actor.grant :type plugin:service :name om.pssd.project.members.replace :perm < :access ADMINISTER :resource -type service actor.revoke >
actor.grant :type plugin:service :name om.pssd.project.members.replace :perm < :access ADMINISTER :resource -type service user.grant >
actor.grant :type plugin:service :name om.pssd.project.members.replace :perm < :access ADMINISTER :resource -type service user.revoke >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.members.replace

# service: om.pssd.project.members.remove
actor.grant :type plugin:service :name om.pssd.project.members.remove :perm < :access ADMINISTER :resource -type service actor.revoke >
actor.grant :type plugin:service :name om.pssd.project.members.remove :perm < :access ADMINISTER :resource -type service user.revoke >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.members.remove

# service: om.pssd.project.members.add
actor.grant :type plugin:service :name om.pssd.project.members.add :perm < :access ADMINISTER :resource -type service actor.grant >
actor.grant :type plugin:service :name om.pssd.project.members.add :perm < :access ADMINISTER :resource -type service user.grant >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.members.add

# service: om.pssd.project.members.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.members.list

# service: om.pssd.project.mail.send
actor.grant :role -type role service-user :type plugin:service :name om.pssd.project.mail.send

# service: om.pssd.r-subject.create
actor.grant :type plugin:service :name om.pssd.r-subject.create :perm < :access ADMINISTER :resource -type service actor.grant >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.r-subject.create

# service: om.pssd.subject.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.subject.create

# service: om.pssd.subject.update
actor.grant :role -type role service-user :type plugin:service :name om.pssd.subject.update

# service: om.pssd.study.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.study.create

# service: om.pssd.study.update
actor.grant :role -type role service-user :type plugin:service :name om.pssd.study.update

# service: om.pssd.dataset.primary.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.primary.create

# service: om.pssd.dataset.primary.update
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.primary.update

# service: om.pssd.dataset.derivation.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.derivation.create

# service: om.pssd.dataset.derivation.update
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.derivation.update

# service: om.pssd.dataset.derivation.find
actor.grant :type plugin:service :name om.pssd.dataset.derivation.find :perm < :access ACCESS :resource -type service * >

# service: om.pssd.object.describe
actor.grant :role -type role daris:pssd.r-subject.guest :type plugin:service :name om.pssd.object.describe
actor.grant :type plugin:service :name om.pssd.object.describe :perm < :access ACCESS :resource -type service * >

# service: om.pssd.object.lock
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.lock

# service: om.pssd.object.unlock
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.unlock

# service: om.pssd.object.session.lock
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.session.lock

# service: om.pssd.object.session.unlock
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.session.unlock

# service: om.pssd.object.exists
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.exists

# service: om.pssd.object.type
actor.grant :type plugin:service :name om.pssd.object.type :perm < :access ACCESS :resource -type service * >

# service: om.pssd.object.attachment.add
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.attachment.add

# service: om.pssd.object.attachment.get
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.attachment.get

# service: om.pssd.object.attachment.list
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.attachment.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.attachment.list

# service: om.pssd.object.attachment.remove
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.attachment.remove

# service: om.pssd.object.thumbnail.set
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.thumbnail.set

# service: om.pssd.object.thumbnail.get
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.thumbnail.get

# service: om.pssd.object.thumbnail.image.get
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.thumbnail.image.get

# service: om.pssd.object.thumbnail.unset
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.thumbnail.unset

# service: om.pssd.object.update
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.update

# service: om.pssd.object.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.destroy

# service: om.pssd.object.find
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.find

# service: om.pssd.object.icon.get
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.object.icon.get

# service: om.pssd.user.describe
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.user.describe

# service: om.pssd.collection.members
actor.grant :perm < :access ACCESS :resource -type service * > :type plugin:service :name om.pssd.collection.members

# service: om.pssd.ex-method.step.transform.find
actor.grant :role -type role service-user :type plugin:service :name om.pssd.ex-method.step.transform.find

# service: om.pssd.transform.find
actor.grant :role -type role service-user :type plugin:service :name om.pssd.transform.find

# service: om.pssd.shoppingcart.content.add
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.content.add

# service: om.pssd.shoppingcart.content.add 
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.content.add

# service: om.pssd.shoppingcart.content.clear
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.content.clear

# service: om.pssd.shoppingcart.content.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.content.list

# service: om.pssd.shoppingcart.content.remove
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.content.remove

# service: om.pssd.shoppingcart.create
actor.grant :type plugin:service :name om.pssd.shoppingcart.create :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.create

# service: om.pssd.shoppingcart.describe
actor.grant :type plugin:service :name om.pssd.shoppingcart.describe :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.describe

# service: om.pssd.shoppingcart.destination.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.destination.list 

# service: om.pssd.shoppingcart.destroy 
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.destroy

# service: om.pssd.shoppingcart.exists
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.exists

# service: om.pssd.shoppingcart.layout-pattern.add
actor.grant :type plugin:service :name om.pssd.shoppingcart.layout-pattern.add :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.layout-pattern.add

# service: om.pssd.shoppingcart.layout-pattern.list
actor.grant :type plugin:service :name om.pssd.shoppingcart.layout-pattern.list :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.layout-pattern.list

# service: om.pssd.shoppingcart.layout-pattern.remove
actor.grant :type plugin:service :name om.pssd.shoppingcart.layout-pattern.remove :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.layout-pattern.remove

# service:  om.pssd.shoppingcart.order 
actor.grant :type plugin:service :name om.pssd.shoppingcart.order :perm < :access ADMINISTER :resource -type service application.property.* >
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.order

# service: om.pssd.shoppingcart.template.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.template.create

# service: om.pssd.shoppingcart.template.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.shoppingcart.template.destroy

# service: om.pssd.dicom.ae.list 
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dicom.ae.list

# service: om.pssd.dicom.send
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dicom.send

# service: om.pssd.dicom.dataset.count
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dicom.dataset.count

# service: om.pssd.dicom.anonymize
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dicom.anonymize

# service: daris.dicom.local.ae.title.list 
actor.grant :role -type role service-user :type plugin:service :name daris.dicom.local.ae.title.list

# service: om.pssd.announcement.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.announcement.create

# service: om.pssd.announcement.describe
actor.grant :role -type role service-user :type plugin:service :name om.pssd.announcement.describe

# service: om.pssd.announcement.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.announcement.destroy

# service: om.pssd.announcement.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.announcement.list

# service: om.pssd.object.tag.add
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.add

# service: om.pssd.object.tag.describe
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.describe

# service: om.pssd.object.tag.exists
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.exists

# service: om.pssd.object.tag.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.list

# service: om.pssd.object.tag.remove
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.remove

# service: om.pssd.object.tag.remove.all
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.remove.all

# service: om.pssd.object.tag.dictionary.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.create

# service: om.pssd.object.tag.dictionary.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.destroy

# service: om.pssd.object.tag.dictionary.entry.add
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.entry.add

# service: om.pssd.object.tag.dictionary.entry.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.entry.list

# service: om.pssd.object.tag.dictionary.entry.remove
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.entry.remove

# service: om.pssd.object.tag.dictionary.get
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.get

# service: om.pssd.object.tag.dictionary.global.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.global.create

# service: om.pssd.object.tag.dictionary.global.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.global.destroy

# service: om.pssd.object.tag.dictionary.global.entry.add
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.global.entry.add

# service: om.pssd.object.tag.dictionary.global.entry.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.global.entry.list

# service: om.pssd.object.tag.dictionary.global.entry.remove
actor.grant :role -type role service-user :type plugin:service :name om.pssd.object.tag.dictionary.global.entry.remove

# service: om.pssd.type.metadata.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.type.metadata.list

# service: om.pssd.user.can.access
actor.grant :role -type role service-user :type plugin:service :name om.pssd.user.can.access

# service: om.pssd.user.can.create
actor.grant :role -type role service-user :type plugin:service :name om.pssd.user.can.create

# service: om.pssd.user.can.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.user.can.destroy

# service: om.pssd.user.can.modify
actor.grant :role -type role service-user :type plugin:service :name om.pssd.user.can.modify

# service: om.pssd.dataset.processed.count
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.count

# service: om.pssd.dataset.processed.destroy
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.destroy

# service: om.pssd.dataset.processed.exists
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.exists

# service: om.pssd.dataset.processed.destroyable.count
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.destroyable.count

# service: om.pssd.dataset.processed.destroyable.exists
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.destroyable.exists

# service: om.pssd.dataset.processed.input.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.processed.input.list

# service: om.pssd.dataset.unprocessed.list
actor.grant :role -type role service-user :type plugin:service :name om.pssd.dataset.unprocessed.list

actor.grant :role -type role service-user :type plugin:service :name daris.archive.content.list

actor.grant :role -type role service-user :type plugin:service :name daris.archive.content.get

actor.grant :role -type role service-user :type plugin:service :name daris.archive.content.image.get

actor.grant :role -type role service-user :type plugin:service :name daris.dicom.archive.content.get


