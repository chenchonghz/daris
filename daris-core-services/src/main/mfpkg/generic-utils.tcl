proc create_or_replace_http_processor { host url app namespace entry_point domain user } {

    set args ":url ${url}"
    if { ${host} != "" } {
        set args "${args} :host ${host}"
    }
    
    # destroy the http processor if it pre-exists
    if { [xvalue exists [eval "http.processor.exists ${args}"]] == "true" } {
        eval "http.processor.destroy ${args}"
    }
    
    # create the http processor
    if { ${app} != "" } {
        set args "${args} :app ${app}"
    }
    set args "${args} :type asset :translate ${namespace} :authentication < :domain ${domain} :user ${user} > :entry-point ${entry_point}"
    eval "http.processor.create ${args}"

}

proc set_http_servlets { host url servlets } {

    set args ":url ${url}"
    if { ${host} != "" } {
        set args "${args} :host ${host}"
    }
    foreach servlet ${servlets} {
        set path     [lindex $servlet 0]
        set default  [lindex $servlet 1]
        set name     [lindex $servlet 2]
        set args "${args} :servlet -path ${path} -default ${default} ${name}"
    }
    eval "http.servlets.set ${args}"

}
