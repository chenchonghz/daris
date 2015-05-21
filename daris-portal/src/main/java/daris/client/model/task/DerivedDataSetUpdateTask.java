package daris.client.model.task;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.dataset.DataSet.Transform;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.dataset.DerivedDataSet.Input;

public class DerivedDataSetUpdateTask extends ImportTask {

	public static final String SERVICE = "om.pssd.dataset.derivation.update";

	private static class Arguments implements Args {
		public String id;
		public String name;
		public String description;
		public Transform transform;
		public XmlElement meta;
		public List<DerivedDataSet.Input> inputs;
		public boolean processed;
		public boolean anonymized;

		public Arguments(XmlElement ae) throws Throwable {
			id = ae.value("id");
			name = ae.value("name");
			description = ae.value("description");
			XmlElement te = ae.element("transform");
			transform = new Transform(te);
			meta = ae.element("meta");
			List<XmlElement> ies = ae.elements("input");
			if (ies != null && !ies.isEmpty()) {
				inputs = new Vector<DerivedDataSet.Input>();
				for (XmlElement ie : ies) {
					inputs.add(new DerivedDataSet.Input(ie.value(), ie.value("@vid")));
				}
			}
			processed = ae.booleanValue("processed", false);
			anonymized = ae.booleanValue("anonymized", false);
		}

		public Arguments(String id, String name, String description, Transform transform, XmlElement meta,
				List<DerivedDataSet.Input> inputs, boolean processed) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.transform = transform;
			this.meta = meta;
			this.inputs = inputs;
			this.processed = processed;
			this.anonymized = anonymized;
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
			if (inputs != null) {
				for (Input input : inputs) {
					w.add("input", new String[] { "vid", input.vid() }, input.id());
				}
			}
			w.add("processed", processed);
			w.add("anonymized", anonymized);
		}
	}

	protected DerivedDataSetUpdateTask(List<LocalFile> files, Map<String, String> variables) {
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
			args = new Arguments(id, null, null, null, null, null, false);
		}
		args.id = id;
		setArgs(args);
	}

	public void setName(String name) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, name, null, null, null, null, false);
		}
		args.name = name;
		setArgs(args);
	}

	public void setDescription(String description) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, description, null, null, null, false);
		}
		args.description = description;
		setArgs(args);
	}

	public void setTransform(Transform transform) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, transform, null, null, false);
		}
		args.transform = transform;
		setArgs(args);
	}

	public void setMeta(XmlElement meta) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, meta, null, false);
		}
		args.meta = meta;
		setArgs(args);
	}

	public void setInputs(List<Input> inputs) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, inputs, false);
		}
		args.inputs = inputs;
		setArgs(args);
	}

	public void setProcessed(boolean processed) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, null, processed);
		}
		args.processed = processed;
		setArgs(args);
	}
}
