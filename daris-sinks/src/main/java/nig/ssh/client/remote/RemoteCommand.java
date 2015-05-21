package nig.ssh.client.remote;

import nig.ssh.client.CommandResult;
import nig.ssh.client.Session;

public interface RemoteCommand {

    CommandResult execute(Session session) throws Throwable;
}
