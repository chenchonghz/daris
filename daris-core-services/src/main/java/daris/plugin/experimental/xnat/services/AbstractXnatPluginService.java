package daris.plugin.experimental.xnat.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.PasswordType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.plugin.experimental.xnat.XnatRestClient;

public abstract class AbstractXnatPluginService extends PluginService {

	protected Interface defn;

	protected AbstractXnatPluginService() {
		defn = new Interface();
		defn.add(new Interface.Element("url", UrlType.DEFAULT, "XNAT site url.", 1, 1));
		Interface.Element auth = new Interface.Element("auth", XmlDocType.DEFAULT, "XNAT authentication details.", 1,
				1);
		auth.add(new Interface.Element("username", StringType.DEFAULT,
				"XNAT username. If not specified, session id must be specified.", 0, 1));
		auth.add(new Interface.Element("password", PasswordType.DEFAULT,
				"XNAT password. If not specified, session id must be specified.", 0, 1));
		auth.add(new Interface.Element("session", PasswordType.DEFAULT,
				"XNAT session id. If not specified, username and password must be specified.", 0, 1));
		defn.add(auth);
	}

	@Override
	public Interface definition() {
		return defn;
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String url = args.value("url");
		String username = args.value("auth/username");
		String password = args.value("auth/password");
		String session = args.value("auth/session");
		if (session == null && (username == null || password == null)) {
			throw new IllegalArgumentException("Expects session, or username and password. Found none.");
		}
		if (session != null && (username != null || password != null)) {
			throw new IllegalArgumentException("Expects session, or username and password. Found both.");
		}
		if (session == null) {
			session = XnatRestClient.login(url, username, password);
		}
		execute(url, session, args, inputs, outputs, w);
	}

	protected abstract void execute(String siteUrl, String session, Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable;
}
