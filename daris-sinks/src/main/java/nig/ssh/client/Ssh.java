package nig.ssh.client;

import nig.ssh.client.ganymed.GanymedSsh;
import nig.ssh.client.jsch.JSchSsh;

public class Ssh {

    public static final int DEFAULT_PORT = 22;
    public static final Library DEFAULT_SSH_LIBRARY = Library.GANYMED;
    public static final String AUTHORIZED_KEYS_FILE = "~/.ssh/authorized_keys";

    public static enum Library {
        JSCH, GANYMED
    }

    public static SshImpl get(Library lib) {
        if (lib == Library.JSCH) {
            return new JSchSsh();
        } else if (lib == Library.GANYMED) {
            return new GanymedSsh();
        } else {
            return null;
        }
    }

    public static SshImpl get() {
        return get(DEFAULT_SSH_LIBRARY);
    }

}
