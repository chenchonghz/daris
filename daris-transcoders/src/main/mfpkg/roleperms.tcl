# Grant access to plugin services
# the basic-user role comes from the essentials package
# which must be installed first
set svc_perms { { service nig.transcode ACCESS } }
grantRolePerms daris:basic-user $svc_perms
