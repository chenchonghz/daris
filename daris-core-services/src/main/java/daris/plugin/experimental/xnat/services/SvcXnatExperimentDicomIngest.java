package daris.plugin.experimental.xnat.services;

import java.io.InputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.experimental.xnat.XnatRestClient;

public class SvcXnatExperimentDicomIngest extends AbstractXnatPluginService {

	public static final String SERVICE_NAME = "daris.xnat.experiment.dicom.ingest";

	public SvcXnatExperimentDicomIngest() {
		defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the destination DaRIS project/subject", 1, 1));
		defn.add(new Interface.Element("project", StringType.DEFAULT, "The source XNAT project id.", 1, 1));
		defn.add(new Interface.Element("subject", StringType.DEFAULT, "The source XNAT subject id.", 1, 1));
		defn.add(new Interface.Element("experiment", StringType.DEFAULT, "The source XNAT experiment id.", 1, 1));

	}

	@Override
	protected void execute(String siteUrl, String session, Element args, Inputs ins, Outputs outs, XmlWriter w)
			throws Throwable {
		String cid = args.value("cid");
		String xnatProject = args.value("project");
		String xnatSubject = args.value("subject");
		String xnatExperiment = args.value("experiment");
		StringBuilder sb = new StringBuilder("/data/archive/projects/");
		sb.append(xnatProject);
		sb.append("/subjects/");
		sb.append(xnatSubject);
		sb.append("/experiments/");
		sb.append(xnatExperiment);
		sb.append("/scans/ALL/resources/DICOM/files?format=zip");
		InputStream in = XnatRestClient.getDownloadStream(siteUrl, sb.toString(), session, true);

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("engine", "nig.dicom");
		dm.add("arg", new String[] { "name", "nig.dicom.id.citable" }, cid);
		dm.add("type", "application/zip");
		PluginService.Inputs inputs = new PluginService.Inputs(
				new PluginService.Input(in, -1, "application/zip", null));
		executor().execute("dicom.ingest", dm.root(), inputs, null);
	}

	@Override
	public Access access() {
		return ACCESS_MODIFY;
	}

	@Override
	public String description() {
		return "Ingest DICOM data from the source XNAT experiment to the destination DaRIS project or subject specified.";
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
