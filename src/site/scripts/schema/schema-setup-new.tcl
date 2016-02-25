# create the schema as system:manager
schema.create :name daris

# switch to the schema as system:manager
schema.set :schema daris

# bind schema with host name daris.vicnode.org.au
schema.member.add :schema daris :member < :host daris.vicnode.org.au >

# switch to the schema
schema.set :schema daris

##
## In the new schema
##
# create authentication domain
authentication.domain.create :domain system

# create system:manager user
authentication.user.create :domain system :user manager :password change_me

# create system-administrator role
authorization.role.create :role system-administrator

# grant system-administrator role with * access
actor.grant :type role :name system-administrator :perm < :resource -type * * :access * >

# install mflux.zip package Failed....
# to work around it, unpack mflux.zip edit __install.tcl:
# add :host argument to http.processor.create call
# repack and install

# install www.zip package failed...
# create role read-only then install

