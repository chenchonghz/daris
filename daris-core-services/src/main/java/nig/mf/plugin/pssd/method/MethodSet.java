package nig.mf.plugin.pssd.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * A utility class to cache the available methods. It uses a Map<String, Method>
 * - Id -> Method mapping.
 * 
 * @author wilson
 * 
 */
public class MethodSet {

	private Map<String, Method> _methods;

	private MethodSet(Map<String, Method> methods) {

		_methods = methods;
	}

	private MethodSet() {

		_methods = new HashMap<String, Method>();
	}

	public Method method(String id) {

		return _methods.get(id);
	}

	public String methodName(String id) {

		Method method = method(id);
		if (method != null) {
			return method.name();
		}
		return null;
	}

	public String methodDescription(String id) {

		Method method = method(id);
		if (method != null) {
			return method.description();
		}
		return null;
	}

	public static MethodSet load(ServiceExecutor executor, ServerRoute sroute, boolean forSubject) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		String where = "xpath(daris:pssd-object/type)='method'";
		dm.add("where", forSubject ? (where + " and (daris:pssd-method-subject has value or daris:pssd-method-rsubject has value)")
				: where);
		dm.add("size", "infinity");
		dm.add("action", "get-value");
		dm.add("xpath", new String[] { "ename", "id" }, "cid");
		dm.add("xpath", new String[] { "ename", "version" }, "meta/daris:pssd-method/version");
		dm.add("xpath", new String[] { "ename", "name" }, "meta/daris:pssd-object/name");
		dm.add("xpath", new String[] { "ename", "description" }, "meta/daris:pssd-object/description");
		XmlDoc.Element r = Asset.query(executor, sroute, dm.root());
		List<XmlDoc.Element> aes = r.elements("asset");
		if (aes == null) {
			return null;
		}
		HashMap<String, Method> methods = new HashMap<String, Method>(aes.size());
		for (XmlDoc.Element ae : aes) {
			String id = ae.value("id");
			String version = ae.value("version");
			String name = ae.value("name");
			String desc = ae.value("description");
			Method m = new Method(id, name, desc, version);
			methods.put(id, m);
		}
		return new MethodSet(methods);
	}
}
