package nig.ssh.client;

public class UserDetails {

	private String _username;
	private String _password;
	private String _privateKey;
	private String _passphrase;
	private String _publicKey;

	public UserDetails(String username, String password, String privateKey,
			String passphrase, String publicKey) {
		_username = username;
		_password = password;
		_privateKey = privateKey;
		_passphrase = passphrase;
		_publicKey = publicKey;
	}

	public UserDetails(String username, String password) {
		this(username, password, null, null, null);
	}

	public UserDetails(String username, String privateKey, String passphrase,
			String publicKey) {
		this(username, null, privateKey, passphrase, publicKey);
	}

	public String username() {
		return _username;
	}

	public String password() {
		return _password;
	}

	public String privateKey() {
		return _privateKey;
	}

	public String publicKey() {
		return _publicKey;
	}

	public String passphrase() {
		return _passphrase;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public void setKeyPair(String privateKey, String publicKey) {
		_privateKey = privateKey;
		_publicKey = publicKey;
	}

	public void setPassphrase(String passphrase) {
		_passphrase = passphrase;
	}
}
