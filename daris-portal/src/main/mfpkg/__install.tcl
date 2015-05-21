##############################################################################
#                                                                            #
#                               Installer Script                             #
#                                      for                                   #
#                           Mediaflux Package: daris                         #
#                                                                            #
##############################################################################
proc checkAppProperty { app prop msg } {
   set ok "false"
   set exists [xvalue exists [application.property.exists :property -app $app $prop]]
   if { $exists == "true" } {
      set pv [xvalue property [application.property.get :property -app $app $prop]]
      if { $pv == "true" } {
         set ok "true"
      } 
   }
   puts "ok = $ok"
   if { $ok == "false" } {
      error $msg
   }
}


#
# Check server version requirements
#
source requireServerVersion.tcl

# Daris underwent a namespace migration (stable-2-27).   Make sure this has been done
# as the current version assumes this.
checkAppProperty daris daris-namespaces-migrate-1 "DaRIS namespaces migration has not been done. You must undertake this migration first by installing stable-2-27"


#
# Clean up the old namespace if it exists
#
if { [xvalue exists [asset.namespace.exists :namespace /www/daris]] == "true" } {
	asset.namespace.destroy :force true :namespace /www/daris
}


#
#
#
set label [string toupper PACKAGE_$package]


#
# Set up authentication
#
#set domain www-public
set domain mflux
#set user www-public
set user public
if { [xvalue exists [authentication.domain.exists :domain ${domain}]] == "false" } {
	authentication.domain.create :domain ${domain}
	authentication.user.create :domain ${domain} :user ${user}
}


#
# Set up namespace
#
set namespace /www/adesktop/Applications/daris
if { [xvalue exists [asset.namespace.exists :namespace ${namespace}]] == "true" } {
	puts "Installing package ${package} -- Destroying existing namespace: ${namespace}" 
	asset.namespace.destroy :namespace ${namespace}	
}
puts "Installing package ${package} -- Creating namespace: ${namespace}" 
asset.namespace.create :namespace -all true ${namespace} :description "the namespace for daris web application"

#
# Import web contents
#
puts "Installing package ${package} -- Importing web contents to namespace: ${namespace}"
asset.import :url archive:///www.zip :namespace ${namespace} :label -create yes ${label} :label PUBLISHED :update true


#
# Install as standalone web application: Set up HTTP processor
#
set url /daris
set entry_point DaRIS.html
if { [xvalue exists [http.processor.exists :url ${url}]] == "false" } {
	puts "Installing package ${package} -- Creating http processor: url=${url}"
	http.processor.create :url ${url} \
		              :app daris \
		              :type asset \
		              :translate ${namespace} \
		              :authentication < :domain $domain :user $user > \
		              :entry-point ${entry_point}
}

#
# Install as Arcitecta Desktop plugin application
#
asset.set :create true :id path=${namespace}/desktop-app.xml :in archive:desktop-app.xml :type application/adesktop
asset.set :create true :id path=${namespace}/desktop-icon.png :in archive:desktop-icon.png :label PUBLISHED
