package nig.ssh.client;

public interface SshImpl {

    Connection getConnection(ServerDetails serverDetails) throws Throwable;

    String getServerHostKey(String host, int port, Algorithm algorithm) throws Throwable;

    String libraryName();
}
