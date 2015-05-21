package nig.webdav.client.owncloud;

import nig.webdav.client.UserCredentials;

public class OwncloudClientFactory {
    private OwncloudClientFactory() {
    }

    public static OwncloudClient create(String serverAddress, UserCredentials user) throws Throwable {
        return new OwncloudClient(serverAddress, user);
    }
}
