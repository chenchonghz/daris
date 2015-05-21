package nig.mf.plugin.pssd.object;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class RelationshipType {

	public static String getInverseType(ServiceExecutor executor, String type) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", type);
		XmlDoc.Element r = executor.execute("asset.relationship.type.describe", dm.root());
		return r.value("type/inverse");
	}

}
