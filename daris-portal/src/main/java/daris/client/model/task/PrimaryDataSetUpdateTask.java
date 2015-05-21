package daris.client.model.task;

import java.util.List;
import java.util.Map;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.dataset.DataSet.Transform;

public class PrimaryDataSetUpdateTask extends ImportTask {

	public static final String SERVICE = "om.pssd.dataset.primary.update";

	private static class Arguments implements Args {
		public String id;
		public String name;
		public String description;
		public Transform transform;
		public XmlElement meta;

		public Arguments(XmlElement ae) throws Throwable {
			id = ae.value("id");
			name = ae.value("name");
			description = ae.value("description");
			XmlElement te = ae.element("transform");
			transform = new Transform(te);
			meta = ae.element("meta");
		}

		public Arguments(String id, String name, String description, Transform transform, XmlElement meta) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.transform = transform;
			this.meta = meta;
		}

		@Override
		public void save(XmlWriter w) {
			w.add("id", id);
			if (name != null) {
				w.add("name", name);
			}
			if (description != null) {
				w.add("description", description);
			}
			if (transform != null) {
				transform.describe(w);
			}
			if (meta != null) {
				w.add(meta, true);
			}
		}
	}

	protected PrimaryDataSetUpdateTask(List<LocalFile> files, Map<String, String> variables) {
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
			args = new Arguments(id, null, null, null, null);
		}
		args.id = id;
		setArgs(args);
	}

	public void setName(String name) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, name, null, null, null);
		}
		args.name = name;
		setArgs(args);
	}

	public void setDescription(String description) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, description, null, null);
		}
		args.description = description;
		setArgs(args);
	}

	public void setTransform(Transform transform) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, transform, null);
		}
		args.transform = transform;
		setArgs(args);
	}

	public void setMeta(XmlElement meta) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, meta);
		}
		args.meta = meta;
		setArgs(args);
	}
}
