# ============================================================================
# Service Permissions
# ============================================================================
actor.grant :type plugin:service :role -type role service-user :name nig.ssh.host-key.scan
actor.grant :type plugin:service :role -type role service-user :name nig.ssh.public-key.push
actor.grant :type plugin:service :role -type role service-user :name nig.user.self.sink.settings.get
actor.grant :type plugin:service :role -type role service-user :name nig.user.self.sink.settings.set
actor.grant :type plugin:service :role -type role service-user :name nig.sink.describe

