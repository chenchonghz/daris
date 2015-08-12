package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.DataObject;
import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.Metadata;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.Subject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcTypeMetadataRemove extends PluginService {
	private Interface _defn;

	public SvcTypeMetadataRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("type",new EnumType(new String[] { Project.TYPE.toString(), Subject.TYPE.toString(), Study.TYPE.toString(), DataSet.TYPE.toString(), DataObject.TYPE.toString(), RSubject.TYPE.toString(), RSubject.TYPE_PRIVATE }), "The type to remove definitions of metadata.", 1, 1));
		_defn.add(new Interface.Element("mtype",StringType.DEFAULT, "The type of the metadata to associate with this object type.", 1, 1));
	}

	public String name() {
		return "om.pssd.type.metadata.remove";
	}

	public String description() {
		return "Removes the association between an existing metadata type with the specified object type on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.stringValue("type");
		String mtype = args.stringValue("mtype");

		removeTagsForType(executor(),type,mtype);
	}
	
	public static void removeTagsForType(ServiceExecutor executor,String type,String mtype) throws Throwable {
		String tag = Metadata.tagForType(type);
		String reqTagA = Metadata.requirementTagForType(type,"optional");
		String reqTagB = Metadata.requirementTagForType(type,"mandatory");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type",mtype);
		dm.add("tag",tag);
		dm.add("tag",reqTagA);
		dm.add("tag",reqTagB);

		executor.execute("asset.doc.type.tag.remove",dm.root());

	}
}
