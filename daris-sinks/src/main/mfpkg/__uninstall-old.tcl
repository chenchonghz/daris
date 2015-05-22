##
## NOTE: this is uninstall the old package 'nig-sinks'. The new package is named 'daris-sinks'.
##

# uninstall nig-sinks package if it exists.
if { [xvalue exists [package.exists :package nig-sinks]] == "true" } {
    package.uninstall :package nig-sinks
}

# remove the library assets if they exist
set path /mflux/plugins/bcprov-jdk16-140.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/commons-codec-1.8.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/commons-logging-1.1.3.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/ganymed-ssh2-261.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/hamcrest-core-1.3.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/httpclient-4.3.2.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/httpcore-4.3.1.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/sardine-5.1.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}

set path /mflux/plugins/slf4j-api-1.7.5.jar
if { [xvalue exists [asset.exists :id path=${path}]] == "true" } {
    asset.destroy :id path=${path}
}
