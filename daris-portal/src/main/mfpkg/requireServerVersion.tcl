set requiredVersion @requiredVersion@
set currentVersion [xvalue version [server.version]]
regsub -all {\.} $requiredVersion {} requiredVerNum
regsub -all {\.} $currentVersion {} currentVerNum
	
if { $requiredVerNum > $currentVerNum } {
	error "This package requires Mediaflux server ${requiredVersion} or higher. Found Mediaflux ${currentVersion}."
}
