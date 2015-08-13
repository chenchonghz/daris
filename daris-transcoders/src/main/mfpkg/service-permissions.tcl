# ============================================================================
# grant service-user role to plugin services
# ============================================================================
actor.grant :role -type role service-user :type plugin:service :name daris.transcode
actor.grant :role -type role service-user :type plugin:service :name daris.transcode.activate
actor.grant :role -type role service-user :type plugin:service :name daris.transcode.list
actor.grant :role -type role service-user :type plugin:service :name daris.transcode.provider.describe


