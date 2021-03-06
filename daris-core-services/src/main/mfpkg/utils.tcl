# ============================================================================
# proc: getServerVersion
# args: none
# description: returns the server version 
#
# ============================================================================
proc getServerVersion { } {

	return [xvalue version [server.version]]
}

# ============================================================================
# proc: requireServerVersion
# args: version
# description: throw exception if the server version less than the specified.
#
# ============================================================================
proc requireServerVersion { version } {
    set server_version [xvalue version [server.version]]
    set sv [string map { . "" } $server_version]
    set rv [string map { . "" } $version]
    if { $sv<$rv } {
        error "The server version (${server_version}) is less than the required version (${version})."
    }
}


# ============================================================================
# proc: checkIfServerVersionNoLessThan
# args: version
# description: returns 1 if server version >= specified version. 0 otherwise.
#
# ============================================================================
proc checkIfServerVersionNoLessThan { version } {
	
	set a [string trimleft [lindex [split $version . ] 0] 0]
	set b [string trimleft [lindex [split $version . ] 1] 0]
	set c [string trimleft [lindex [split $version . ] 2] 0]
	set sver [xvalue version [server.version]]
	set sa [string trimleft [lindex [split $sver . ] 0] 0]
	set sb [string trimleft [lindex [split $sver . ] 1] 0]
	set sc [string trimleft [lindex [split $sver . ] 2] 0]
	
	
	if { $a > $sa } {
		return 0;
	}
	
	if { $b > $sb } {
		return 0;
	}
	
	if { $c > $sc } {
		return 0;
	}	
	return 1;
}

# ============================================================================
# proc: getServer
# args: servers
# description: get the server information (name, organization) from the 
#              $servers list, a TCL list in the form of 
#                                        { { $name $uuid $organization } ... }
# ============================================================================
proc getServer { servers } {

    set uuid [xvalue uuid [server.uuid]]
    foreach server $servers {
        if { [lindex $server 1] == $uuid } {
            return $server
        }
    }
    return { "Untitled" $uuid "Unknown Organization" }
    
}

proc getServerName { servers } {
    
    set server [getServer $servers]
    return [lindex $server 0]

}

proc getServerUUID { } {
    
    return [xvalue uuid [server.uuid]]
    
}

proc getServerOrganization { servers } {
    
    set server [getServer $servers]
    return [lindex $server 2]

}

proc getDicomNotificationRecipient { recipients } {
    
    set uuid [getServerUUID]
    foreach recipient $recipients {
        if { [lindex $recipient 0] == $uuid } {
            return [lindex $recipient 1]
        }
    }
    return 0
    
}

# ============================================================================
# proc: loadPlugins
# desc: install plugin.
#
# args: ns - namespace
#       zip - plugin zip file
#       jar - plugin jar file
#       class - plugin module class
#       label - label for the plugin jar asset
# ============================================================================
proc loadPlugin { ns zip jar class label { libs {} } } {
    
	# import the plugin jar from the zip file to Mediaflux system. It will be an
	# asset in the specified namespace with plugin jar file as content.
	asset.import :url archive:${zip} \
		:namespace -create yes ${ns} \
		:label -create yes ${label} :label PUBLISHED \
        :update true

	# install the plugin module
	set args ":path ${ns}/${jar} :class ${class}"
    foreach lib $libs {
       set args "${args} :lib libs/${lib}"
    }
	if { [xvalue exists [plugin.module.exists :path ${ns}/${jar} :class ${class}]] == "false" } {
		plugin.module.add  ${args}
	}
    
	# Now that the plugins have been registered, we need to refresh the known
	# services
	# with this session so that we can grant permissions for those plugins.
	system.service.reload

	# Make the (new) commands available to the enclosing shell.
	srefresh

}

# ============================================================================
# proc: unloadPlugin
# args: path - path to the plugin asset
#       class - plugin module class
# description: uninstall plugin.
# ============================================================================
proc unloadPlugin { path class { libs { } } } {

	# remove the plugin module
	if { [xvalue exists [plugin.module.exists :path ${path} :class ${class}]] == "true" } {
		plugin.module.remove :path ${path} :class ${class}
	}

	# destroy the plugin asset
  	if { [xvalue exists [asset.exists :id name=${path}]] == "true" } {
   		asset.hard.destroy :id name=${path}
   	}

	system.service.reload

	srefresh

}

# ============================================================================
# proc: createDictionary
# args: dictionary - the dictionary to create.
# description: create a dictionary.
# ============================================================================
proc createDictionary { dictionary } {

	if { [xvalue exists [dictionary.exists :name $dictionary]] == "false" } {
		dictionary.create :name $dictionary :case-sensitive true
	}

}

# ============================================================================
# proc: destroyDictionary
# args: dictionary - the dictionary to destroy.
# description: destroy a dictionary.
# ============================================================================
proc destroyDictionary { dictionary } {

	if { [xvalue exists [dictionary.exists :name $dictionary]] == "true" } {
		dictionary.destroy :name $dictionary
	}

}

# ============================================================================
# proc: addDictionaryEntry
# args: dictionary - the dictionary name.
#       term - the term to add
#       definition - the definition to add. (optional)
# description: add a dictionary entry. Add only if the term does not exist.
# ============================================================================
proc addDictionaryEntry { dictionary term { definition 0 } } {
	if { [xvalue exists [dictionary.contains :dictionary $dictionary :term $term]] != "true" } {
		if { $definition != 0 } {
			dictionary.entry.add :dictionary $dictionary :term $term :definition $definition
		} else {
			dictionary.entry.add :dictionary $dictionary :term $term
		}
	}
}

# ============================================================================
# proc: removeDictionaryEntry
# args: dictionary - the dictionary name.
#       term - the term to remove
#       definition - the definition to remove. (optional)
# description: remove a dictionary entry.
# ============================================================================
proc removeDictionaryEntry { dictionary term { definition 0 } } {

	if { [lsearch [xvalues term [dictionary.entries.list :dictionary $dictionary]] $term] != -1 } {
		if { $definition != 0 } {
			dictionary.entry.remove :dictionary $dictionary :term $term :definition $definition
		} else {
			dictionary.entry.remove :dictionary $dictionary :term $term
		}
	}

}

# ============================================================================
# proc: createRole
# args: role - the role name
# description: create an authorization role
# ============================================================================
proc createRole { role } {
	
	authorization.role.create :ifexists ignore :role $role
	
}

# ============================================================================
# proc: destroyRole
# args: role - the role name
# description: destroy an authorization role
# ============================================================================
proc destroyRole { role } {
	
	if { [xvalue exists [authorization.role.exists :role $role]] == "true" } {
		authorization.role.destroy :role $role
	}
	
}

# ============================================================================
# proc: grantPerm
# args: type - the type of the actor to grant perm to
#       name - the name of the actor to grant perm to
#       perm - the perm to grant. it is a tcl list, in the form of
#              { $type $resource $access } e.g. { service asset.get ACCESS }
# ============================================================================
proc grantPerm { type name perm } {

	# retrieve the resource type, name and access from the list
	set rsType     [lindex ${perm} 0]
	set rsName     [lindex ${perm} 1]
	set rsAccess   [lindex ${perm} 2]
	# grant the perm to the actor
	actor.grant :type $type :name $name \
		:perm < :resource -type ${rsType} ${rsName} :access ${rsAccess} >

}

proc grantPerms { type name perms } {
	
	foreach perm $perms {
		grantPerm $type $name $perm
	}
	
}

# ============================================================================
# proc: revokePerm
# args: type - the type of the actor to revoke perm from
#       name - the name of the actor to revoke perm from
#       perm - the perm to revoke. it is a tcl list, in the form of
#              { $type $resource $access } e.g. { service asset.get ACCESS }
# ============================================================================
proc revokePerm { type name perm } {

	# retrieve the resource type, name and access from the list
	set rsType     [lindex ${perm} 0]
	set rsName     [lindex ${perm} 1]
	set rsAccess   [lindex ${perm} 2]
	# grant the perm to the actor
	actor.revoke :type $type :name $name \
		:perm < :resource -type ${rsType} ${rsName} :access ${rsAccess} >

}

proc revokePerms { type name perms } {
	
	foreach perm $perms {
		revokePerm $type $name $perm	
	}

}

# ============================================================================
# proc: grantRole
# args: type - the type of the actor to grant role to
#       name - the name of the actor to grant role to
#       role - the role to grant.
# ============================================================================
proc grantRole { type name role } {

	actor.grant :type $type :name $name :role -type role $role

}

proc grantRoles { type name roles } {
	
	foreach role $roles {
		grantRole $type $name $role
	}
	
}

# ============================================================================
# proc: revokeRole
# args: type - the type of the actor to revoke role from
#       name - the name of the actor to revoke role from
#       role - the role to revoke.
# ============================================================================
proc revokeRole { type name role } {

	actor.revoke :type $type :name $name :role -type role $role

}

# ============================================================================
# proc: revokeRoles
# args: type - the type of the actor to revoke role from
#       name - the name of the actor to revoke role from
#       roles - the list of roles to revoke.
# ============================================================================
proc revokeRoles { type name roles } {
	
	foreach role $roles {
		revokeRole $type $name $role
	}

}

# ============================================================================
# proc: grantRolePerm
# args: role - the role to grant perm to
#       perm - the perm to grant. it is a tcl list, in the form of
#              { $type $resource $access } e.g. { service asset.get ACCESS }
# ============================================================================
proc grantRolePerm { role perm } {

	grantPerm role $role $perm

}

proc grantRolePerms { role perms } {
	
	grantPerms role $role $perms
	
}

proc createRolePerms { role perms } {
    
    createRole $role
    grantRolePerms $role $perms
    
}

proc revokeRolePerm { role perm } {
	
	revokePerm role $role $perm
	
}

proc revokeRolePerms { role perms } {
	
	revokePerms role $role $perms
	
}

proc grantRoleReadAccessService { role service } {
	
	grantRolePerm $role [list service $service ACCESS]
	
}

proc grantRoleReadAccessServices { role services } {
	
	foreach service $services {
		grantRoleReadAccessService $role $service
	}
	
}

proc grantRoleWriteAccessService { role service } {
	
	grantRolePerm $role [list service $service MODIFY]
	
}

proc grantRoleWriteAccessServices { role services } {
	
	foreach service $services {
		grantRoleWriteAccessService $role $service
	}
	
}

proc grantRoleAdminAccessService { role service } {
	
	grantRolePerm $role [list service $service ADMINISTER]
	
}

proc grantRoleReadAccessDocType { role docType } {
	
	grantRolePerm $role [list document $docType ACCESS]
	
}

proc grantRoleReadAccessDocTypes { role docTypes } {
	
	foreach docType $docTypes {
		grantRoleReadAccessDocType $role $docType
	}
	
}

proc grantRoleWriteAccessDocType { role docType } {
	
	grantRolePerm $role [list document $docType PUBLISH]
	
}

proc grantRoleWriteAccessDocTypes { role docTypes } {
	
	foreach docType $docTypes {
		grantRoleWriteAccessDocType $role $docType
	}
	
}

proc grantRoleReadWriteAccessDocType { role docType } {
	
	grantRoleReadAccessDocType $role $docType
	grantRoleWriteAccessDocType $role $docType
	
}

proc grantRoleReadWriteAccessDocTypes { role docTypes } {
	
	foreach docType $docTypes {
		grantRoleReadWriteAccessDocType $role $docType
	}
}

proc revokeRoleReadAccessDocType { role docType } {
	
	revokeRolePerm $role [list document $docType ACCESS]
}

proc revokeRoleWriteAccessDocType { role docType } {
	
	revokeRolePerm $role [list document $docType PUBLISH]
	
}
	
proc revokeRoleReadWriteAccessDocType { role docType } {
	
	revokeRoleReadAccessDocType $role $docType
	revokeRoleWriteAccessDocType $role $docType	
}

proc revokeRoleReadWriteAccessDocTypes { role docTypes } {
	
	foreach docType $docTypes {
		revokeRoleReadWriteAccessDocType $role $docType
	}
	
}


# ============================================================================
# proc: grantRoleRole
# args: role - the role to grant to
#       grole - the role to grant
# description: grant $role with $grole
# ============================================================================
proc grantRoleRole { role grole } {

	grantRole role $role $grole

}

# ============================================================================
# proc: grantServicePerm
# args: service - the plugin service to grant to
#       perm - the perm to grant. in the form of { $type $resource $access }
# description: grant $service with $perm
# ============================================================================
proc grantServicePerm { service perm } {

	grantPerm "plugin:service" $service $perm
	
}

proc grantServicePerms { service perms } {
	
	foreach perm $perms {
		grantServicePerm $service $perm
	}

}

# ============================================================================
# proc: grantServiceRole
# args: service - the plugin service to grant role to.
#       role - the role to grant
# description: grant $service with $role
# ============================================================================
proc grantServiceRole { service role } {

	grantRole "plugin:service" $service $role

}

# ============================================================================
# proc: grantUserRole
# args: name - the name of the user. In the form of $domain:$user or 
#              $authority:$domain:$user
#       role - the role to grant
# description: grant the specified user with $role
# ============================================================================
proc grantUserRole { name role } {

    grantRole "user" $name $role
    
}


# ============================================================================
# proc: revokeUserRole
# args: name - the name of the user. In the form of $domain:$user or 
#              $authority:$domain:$user
#       role - the role to revoke
# description: revoke $role from the specified user.
# ============================================================================
proc revokeUserRole { name role } {

    revokeRole "user" $name $role
    
}





# ============================================================================
# proc: createRelationshipType
# args: type - the relationship type to create
#       inverse - the inverse relationship type. (optional)
# description: create a relationship type.
# ============================================================================
proc createRelationshipType { type { inverse 0 } } {

	if { [xvalue exists [asset.relationship.type.exists :type $type]] == "false" } {
		if { $inverse == 0 } {
			asset.relationship.type.create :type $type
		} else {
			asset.relationship.type.create :type $type :inverse $inverse
		}
	}

}

# ============================================================================
# proc: destroyRelationshipType
# args: type - the relationship type to destroy
# description: destroy a relationship type.
# ============================================================================
proc destroyRelationshipType { type { inverse 0 } } {

	if { [xvalue exists [asset.relationship.type.exists :type $type]] == "true" } {
		asset.relationship.type.destroy :type $type
	}

}

# ============================================================================
# proc: destroyDocType
# args: type - the doc type to destroy
#       force - defaults to false. (optional)
# description: destroy a doc type.
# ============================================================================
proc destroyDocType { type { force "false" } } {

	if { [xvalue exists [asset.doc.type.exists :type $type]] == "true" } {
		asset.doc.type.destroy :type $type :force $force
	}

}

proc setServerProperty { name value } {
    
    server.property.set :property -name $name $value

}

proc setServerIdentity { name organization } {
    
    server.identity.set :name ${name} :organization ${organization}
    
}

# ============================================================================
# proc: createDomain
# args: domain - the authentication domain name to create
# description: creates an authentication domain.
# ============================================================================
proc createDomain { domain } {

    authentication.domain.create :domain ${domain} :ifexists ignore

}

# ============================================================================
# proc: createUser
# args: domain - the authentication domain of the user
#       user - the user to create
# description: creates a limited user tokenin the specified authentication domain.
# no password is given so the user cannot connect (e.g. the DICOM user)
# ============================================================================
proc createUser { domain user } {
        authentication.user.create :domain ${domain} :user ${user} :ifexists ignore
}

# ============================================================================
# proc: createUsers
# args: domain - the authentication domain of the user
#       users - the list of users to create
# description: creates a list of users in the specified authentication domain.
# ============================================================================
proc createUsers { domain users } {

    foreach user $users {
        createUser $domain $user
    }

}

proc addMailNotification { objectType objectName event recipient } {
    
    set r [notification.describe :object -type $objectType $objectName :event $event :method email :recipient $recipient]
    if { [xexists notification $r] == 0 } {
        notification.add :object -type $objectType $objectName :event $event :method email :recipient $recipient
    }
    
}

proc createNamespace { ns description { store 0 } } {
    
    if { $store == 0 } {
        if { [xvalue exists [asset.namespace.exists :namespace $ns]] == "false" } {
            asset.namespace.create :namespace $ns :description $description
        }        
    } else {    
        if { [xvalue exists [asset.store.exists :name $store]] == "true" } {
            if { [xvalue exists [asset.namespace.exists :namespace $ns]] == "false" } {
                asset.namespace.create :namespace $ns :store $store :description $description
            }
        }
    }
    
}

proc revokeAll { type name } {
    
    set r [actor.describe :type $type :name $name]
    set perms [xelements actor/perm $r]
    # Revoke Perms
    set count [xcount perm $perms]
    for { set i 0 } { $i <$count } { incr i } {
        set resource     [xvalue perm\[${i}\]/resource $perms]
        set resourceType [xvalue perm\[${i}\]/resource/@type $perms]
        set access       [xvalue perm\[${i}\]/access $perms]
        actor.revoke :type $type :name $name \
            :perm < :resource -type $resourceType $resource :access $access >
    }
    # Revoke Roles
    set roles [xelements actor/role $r]
    set count [xcount role $roles]
    for { set i 0 } { $i <$count } { incr i } {
        set role     [xvalue role\[${i}\] $roles]
        set roleType [xvalue role\[${i}\]/@type $roles]
        actor.revoke :type $type :name $name \
            :role -type $roleType $role
    }

}

proc createMimeType { type description } {
    
    if { [xvalue exists [type.exists :type $type]] == "false" } {
        type.create :type $type :description $description
    }
    
}

proc removeTranscodeProvider { from to } {
    puts "Removing trancode provider ${from} - ${to}..."
    if {[xvalue exists [transcode.provider.exists :from $from :to $to]] == "true" } {        
        transcode.provider.remove :from $from :to $to
    }
    
}

proc removeTranscodeProviders { providers } {
    
    foreach provider $providers {
        set from [lindex $provider 0]
        set to   [lindex $provider 1]
        removeTranscodeProvider $from $to
    }

}


# ============================================================================
# Method management functions
# Function to determine if a Method pre-exists by name.
# If more than one, returns the first
#
proc getMethodId { methodName } {
    set methodId ""
    set r [om.pssd.method.find :name $methodName]
    set n [xcount id $r]
    # If more than one will take first
    return [xvalue id $r] 
}

# 
# Set arguments for Method update service 
#
# id The citable ID of the Method, if it pre-exists
# fillIn fill in citeable allocator space when creating methods
# action   Controls what to do if the Method pre-exists (i.e. id is non-null)
#          If the Method does not pre-exist, a new one will always be made
#          0 - means do nothing.
#          1 - means replace it with the new specification
#          2 - means make a new one anyway
# return   Returns 'quit' if no further action should be taken
#          Otherwise returns arguments to be handed to the 
#            om.pssd.method.for.subject.update service
#
proc setMethodUpdateArgs { id { action 0 } { fillIn 0 }} {
    set margs ""
    if  { $id != "" } {
		if { $action == "0" } {
		    # Do nothing to pre-existing
		    return "quit"
		} elseif { $action == "2" } {
		    # Create new
		    set margs ""
		} elseif { $action == "1" } {
		    # Replace existing
		    set margs ":replace 1 :id ${id}"
		} else {
		    # Return do nothing
		    return "quit"
		}
    } else {
		# Create new
    }
    if { $fillIn == "1" } {
       set margs  "${margs} :fillin true"
    } else {
       set margs  "${margs} :fillin false"
    }
       
    return ${margs}
}

proc checkAppProperty { app prop msg } {
   set ok "false"
   set exists [xvalue exists [application.property.exists :property -app $app $prop]]
   if { $exists == "true" } {
      set pv [xvalue property [application.property.get :property -app $app $prop]]
      if { $pv == "true" } {
         set ok "true"
      } 
   }
   if { $ok == "false" } {
      error $msg
   }
}

# Try and figure out if DaRIS has been installed
# The absence of PSSD objects is not sufficient
proc isDaRISInstalled { } {

   set exists [xvalue exists [authorization.role.exists :role pssd.model.user]]
   if { $exists == "true" } {
     return "true"
   }
   set exists [xvalue exists [authorization.role.exists :role daris:pssd.model.user]]
   return $exists
}



# Version 6
