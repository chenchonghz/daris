# Grant access to plugin services
# the basic-user role comes from the essentials package
# which must be installed first
set svc_perms { { service nig.image.get ACCESS } \
                { service nig.image.metadata.get ACCESS }
              }
grantRolePerms daris:basic-user $svc_perms

# Used mainly by clients, but also some services.
# the basic-user role comes from the essentials package
grantRoleReadWriteAccessDocTypes daris:basic-user { daris:nifti-1 }
 
