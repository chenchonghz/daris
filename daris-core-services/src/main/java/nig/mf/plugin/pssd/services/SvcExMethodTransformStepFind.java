package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.*;

public class SvcExMethodTransformStepFind extends PluginService {
	private Interface _defn;

	public SvcExMethodTransformStepFind() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the executing ExMethod object.", 1, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.ex-method.transform.step.find";
	}

	public String description() {
		return "Finds Transform Action steps within an executing method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Find the ExMethod. Can be primary or replica; we are not editingt it
		// here...
		DistributedAsset dEID = new DistributedAsset(args.element("id"));

		// Instantiate ExMethod from asset
		ExMethod em = ExMethod.lookup(executor(), dEID);

		// Regenerate Method object
		Method m = em.method();
		if (m != null) {
			List<Method.TransformAction> sas = m.transformActionStepPaths();
			if (sas != null) {
				w.push("ex-method", new String[] { "id", dEID.getCiteableID() });
				for (Method.TransformAction sa : sas) {
					w.add("step", sa.path());
				}
				w.pop();
			}
		}

	}

}
