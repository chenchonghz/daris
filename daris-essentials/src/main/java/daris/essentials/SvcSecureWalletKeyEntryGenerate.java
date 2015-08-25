package daris.essentials;

import arc.mf.plugin.PluginService;

import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;



public class SvcSecureWalletKeyEntryGenerate extends PluginService {
	private Interface _defn;

	public SvcSecureWalletKeyEntryGenerate() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("host", StringType.DEFAULT, "The host name or IP.", 1, 1));
		_defn.add(new Interface.Element("user", StringType.DEFAULT, "The user name on the host", 1, 1));
		_defn.add(new Interface.Element("type", new EnumType(new String[] { "rsa", "dsa"}, true),
				"Key type. Defaults to rsa", 0, 1));
		_defn.add(new Interface.Element("size", new EnumType(new String[] { "1024", "2048", "4096"}, true),
				"Key size. Defaults to 1024", 0, 1));
		//
		Interface.Element me = new Interface.Element("usage", StringType.DEFAULT,"If a usage is specified, then the wallet entry can only be used from that context.", 0, 1);
		me.add(new Interface.Attribute("type", new EnumType(new String[] {"system", "service"}),
				"Type of security context. If system, then must be one of the known system contexts (see service secure.wallet.system.context.list for a list of the available system contexts).", 1));				
		_defn.add(me);
	}

	@Override
	public String name() {

		return "nig.secure.wallet.key.generate";
	}

	@Override
	public String description() {

		return "Generates a private/public key pair. Prints out the public key (for installation on a remote host) and puts the private key in the caller's secure wallet. The wallet entry name will be 'host-credentials:ssh://<host>' and the entry value will be in the XML structure :xvalue < :user :private-key > for use with secure-shell invocations.";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public boolean canBeAborted() {

		return false;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		String host = args.value("host");
		String user = args.value("user");
		String type = args.stringValue("type", "rsa");
		String size = args.stringValue("size", "1024");
		XmlDoc.Element usage = args.element("usage");
		
		// Make key pair
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", type);
		dm.add("size", size);
		XmlDoc.Element r = executor().execute("secure.shell.keygen", dm.root());
		
		// Show public
		w.add(r.element("public-key"));
		

		// Store private
		dm = new XmlDocMaker("args");
		dm.add("key", "host-credentials:ssh://" + host);
		dm.push("xvalue");
		dm.add("user", user);
		dm.add(r.element("private-key"));
		dm.pop();
		if (usage!=null) dm.add(usage);
		executor().execute("secure.wallet.set", dm.root());
	}
}
