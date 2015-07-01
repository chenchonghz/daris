package daris.installer;

public class ConnectionSettings {

	private boolean _useHttp;
	private boolean _encrypt;
	private String _host;
	private int _port;
	private String _domain;
	private String _user;
	private String _password;

	public ConnectionSettings() {

	}

	public void setUseHttp(boolean useHttp) {
		_useHttp = useHttp;
	}

	public boolean useHttp() {
		return _useHttp;
	}

	public void setEncrypt(boolean encrypt) {
		_encrypt = encrypt;
	}

	public boolean encrypt() {
		return _encrypt;
	}

	public void setHost(String host) {
		_host = host;
	}

	public String host() {
		return _host;
	}

	public void setPort(int port) {
		_port = port;
	}

	public int port() {
		return _port;
	}

	public void setDomain(String domain) {
		_domain = domain;
	}

	public String domain() {
		return _domain;
	}

	public void setUser(String user) {
		_user = user;
	}

	public String user() {
		return _user;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public String password() {
		return _password;
	}

}
