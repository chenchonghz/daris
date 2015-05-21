package nig.webdav.client;

public interface WebdavClientBuilder {

    WebdavClient build() throws Throwable;

    void setUserCredentials(UserCredentials user);
}
