package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcExMethodSubjectStepFind extends PluginService {
	private Interface _defn;

	public SvcExMethodSubjectStepFind() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the executing ExMethod object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.ex-method.subject.step.find";
	}

	public String description() {
		return "Finds Subject Action steps that specify Subject state changes within an executing method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {	
		
		// Find the ExMethod. Can be primary or replica; we are not editingt it here...
		DistributedAsset dEID = new DistributedAsset(args.element("id"));

		// Instantiate ExMethod from asset
		ExMethod em = ExMethod.lookup(executor(), dEID);

		// Regenerate Method object
		Method m = em.method();
		if ( m != null ) {
			List<Method.SubjectAction> sas = m.subjectActionStepPaths();
			if ( sas != null ) {
				w.push("ex-method",new String[] { "id", dEID.getCiteableID() });
				for ( int i=0; i < sas.size(); i++ ) {
					Method.SubjectAction sa = sas.get(i);
					w.add("step",sa.path());
				}
				w.pop();
			}
		}

	}
	
}
