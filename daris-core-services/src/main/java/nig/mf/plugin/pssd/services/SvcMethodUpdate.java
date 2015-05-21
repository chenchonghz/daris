package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcMethodUpdate extends PluginService {
	private Interface _defn;

	public SvcMethodUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the method. If not given, a new Method is created.", 0, 1));
		SvcMethodCreate.addInterface(_defn);
		SvcMethodCreate.addCreateInterface(_defn);
		_defn.add(new Interface.Element("replace", BooleanType.DEFAULT, "Replace (default) or merge the meta-data", 0,
				1));
	}

	public String name() {
		return "om.pssd.method.update";
	}

	public String description() {
		return "Updates or creates a research method. replace=true fully replaces all internals.  replace=false merges, but you can only add steps to the end of the existing list (you can't edit a pre-existing step).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		updateMethod(executor(), args, in, out, w);
	}

	public static void updateMethod(ServiceExecutor executor, XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {


		// Set distributed citeable ID for the Method. It is local by definition
		String proute = null;
		String id = args.value("id"); // Can be null if creating
		if (id != null) {
			DistributedAsset dID = new DistributedAsset(proute, id);

			// Check a few things...
			PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor, dID));
			if (type == null) {
				throw new Exception("The asset associated with " + dID.toString() + " does not exist");
			}
			if (!type.equals(Method.TYPE)) {
				throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
			}
			if (dID.isReplica()) {
				throw new Exception("The supplied Method is a replica and this service cannot modify it.");
			}
		}

		// Get the citable root name. The standard is 'pssd.method' and we allow
		// that to be auto-created. However,
		// we want to control other ones a little more and require that they be
		// explicitly created first.
		// Otherwise we could end up with a big mess of uncontrolled roots
		String cidRootName = args.stringValue("cid-root-name", "pssd.method");
		String methodRoot = SvcMethodCreate.getMethodRoot(executor, proute, cidRootName);

		// Merge or replace meta-data
		Boolean replace = args.booleanValue("replace", true);
		SvcMethodCreate.execute(executor, methodRoot, id, proute, args, w, replace);
	}
}
