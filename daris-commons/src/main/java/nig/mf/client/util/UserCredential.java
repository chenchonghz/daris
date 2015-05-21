package nig.mf.client.util;


/**
 * Holds the {domain,user} that a client connection was authenticated with.
 * If the authentication occurred via secure identity token then this can be
 * stored also.
 * 
 * @author nebk
 *
 */
public class UserCredential  {

	private String domain_ = null;
	private String user_ = null;
	private Boolean fromToken_ = null;
	

	/**
	 * Constructor. 
	 * 
	 * @param domain
	 * @param user
	 * @param fromToken was the identity derived form a secure identity token ?
	 * @throws Throwable
	 */
	public UserCredential (String domain, String user, Boolean fromToken) throws Throwable {
		domain_ = domain;
		user_ = user;
		fromToken_ = fromToken;
	}

	
	public String domain () {return domain_;}
	public String user () {return user_;};
	public Boolean fromToken () { return fromToken_;};
	public void setDomain (String domain) {domain_ = domain;};
	public void setUser (String user) {user_ = user;};
	public void setFromToken (Boolean fromToken) {fromToken_ = fromToken;};
	public String toString () {
		String t = "Domain     : " + domain_ + "\n" +
	               "User       : " + user_ + "\n" +
				   "From token : " + fromToken_;
		return t;
	}
}
