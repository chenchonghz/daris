# These are for PSSD data
# Store must pre-exist (generally configured after Mediaflux server install
# and before packages are installed
set pssd_store            pssd
set pssd_namespace        pssd
set pssd_method_namespace pssd/methods
set pssd_fcp_namespace    pssd/fcp

# If the store does not exist, this function will do nothing
# If the namespace pre-exists, it does nothing
createNamespace $pssd_namespace        "pssd namespace"         $pssd_store
createNamespace $pssd_method_namespace "pssd methods namespace"
createNamespace $pssd_fcp_namespace "pssd file compilation profiles"
