package daris.client.model.task;

import java.util.List;
import java.util.Map;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObjectRef;

public class AttachmentAddTask extends ImportTask {

	public static final String SERVICE = "om.pssd.object.attach";

	private static class Arguments implements Args {

		public String id;
		public String attachmentName;
		public String attachmentDescription;

		public Arguments(XmlElement ae) throws Throwable {
			id = ae.value("id");
			attachmentName = ae.value("attachment/name");
			attachmentDescription = ae.value("attachment/description");
		}

		public Arguments(String id, String attachmentName, String attachmentDescription) {
			this.id = id;
			this.attachmentName = attachmentName;
			this.attachmentDescription = attachmentDescription;
		}

		public void save(XmlWriter w) {
			w.add("id", id);
			w.push("attachment");
			w.add("name", attachmentName);
			if (attachmentDescription != null) {
				w.add("description", attachmentDescription);
			}
			w.pop();
		}
	}

	public AttachmentAddTask(DObjectRef o, List<LocalFile> files) {
		this(files, null);
		setObjectId(o.id());
	}

	protected AttachmentAddTask(List<LocalFile> files, Map<String, String> variables) {
		super(FileCompilationProfile.PSSD_IMPORT, files, variables);
		setVariable(VAR_SERVICE, SERVICE);
	}

	@Override
	protected Args parseArgs(XmlElement ae) throws Throwable {
		return new Arguments(ae);
	}

	public void setObjectId(String id) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(id, null, null);
		}
		args.id = id;
		setArgs(args);
	}

	public void setAttachmentName(String name) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, name, null);
		}
		args.attachmentName = name;
		setArgs(args);
	}

	public void setAttachmentDescription(String description) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, description);
		}
		args.attachmentDescription = description;
		setArgs(args);
	}

}
