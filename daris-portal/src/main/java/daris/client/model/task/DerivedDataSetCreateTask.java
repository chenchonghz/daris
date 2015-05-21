package daris.client.model.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.dataset.DataSet.Transform;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.dataset.DerivedDataSet.Input;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DerivedDataSetCreateTask extends ImportTask {

	public static final String SERVICE = "om.pssd.dataset.derivation.create";

	private static class Arguments implements Args {
		public String pid;
		public String proute;
		public String name;
		public String filename;
		public String description;
		public Transform transform;
		public XmlElement meta;
		public List<DerivedDataSet.Input> inputs;
		public boolean processed;

		public Arguments(XmlElement ae) throws Throwable {
			pid = ae.value("pid");
			proute = ae.value("pid/@proute");
			name = ae.value("name");
			filename = ae.value("filename");
			description = ae.value("description");
			XmlElement te = ae.element("transform");
			if (te != null) {
				transform = new Transform(te);
			}
			meta = ae.element("meta");
			List<XmlElement> ies = ae.elements("input");
			if (ies != null && !ies.isEmpty()) {
				inputs = new Vector<DerivedDataSet.Input>();
				for (XmlElement ie : ies) {
					inputs.add(new DerivedDataSet.Input(ie.value(), ie
							.value("@vid")));
				}
			}
			processed = ae.booleanValue("processed", false);
		}

		public Arguments(String pid, String proute, String name,
				String filename, String description, Transform transform,
				XmlElement meta, List<DerivedDataSet.Input> inputs,
				boolean processed) {
			this.pid = pid;
			this.proute = proute;
			this.name = name;
			this.filename = filename;
			this.description = description;
			this.transform = transform;
			this.meta = meta;
			this.inputs = inputs;
			this.processed = processed;
		}

		@Override
		public void save(XmlWriter w) {
			w.add("pid", new String[] { "proute", proute }, pid);
			if (name != null) {
				w.add("name", name);
			}
			if (filename != null) {
				w.add("filename", filename);
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
					w.add("input", new String[] { "vid", input.vid() },
							input.id());
				}
			}
			w.add("processed", processed);
		}
	}

	private DObjectRef _input;

	public DerivedDataSetCreateTask(DObjectRef po, DObjectRef input,
			List<LocalFile> files) {
		this(files, null);
		_input = input;
		setParent(po.id(), po.proute());
		if (_input != null) {
			if (_input.referent() != null && _input.referent().vid() == null) {
				_input.reset();
			}
			_input.resolve(new ObjectResolveHandler<DObject>() {

				@Override
				public void resolved(DObject o) {
					if (o != null) {
						List<Input> inputs = new ArrayList<Input>(1);
						inputs.add(new DerivedDataSet.Input(o.id(), o.vid()));
						setInputs(inputs);
					}
				}
			});
		}

	}

	protected DerivedDataSetCreateTask(List<LocalFile> files,
			Map<String, String> variables) {
		super(FileCompilationProfile.PSSD_IMPORT, files, variables);
		setVariable(VAR_SERVICE, SERVICE);
	}

	@Override
	protected Args parseArgs(XmlElement ae) throws Throwable {
		return new Arguments(ae);
	}

	@Override
	public void setFiles(List<LocalFile> files) {
		if (files != null && files.size() == 1) {
			String filename = files.get(0).name();
			Arguments args = (Arguments) args();
			if (args == null) {
				args = new Arguments(null, null, null, filename, null, null,
						null, null, false);
			}
			args.filename = filename;
			setArgs(args);
		}
		super.setFiles(files);
	}

	public void setParent(String pid, String proute) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(pid, proute, null, null, null, null, null,
					null, false);
		}
		args.pid = pid;
		args.proute = proute;
		setArgs(args);
	}

	public void setName(String name) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, name, null, null, null, null,
					null, false);
		}
		args.name = name;
		setArgs(args);
	}

	public void setDescription(String description) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, description, null,
					null, null, false);
		}
		args.description = description;
		setArgs(args);
	}

	public void setTransform(Transform transform) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, transform, null,
					null, false);
		}
		args.transform = transform;
		setArgs(args);
	}

	public void setMeta(XmlElement meta) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, null, meta,
					null, false);
		}
		args.meta = meta;
		setArgs(args);
	}

	public void setInputs(List<Input> inputs) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, null, null,
					inputs, false);
		}
		args.inputs = inputs;
		setArgs(args);
	}

	public void setProcessed(boolean processed) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(null, null, null, null, null, null, null,
					null, processed);
		}
		args.processed = processed;
		setArgs(args);
	}

	public DObjectRef inputDataSet() {
		return _input;
	}
}
