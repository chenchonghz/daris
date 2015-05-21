package nig.ssh.client;

public interface Abortable {

    boolean aborted();

    void abort();

}
