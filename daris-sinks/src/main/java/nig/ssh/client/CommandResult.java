package nig.ssh.client;

public class CommandResult {
    
    public final int exitStatus;
    public final String output;
    public final String error;
    
    public CommandResult(int exitStatus, String output, String error) {
        this.exitStatus = exitStatus;
        this.output = output;
        this.error = error;
    }

}
