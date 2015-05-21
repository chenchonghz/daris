package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class Subject {
	public static final PSSDObject.Type TYPE = PSSDObject.Type.subject;
	
	public static final int DEPTH = 1;
	
	public static final String MODEL = "om.pssd.subject";
	
	/**
	 * Returns true if the CID is for a Subject object
	 */
	public static boolean isObjectSubject (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",cid);	
		XmlDoc.Element r = executor.execute("om.pssd.object.type",dm.root());	
        PSSDObject.Type type = PSSDObject.Type.parse(r.value("type"));
        if (type==TYPE) return true;
		return false;
	}

}
