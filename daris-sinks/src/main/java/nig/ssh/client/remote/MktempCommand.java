package nig.ssh.client.remote;

import nig.ssh.client.CommandResult;
import nig.ssh.client.Session;

public class MktempCommand implements RemoteCommand {
    private String _template;

    public MktempCommand(String template) {
        _template = template;
    }

    @Override
    public CommandResult execute(Session session) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("temp=");
        sb.append(_template);
        sb.append("; dir=$(dirname $a); if [[ ! -d $dir ]]; then mkdir -p $dir; fi; mktemp $temp");
        return session.execCommand(sb.toString());
    }

}
