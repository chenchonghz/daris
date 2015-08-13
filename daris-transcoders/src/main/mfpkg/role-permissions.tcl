# ============================================================================
# grant daris:basic-user role access to plugin services
# ============================================================================
if { [xvalue exists [authorization.role.exists :role daris:basic-user]] == "true" } {
    actor.grant :type role :name daris:basic-user :perm < :resource -type service daris.transcode.* :access ACCESS >
}
