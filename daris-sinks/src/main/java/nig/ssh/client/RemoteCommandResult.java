package nig.ssh.client;

public class RemoteCommandResult {
    
    public final int exitStatus;
    public final String output;
    public final String error;
    
    public RemoteCommandResult(int exitStatus, String output, String error) {
        this.exitStatus = exitStatus;
        this.output = output;
        this.error = error;
    }

}
