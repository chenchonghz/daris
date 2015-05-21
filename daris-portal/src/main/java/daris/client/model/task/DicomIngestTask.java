package daris.client.model.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObjectRef;

public class DicomIngestTask extends ImportTask {

	public static final String ENGINE_NIG_DICOM = "nig.dicom";
	public static final String NIG_DICOM_ID_CITABLE = "nig.dicom.id.citable";

	private static class Arguments implements Args {

		public boolean anonymize = true;
		public String anonymizeElements;
		public Map<String, String> ingestArgs;
		public String engine;
		public String service;
		public String type;
		public boolean wait = true;

		public Arguments(XmlElement ae) throws Throwable {
			anonymize = ae.booleanValue("anonymize", true);
			anonymizeElements = ae.stringValue("anonymize-elements");
			List<XmlElement> iaes = ae.elements("arg");
			if (iaes != null && !iaes.isEmpty()) {
				Map<String, String> ingestArgs = new HashMap<String, String>();
				for (XmlElement iae : iaes) {
					ingestArgs.put(iae.value("name"), iae.value());
				}
			}
			engine = ae.value("engine");
			service = ae.value("service");
			type = ae.value("type");
			wait = ae.booleanValue("wait", true);
		}

		public Arguments(boolean anonymize, String anonymizeElements, Map<String, String> ingestArgs, String engine,
				String service, String type, boolean wait) {
			this.anonymize = anonymize;
			this.anonymizeElements = anonymizeElements;
			this.ingestArgs = ingestArgs;
			this.engine = engine;
			this.service = service;
			this.type = type;
			this.wait = wait;
		}

		public void save(XmlWriter w) {
			w.add("anonymize", anonymize);
			if (anonymizeElements != null) {
				w.add("anonymize-elements", anonymizeElements);
			}
			if (ingestArgs != null) {
				for (String name : ingestArgs.keySet()) {
					w.add("arg", new String[] { "name", name }, ingestArgs.get(name));
				}
			}
			w.add("engine", engine);
			if (service != null) {
				w.add("service", service);
			}
			if (type != null) {
				w.add("type", type);
			}
			w.add("wait", wait);
		}
	}

	public DicomIngestTask(DObjectRef po, List<LocalFile> files) {
		this(files, null);
		setIdCiteable(po.id());
	}

	protected DicomIngestTask(List<LocalFile> files, Map<String, String> variables) {
		super(FileCompilationProfile.DICOM_INGEST, files, variables);
	}

	@Override
	protected Args parseArgs(XmlElement ae) throws Throwable {
		return new Arguments(ae);
	}

	public void setAnonymize(boolean anonymize) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(anonymize, null, null, ENGINE_NIG_DICOM, null, null, true);
		}
		args.anonymize = anonymize;
		setArgs(args);
	}

	public void setAnonymizeElements(String anonymizeElements) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, anonymizeElements, null, ENGINE_NIG_DICOM, null, null, true);
		}
		args.anonymizeElements = anonymizeElements;
		setArgs(args);
	}

	public void setIngestArgs(Map<String, String> ingestArgs) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, ingestArgs, ENGINE_NIG_DICOM, null, null, true);
		}
		args.ingestArgs = ingestArgs;
		setArgs(args);
	}

	public void setEngine(String engine) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, null, engine, null, null, true);
		}
		args.engine = engine;
		setArgs(args);
	}

	public void setService(String service) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, null, ENGINE_NIG_DICOM, service, null, true);
		}
		args.service = service;
		setArgs(args);
	}

	public void setType(String type) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, null, ENGINE_NIG_DICOM, null, type, true);
		}
		args.type = type;
		setArgs(args);
	}

	public void setWait(boolean wait) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, null, ENGINE_NIG_DICOM, null, null, wait);
		}
		args.wait = wait;
		setArgs(args);
	}

	public void setIdCiteable(String pid) {
		Arguments args = (Arguments) args();
		if (args == null) {
			args = new Arguments(true, null, null, ENGINE_NIG_DICOM, null, null, true);
		}
		Map<String, String> ingestArgs = args.ingestArgs;
		if (ingestArgs == null) {
			ingestArgs = new HashMap<String, String>();
		}
		ingestArgs.put(NIG_DICOM_ID_CITABLE, pid);
		args.ingestArgs = ingestArgs;
		setArgs(args);
	}

}
