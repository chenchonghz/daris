package nig.webdav.client;

public class UserCredentials {

    private String _userName;
    private String _password;

    public UserCredentials(String userName, String password) {
        _userName = userName;
        _password = password;
    }

    public String userName() {
        return _userName;
    }

    public String password() {
        return _password;
    }

}
