##
## NOTE: this is uninstall the old package 'daris'. The new package is named 'daris-portal'.
##

# uninstall daris package if it exists.
if { [xvalue exists [package.exists :package daris]] == "true" } {
    package.uninstall :package daris
}

