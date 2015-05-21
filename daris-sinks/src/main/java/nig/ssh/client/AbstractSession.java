package nig.ssh.client;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import nig.ssh.client.BackgroundCommand.ResponseHandler;
import nig.util.StringUtil;

public abstract class AbstractSession implements Session {

    private UserDetails _userDetails;

    protected AbstractSession(UserDetails userDetails) {
        _userDetails = userDetails;
    }

    protected UserDetails userDetails() {
        return _userDetails;
    }

    @Override
    public BackgroundCommand execBackground(String command, ResponseHandler rh) throws Throwable {
        BackgroundCommand rc = new BackgroundCommand(this, command, rh);
        Thread thread = new Thread(rc);
        thread.start();
        return rc;
    }

    @Override
    public int exec(String command) throws Throwable {
        return exec(command, null, null, null);
    }

    @Override
    public CommandResult execCommand(String command) throws Throwable {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        CommandResult result = null;
        try {
            open();
            int exitStatus = exec(command, out, err, null);
            result = new CommandResult(exitStatus, out.toString(), err.toString());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean commandExists(String command) throws Throwable {
        return execCommand("which " + command).exitStatus == 0;
    }

    @Override
    public String getHome() throws Throwable {
        String home = execCommand("echo $HOME").output;
        if (home != null) {
            return StringUtil.trimTrailing(home, "\n", true);
        } else {
            return home;
        }
    }

    @Override
    public void installPublicKey(String publicKey) throws Throwable {
        installPublicKey(new AuthorizedKey(publicKey));
    }

    public void installPublicKey(AuthorizedKey ak) throws Throwable {
        if (ak.comment() == null) {
            String username = _userDetails.username();
            String host = InetAddress.getLocalHost().getHostAddress();
            ak.setComment(username + "@" + host);
        }
        try {
            open();
            StringBuilder cmd = new StringBuilder();
            cmd.append("if [[ ! -f " + Ssh.AUTHORIZED_KEYS_FILE + " ]]; then ");
            cmd.append("echo '" + ak.toString() + "' >> " + Ssh.AUTHORIZED_KEYS_FILE + "; ");
            cmd.append("elif [[ -z $(cat " + Ssh.AUTHORIZED_KEYS_FILE + " | grep '" + ak.key()
                    + "') ]]; then ");
            cmd.append("cp " + Ssh.AUTHORIZED_KEYS_FILE + " $(mktemp " + Ssh.AUTHORIZED_KEYS_FILE
                    + ".bak.XXXXX); ");
            cmd.append("echo '" + ak.toString() + "' >> " + Ssh.AUTHORIZED_KEYS_FILE + "; ");
            cmd.append("fi; ");
            cmd.append("chmod 600 " + Ssh.AUTHORIZED_KEYS_FILE);
            exec(cmd.toString());
        } finally {
            close();
        }
    }

    @Override
    public void mkdir(String path, boolean parents, String mode) throws Throwable {
        if (path == null || path.trim().length() == 0) {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (mode != null && !FileMode.isValid(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("mkdir ");
        if (parents) {
            sb.append("-p ");
        }
        if (mode != null) {
            sb.append("-m ");
            sb.append(mode);
            sb.append(" ");
        }
        sb.append('"');
        sb.append(path);
        sb.append('"');
        String command = sb.toString();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitStatus = exec(command, null, err, null);
        String msg = new String(err.toByteArray());
        if (!msg.isEmpty()) {
            // throw new RuntimeException("Remote command error:" + msg);
            System.out.println("Error from remote SSH server: " + msg);
        }
        if (exitStatus != 0) {
            StringBuilder ex = new StringBuilder("Remote command exit status=" + exitStatus + ". ");
            if (!msg.isEmpty()) {
                ex.append("Error from remote SSH server: " + msg);
            }
            throw new RuntimeException(ex.toString());
        }
    }

    @Override
    public String mktemp(String template) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("temp=");
        sb.append(template);
        sb.append("; dir=$(dirname $temp); if [[ ! -d $dir ]]; then mkdir -p $dir; fi; mktemp $temp");
        return execCommand(sb.toString()).output;
    }

    public static void main(String[] args) throws Throwable {
        // AuthorizedKey ak = new AuthorizedKey(
        // "AAAAB3NzaC1yc2EAAAADAQABAAACAQCzUMQ2F/PCax0sfkU6MqUb9N3Nc4KW5L25sOYo+O668QVXRBA8Xvb512sKMDejhysiTzj/PyYnF3AOt8WOEcyhUdBG3r4Ni2DVEw9E8SwUQfBKxwzhWrw9Wae/q3/q1SIhT3OdiS+unyfb+fFdr52Qanx7HwPX0y7ZHiZb81FavAbs/QmaGqgNdPqpNTKcqB3F88jhzaO1QEZGYZuXDOajwkQHRWxImliFQGBl/MRA3zl22lEywi90DACy0AiDgiM6Tb3uxjsyW09sCNmYskygDSAV7OmLHdzcJm0sequeoO6qZdgRJ9ApOKBd/S1dBsv641G4NsT2HLqw/VJ2tVfl5h+Y1fs2cAXP37xGRfZt9Cc2RGxF3Y0qKmO3/qdG4IwvxnDJvhWEbW+EbX+bh7qTNeASQ2FGAxRltmNmxtoWRqtK2qIO/JSmP1xnDQRXCX3JZVUn2cYKlvHn57a/ld7HU4UU6gya+JoiR1M6D+T98g3PQbBygto3md9Tb+MzABxRbigqn2urg8SyCdJxVOHA9VLyXLr+hXryWUO2YNWirSqevCe8L2TW2Q/wiwNGBsktxqx3iTccdhI+JhdbXH2ICUk2pMJIbSfg6kEFeXkpViV3WluVS3w1xo2I4M1iMXe0d05u2ysBxIJ5A54q9cp5+fIfW01FtqCs+NRaL3znew==");
        // StringBuilder cmd = new StringBuilder();
        // cmd.append("if [[ ! -f " + Ssh.AUTHORIZED_KEYS_FILE + " ]]; then ");
        // cmd.append("echo '" + ak.toString() + "' >> " + Ssh.AUTHORIZED_KEYS_FILE + "; ");
        // cmd.append("elif [[ -z $(cat " + Ssh.AUTHORIZED_KEYS_FILE + " | grep '" + ak.key() +
        // "') ]]; then ");
        // cmd.append("cp " + Ssh.AUTHORIZED_KEYS_FILE + " $(mktemp " + Ssh.AUTHORIZED_KEYS_FILE +
        // ".bak.XXXXX); ");
        // cmd.append("echo '" + ak.toString() + "' >> " + Ssh.AUTHORIZED_KEYS_FILE + "; ");
        // cmd.append("fi; ");
        // cmd.append("chmod 600 " + Ssh.AUTHORIZED_KEYS_FILE);
        // System.out.println(cmd.toString());

        // Session session = Ssh.get().getConnection(new
        // ServerDetails("edward.hpc.unimelb.edu.au")).connect(new
        // UserDetails("UUUUUU", "XXXXXX"), false);
        // System.out.println(session.getHome());
        // System.out.println(session.execCommand("uname -m"));
    }
}
