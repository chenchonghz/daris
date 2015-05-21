package nig.mf.client.util;



import nig.io.EraserThread;
import nig.sec.crypto.PasswordObfuscator;
import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;

public class ClientConnection  {


	/**
	 * Open a connection to a Mediaflux server by reading properties set in the 
	 * command line call to the executing Java class
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static ServerClient.Connection createServerConnection(String host, String portS, String transport) throws Throwable {
		boolean useHttp = false;
		boolean encrypt = false;
		if (host == null) {
			throw new Exception("Mediaflux host not specfied");
		}

		if (portS == null) {
			throw new Exception("Mediaflux port not specified");
		}
		int port = Integer.parseInt(portS);

		if (transport == null) {
			throw new Exception("Mediaflux transport not specified");
		}

		if (transport.equalsIgnoreCase("TCPIP")) {
			useHttp = false;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTP")) {
			useHttp = true;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTPS")) {
			useHttp = true;
			encrypt = true;
		} else {
			throw new Exception("Unexpected transport: " + transport + ", expected one of [tcpip,http,https]");
		}

		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = server.open();
		return cxn;
	}

	
	
	
	
	
	/**
	 * Find host, port and transport from system server properties and create connection
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static ServerClient.Connection createServerConnection() throws Throwable {
		boolean useHttp = false;
		boolean encrypt = false;
		String host = getProperty("mf.host");

		String p = getProperty("mf.port");
		int port = Integer.parseInt(p);

		String transport = getProperty("mf.transport");

		if (transport.equalsIgnoreCase("TCPIP")) {
			useHttp = false;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTP")) {
			useHttp = true;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTPS")) {
			useHttp = true;
			encrypt = true;
		} else {
			throw new Exception("Unexpected transport: " + transport + ", expected one of [tcpip,http,https]");
		}

		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = server.open();
		return cxn;
	}


	/**
	 * FInd domain, user and password from system properties and authenticate with
	 * existing connection
	 * 
	 * 
	 * @param cxn
	 * @param decrypt  If true decrypt p/w 
	 */
	public static void connect (ServerClient.Connection cxn, Boolean decrypt) throws Throwable {
		// Get user credential
		String domain = getProperty("mf.domain");
		String user = getProperty("mf.user");
		String password = getProperty("mf.password");		
		if (decrypt) password = decryptPassword (password);

		//
		cxn.connect(domain, user, password);
	}
	
	/**
	 * Fetch user credentials or secure token from system properties
	 * and then authenticate.  Connects with token (first) or user credential
	 * 
	 * Exception if neither available
	 * 
	 * @param cxn
	 * @param tokenApp  The app string that the token is bound to.If null skip to user/domain/pw
	 * @param decryptPassword  Decrypt the password if true
	 * @returns The UserCredential holding domain, user, token and token context.  The latter two null
	 * if no token used.  If token used, domain and user are filled in from the calling user that
	 * the token represents.
	 * @throws Throwable
	 */
	public static UserCredential connect (ServerClient.Connection cxn, String tokenApp, Boolean decryptPassword) throws Throwable {

		// Fetch and set to null if zero length
		String domain = System.getProperty("mf.domain");
		if (domain.length()==0) domain = null;
		String user = System.getProperty("mf.user");
		if (user.length()==0) user = null;
		String pw = System.getProperty("mf.password");
		if (pw.length()==0) pw = null;
		String token = System.getProperty("mf.token");
		if (token.length()==0) token = null;	
		//
		return ClientConnection.connect (cxn, tokenApp, token, domain, user, pw, decryptPassword);
	}

	/**
	 * Connect with token (first) or user credential if token null
	 * 
	 * Exception if neither available or if authentication fails.
	 * 
	 * @param cxn
	 * @param tokenApp  The app string that the token is bound to. 
	 * @param decryptPassword  Decrypt the password if true
	 * @returns The UserCredential holding domain, user, token and token context.  The latter two null
	 * if no token used.  If token used, domain and user are filled in from the calling user that
	 * the token represents.
	 * @throws Throwable
	 */
	public static UserCredential connect (ServerClient.Connection cxn, String tokenApp, String token,
			String domain, String user, String pw, Boolean decryptPassword) throws Throwable {
		UserCredential cred = null;
		if (token != null) {
			cxn.connectWithToken (tokenApp, token);
			
			XmlDoc.Element r = cxn.execute("user.self.describe");
			if (r!=null) {
				domain = r.value("user/@domain");
				user = r.value("user/@user");
				cred = new UserCredential (domain, user, true);
			}
		} else {
			if (domain!=null && user!=null && pw!=null) {
				ClientConnection.connect(cxn, domain, user, pw, decryptPassword);
				cred = new UserCredential(domain, user, false);
			} else {
				throw new Exception ("You must supply either a secure identity token (mf.token or domain (mf.domain), user (mf.user) and password (mf.password)");
			}
		}
		return cred;
	}

	/**
	 * Authnenticate with existing connection
	 * 
	 * @param cxn
	 * @param domain
	 * @param user
	 * @param password
	 * @param decrypt If true decrypt p/w (Base 64)
	 * @throws Throwable
	 */
	public static void connect (ServerClient.Connection cxn, String domain, String user, String password, Boolean decrypt) throws Throwable {
		if (decrypt) password = decryptPassword (password);
		cxn.connect(domain, user, password);
	}



	/**
	 * Authenticate to the server by prompting interactively for the p/w
	 *
	 * @param cxn
	 * @throws Throwable
	 */
	public static void interactiveAuthenticate (ServerClient.Connection cxn, String domain, String user) throws Throwable {
		EraserThread et = new EraserThread("Enter password for " + domain+":"+user +":");
		Thread mask = new Thread(et);
		mask.start();
		String pw = EraserThread.readString(null);
		et.stopMasking();

		// Authenticate
		cxn.connect(domain, user, pw);
	}

	public static String getProperty (String property) throws Throwable {
		String host = System.getProperty(property);
		if (host == null) {
			throw new Exception("Cannot find system property '" + property + "'");
		}
		return host;
	}

	private static String decryptPassword (String pw) {
		PasswordObfuscator encoder = PasswordObfuscator.defaultXor();
		char[] pc = encoder.decode(pw);
		return String.valueOf(pc);
	}
}
