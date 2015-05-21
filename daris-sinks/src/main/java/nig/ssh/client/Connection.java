package nig.ssh.client;

public interface Connection {

    Session connect(UserDetails userDetails, boolean verifyHostKey) throws Throwable;

    void disconnect() throws Throwable;

}
