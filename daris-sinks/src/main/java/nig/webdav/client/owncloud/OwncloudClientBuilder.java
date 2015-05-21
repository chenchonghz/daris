package nig.webdav.client.owncloud;

import nig.util.PathUtil;
import nig.webdav.client.UserCredentials;
import nig.webdav.client.WebdavClient;
import nig.webdav.client.WebdavClientBuilder;
import nig.webdav.client.WebdavClientFactory;

public class OwncloudClientBuilder implements WebdavClientBuilder {

    public static final String WEBDAV_PATH_1_2 = "/webdav/owncloud.php";
    public static final String WEBDAV_PATH_2_0 = "/files/webdav.php";
    public static final String WEBDAV_PATH_4_0 = "/remote.php/webdav";
    public static final String ODAV_PATH = "/remote.php/odav";
    public static final String SAML_SSO_PATH = "/remote.php/webdav";
    public static final String CARDDAV_PATH_2_0 = "/apps/contacts/carddav.php";
    public static final String CARDDAV_PATH_4_0 = "/remote/carddav.php";
    public static final String STATUS_PATH = "/status.php";

    private OwncloudVersion _version;
    private boolean _supportOAuth;
    private boolean _supportSamlSso;
    private String _baseUrl;

    private UserCredentials _userCredentials;

    @Override
    public void setUserCredentials(UserCredentials userCredentials) {
        _userCredentials = userCredentials;
    }

    public void setVersion(OwncloudVersion version) {
        _version = version;
    }

    public void setSupportOAuth(boolean supportOAuth) {
        _supportOAuth = supportOAuth;
    }

    public void setSupportSamlSso(boolean supportSamlSso) {
        _supportSamlSso = supportSamlSso;
    }

    public void setBaseUrl(String baseUrl) {
        _baseUrl = baseUrl;
    }

    public String webdavPath() {
        if (_supportOAuth) {
            return ODAV_PATH;
        } else if (_supportSamlSso) {
            return SAML_SSO_PATH;
        } else if (_version.compareTo(OwncloudVersion.OC4_00) >= 0) {
            return WEBDAV_PATH_4_0;
        } else if (_version.compareTo(OwncloudVersion.OC3_00) >= 0 || _version.compareTo(OwncloudVersion.OC2_00) >= 0) {
            return WEBDAV_PATH_2_0;
        } else if (_version.compareTo(OwncloudVersion.OC1_00) >= 0) {
            return WEBDAV_PATH_1_2;
        } else {
            return null;
        }
    }

    public String serverAddress() {
        return PathUtil.join(_baseUrl, webdavPath());
    }

    @Override
    public WebdavClient build() throws Throwable {
        if (_version == null) {
            throw new IllegalArgumentException("The version of the owncloud server is not set.");
        }
        if (_baseUrl == null) {
            throw new IllegalArgumentException("The base url of the owncloud server is not set.");
        }
        String webdavPath = webdavPath();
        if (webdavPath == null) {
            throw new IllegalArgumentException("Failed to determine webdav path of the owncloud server V" + _version
                    + ".");
        }
        String serverAddress = PathUtil.join(_baseUrl, webdavPath);

        if (_userCredentials == null) {
            throw new IllegalArgumentException("The user credentials for accessing the owncloud server(" + _baseUrl
                    + ") is not set.");
        }
        return WebdavClientFactory.create(serverAddress, _userCredentials);
    }

}
