proc isDarisInstalled { } {
    return [expr { [expr { [xvalue exists [authorization.role.exists :role pssd.model.user]] == "true" }] || [expr { [xvalue exists [authorization.role.exists :role daris:pssd.model.user]] == "true" }] }]
}
