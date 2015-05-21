package nig.ssh.client;

import java.io.InputStream;
import java.io.OutputStream;

public interface Session {

    int exec(String command, OutputStream stdout, OutputStream stderr, Abortable abort) throws Throwable;

    int exec(String command) throws Throwable;

    CommandResult execCommand(String command) throws Throwable;

    BackgroundCommand execBackground(String command, BackgroundCommand.ResponseHandler rh) throws Throwable;

    OutputStream scpPut(String remoteFile, long length, String mode) throws Throwable;

    InputStream scpGet(String remoteFile) throws Throwable;

    void mkdir(String path, boolean parents, String fileMode) throws Throwable;
    
    String mktemp(String template) throws Throwable;

    boolean commandExists(String command) throws Throwable;

    String getHome() throws Throwable;

    void open() throws Throwable;

    void close() throws Throwable;

    void installPublicKey(String publicKey) throws Throwable;

    void installPublicKey(AuthorizedKey publicKey) throws Throwable;

}
