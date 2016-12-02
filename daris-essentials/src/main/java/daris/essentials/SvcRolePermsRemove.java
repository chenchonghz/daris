package daris.essentials;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.util.DateUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcRolePermsRemove extends PluginService {




	private Interface _defn;

	public SvcRolePermsRemove()  throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("role", StringType.DEFAULT, "The role.", 1, 1));
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public Interface definition() {
		return _defn;

	}

	public String description() {
		return "Revoke all top level 'perm' elements from the given role.";
	}

	public String name() {
		return "nig.role.perms.revoke";
	}

	public boolean canBeAborted() {
		return false;
	}

	@Override
	public int minNumberOfOutputs() {
		return 0;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 0;
	}



	public void execute(Element args, Inputs inputs, Outputs outputs, final XmlWriter w) throws Throwable {

		String role  = args.value("role");
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", role);
		dm.add("type", "role");
		XmlDoc.Element r = executor().execute("actor.describe", dm.root());
		Collection<XmlDoc.Element> perms = r.elements("actor/perm");
		if (perms==null) return;
		for (XmlDoc.Element perm : perms) {
			dm = new XmlDocMaker("args");
			dm.add("name", role);
			dm.add("type", "role");
			dm.add(perm);
			try {
				executor().execute("actor.revoke", dm.root());
			} catch (Throwable t) {
				w.add("failed", perm);
			}
		}


	}


}
