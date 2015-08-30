package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.util.AssetRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMAEAdd extends PluginService {

	public static final String DOCTYPE = "daris:pssd-dicom-server-registry";
	public static final String REGISTRY_ASSET_TYPE = "pssd-dicom-server-registry"; 
	

	public static final String ACCESS_PUBLIC="public";
	public static final String ACCESS_PRIVATE="private";


	private Interface _defn;

	public SvcDICOMAEAdd() {
		_defn = new Interface();
		addInterface (_defn, false, null);
		_defn.add(new Interface.Element("access", new EnumType(new String[] {"public", "private"}),
				"The access type. The default,  'public', means accessible to all, 'private' means accessible only by the creator.", 0, 1));
	}

	/**
	 * This function is re-used by a number of AE services (registry and dicom send)
	 * 
	 * @param defn
	 * @param childrenOptional Means that the host,port,aet children elements are optional (min=0)
	 * @param nameDescription Lets you over-ride the default description for the name attribute.  Give null
	 * for default value
	 */
	public static void addInterface (Interface defn, boolean childrenOptional, String nameDescription)  {
		int minOccurs = 1;
		if (childrenOptional) minOccurs = 0;
		//
		String d = "A unique convenience name that this AE, after creation, may be referred to by.";
		if (nameDescription!=null) d = nameDescription;
		Interface.Element me = new Interface.Element("ae",XmlDocType.DEFAULT,
				"The DICOM Application Entity (e.g. a DICOM server).",1,1);
		me.add(new Interface.Attribute("name", StringType.DEFAULT, d, 1));
		me.add(new Interface.Element("host", StringType.DEFAULT, "The AE host name or IP address.", minOccurs, 1));
		me.add(new Interface.Element("port", IntegerType.DEFAULT, "The port number of the AE", minOccurs, 1));
		me.add(new Interface.Element("aet",StringType.DEFAULT,"The AET of the AE.",minOccurs, 1));
		defn.add(me);

	}

	public String name() {
		return "om.pssd.dicom.ae.add";
	}

	public String description() {
		return "Adds a DICOM Application Entity to the DICOM AE registry.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		// Parse and rebuild in fixed order so that Registry comparison code works
		XmlDoc.Element ae0 = args.element("ae");
		XmlDoc.Element ae = rebuildAEInOrder (ae0);
				//
		String access = args.stringValue("access", "public");
		AssetRegistry.AccessType accessType = AssetRegistry.parseAccessType(access);

		// CHeck name is unique. This context is specific to the DICOM AE context and not really a generic registry function
		checkNameIsUnique (executor(), ae.attribute("name").value());


		// Add the 'remote' attribute. We also have a 'local' AE but this is stored
		// as a server property and fetched by om.pssd.ae.list.  It's not stored
		// in the AE registry.
		XmlDoc.Attribute att = new XmlDoc.Attribute("type", "remote");
		ae.add(att);

		// Add the accessibility attribute
		att = new XmlDoc.Attribute("access", access);
		ae.add(att);

		// See if the singleton asset already exists; else create.
		String id = AssetRegistry.findAndCreateRegistry(executor(), REGISTRY_ASSET_TYPE, accessType);

		// Add the new (unique) role. 
		AssetRegistry.addItem (executor(), id, ae, DOCTYPE);
		w.add("id", id);
	}

	static public XmlDoc.Element rebuildAEInOrder (XmlDoc.Element ae) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		String name = ae.attribute("name").value();
		dm.push("ae", new String[] {"name", name});
		dm.add("aet", ae.value("aet"));
		dm.add("host", ae.value("host"));
		dm.add("port", ae.value("port"));
		dm.pop();
		//
		return dm.root().element("ae");
	}


	private void checkNameIsUnique(ServiceExecutor executor, String newName) throws Throwable {
		XmlDoc.Element r = executor.execute("om.pssd.dicom.ae.list");
		if (r==null) return;
		//
		Collection<String> names = r.values("ae/@name");
		if (names==null) return;
		//
		for (String name : names) {
			if (newName.equals(name)) {
				throw new Exception ("The AE name must be unique amongst all the AEs visible to you (public and private)");
			}
		}
	}
}
