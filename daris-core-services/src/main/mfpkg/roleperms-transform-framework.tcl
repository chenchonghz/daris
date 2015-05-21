set PKG_TRANSFORM_FRAMEWORK "Transform Framework"

if { [xvalue exists [package.exists :package ${PKG_TRANSFORM_FRAMEWORK}]] == "true" } {
	actor.grant :type role :name daris:pssd.model.user :role -type role transform.user
	actor.grant :type role :name daris:pssd.object.admin :role -type role transform.admin
}