package nig.webdav.client;

import java.io.InputStream;

public interface WebdavClient {

    InputStream get(String remotePath) throws Throwable;

    void put(InputStream in, long length, String remotePath) throws Throwable;

    void mkdir(String remotePath, boolean parents) throws Throwable;

    void delete(String remotePath) throws Throwable;

}
