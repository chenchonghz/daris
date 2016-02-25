set MANAGER_PASSWD change_me8

if { [xvalue schema/name [schema.self.describe]] != "daris" } {
    puts "Not in daris schema. Quit"
    return
}

# create system:manager account
authentication.domain.create :ifexists ignore :domain system
authentication.user.create :ifexists ignore :domain system :user manager :password $MANAGER_PASSWD

# create system-administrator role
authorization.role.create :ifexists ignore :role system-administrator

# grant perms for system-administrator role
actor.grant :type role :name system-administrator :perm < :resource -type * * :access * >

# create read-only role
authorization.role.create :ifexists ignore :role read-only

# grant perms for read-only role
actor.grant :type role :name read-only \
    :perm < :access ACCESS :resource -type document * > \
    :perm < :access ACCESS :resource -type document:namespace * > \
    :perm < :access ACCESS :resource -type service * > \
    :perm < :access MODIFY :resource -type service "asset.filter.*" > \
    :perm < :access MODIFY :resource -type service "asset.licence.accept" > \
    :perm < :access MODIFY :resource -type service "user.self.*" >
