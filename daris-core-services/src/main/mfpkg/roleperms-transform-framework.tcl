
if { [xvalue exists [package.exists :package "Transform Framework"]] == "true" || [xvalue exists [package.exists :package daris-transform]] == "true" } {
	actor.grant :type role :name daris:pssd.model.user :role -type role transform.user
	actor.grant :type role :name daris:pssd.object.admin :role -type role transform.admin
}
