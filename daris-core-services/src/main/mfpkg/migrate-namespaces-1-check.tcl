set APP "daris"
set PROPERTY_NS_MIGRATE_1 "daris-namespaces-migrate-1"
set NS_MIGRATE_1_DONE [ expr { [xvalue exists [application.property.exists :property -app ${APP} ${PROPERTY_NS_MIGRATE_1}]]=="true" } ]
if { ! $NS_MIGRATE_1_DONE } {
    puts "DaRIS doc type, dictionary and role namespace migration need to be done before installing this package. Please execute the migration script at https://gist.github.com/wliu5/257bdff946c8d7cb86df first."
    error "Namespace migration has not been done."
}
