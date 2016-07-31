package daris.plugin.experimental.xnat.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.PasswordType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.plugin.experimental.xnat.XnatRestClient;

public class SvcXnatLogin extends PluginService {

	public static final String SERVICE_NAME = "daris.xnat.login";

	private Interface _defn;

	public SvcXnatLogin() {
		_defn = new Interface();
		_defn.add(new Interface.Element("url", UrlType.DEFAULT, "XNAT site url.", 1, 1));
		_defn.add(new Interface.Element("username", StringType.DEFAULT, "XNAT username.", 1, 1));
		_defn.add(new Interface.Element("password", PasswordType.DEFAULT, "XNAT password.", 1, 1));

	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Login to the specified XNAT site and return session id.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String url = args.value("url");
		String username = args.value("username");
		String password = args.value("password");
		w.add("session", XnatRestClient.login(url, username, password));
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
