proc requireMfluxVersion { version } {
    set server_version [xvalue version [server.version]]
    set sv [string map { . "" } $server_version]
    set rv [string map { . "" } $version]
    if { $sv<$rv } {
        error "The server version (${server_version}) is less than the required version (${version})."
    }
}
requireMfluxVersion @REQUIRE_MFLUX_VERSION@

