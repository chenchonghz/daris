proc sink_exists { name } {

    foreach sink [xvalues sink [sink.list]] {
        if { $sink == $name } {
            return 1
        }
    }
    return 0

}

# generic spc sink

if { ! [sink_exists "scp"] } {

    sink.add :name "scp" :destination < :type scp >
    
}

# edward scp sink
if { ! [sink_exists "scp2edward"] } {

    sink.add :name "scp2edward" \
	         :destination < \
                :type scp \
                :arg -name host edward.hpc.unimelb.edu.au \
                :arg -name port 22 \
                :arg -name host-key [xvalue host-key [nig.ssh.host-key.scan :host edward.hpc.unimelb.edu.au :type rsa]] \
                :arg -name decompress true \
             >

}

# cloudstor+ sink
if { ! [sink_exists "cloudstor+"] } {

    sink.add :name "cloudstor+" \
	         :destination < \
                :type owncloud \
                :arg -name url "https://cloudstor.aarnet.edu.au/plus/remote.php/webdav/" \
                :arg -name chunked true \
                :arg -name decompress true \
             >

}

