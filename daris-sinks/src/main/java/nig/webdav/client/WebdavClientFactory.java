package nig.webdav.client;

public class WebdavClientFactory {

    private WebdavClientFactory() {
    }

    public static WebdavClient create(String serverAddress, UserCredentials user) throws Throwable {
        return new WebdavClientImpl(serverAddress, user);
    }

}
