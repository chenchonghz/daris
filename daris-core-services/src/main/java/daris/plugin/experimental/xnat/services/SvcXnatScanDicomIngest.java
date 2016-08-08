package daris.plugin.experimental.xnat.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.experimental.xnat.XnatRestClient;

public class SvcXnatScanDicomIngest extends AbstractXnatPluginService {

	public static final String SERVICE_NAME = "daris.xnat.scan.dicom.ingest";

	public SvcXnatScanDicomIngest() {
		defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the destination DaRIS project/subject/study", 1, 1));
		defn.add(new Interface.Element("project", StringType.DEFAULT, "The source XNAT project id.", 1, 1));
		defn.add(new Interface.Element("subject", StringType.DEFAULT, "The source XNAT subject id.", 1, 1));
		defn.add(new Interface.Element("experiment", StringType.DEFAULT, "The source XNAT experiment id.", 1, 1));
		defn.add(new Interface.Element("scan", StringType.DEFAULT, "The source XNAT scan id.", 1, Integer.MAX_VALUE));
	}

	@Override
	protected void execute(String siteUrl, String session, Element args, Inputs ins, Outputs outs, XmlWriter w)
			throws Throwable {
		String cid = args.value("cid");
		String xnatProject = args.value("project");
		String xnatSubject = args.value("subject");
		String xnatExperiment = args.value("experiment");
		List<String> xnatScanIds = new ArrayList<String>(args.values("scan"));
		StringBuilder sb = new StringBuilder("/data/archive/projects/");
		sb.append(xnatProject);
		sb.append("/subjects/");
		sb.append(xnatSubject);
		sb.append("/experiments/");
		sb.append(xnatExperiment);
		sb.append("/scans/");
		for (int i = 0; i < xnatScanIds.size(); i++) {
			String xnatScanId = xnatScanIds.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(xnatScanId);
		}
		sb.append("/resources/DICOM,secondary/files?format=zip");
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
		return "Ingest DICOM data from the source XNAT scan(s) to the destination DaRIS project or subject specified.";
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
