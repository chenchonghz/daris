package daris.plugin.experimental.xnat.services;

import java.util.Collection;

import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.plugin.experimental.xnat.XnatRestClient;

public class SvcXnatScanDownload extends AbstractXnatPluginService {

	public static final String SERVICE_NAME = "daris.xnat.scan.download";

	public SvcXnatScanDownload() {
		addToDefn(defn);
	}

	static void addToDefn(Interface defn) {
		defn.add(new Interface.Element("project", StringType.DEFAULT, "XNAT project id.", 1, 1));
		defn.add(new Interface.Element("subject", StringType.DEFAULT, "XNAT subject id.", 1, 1));
		defn.add(new Interface.Element("experiment", StringType.DEFAULT, "XNAT experiment id.", 1, 1));
		defn.add(new Interface.Element("scan", StringType.DEFAULT, "XNAT scan id.", 1, Integer.MAX_VALUE));
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public String description() {
		return "Downloads the specified scan from XNAT.";
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

	@Override
	protected void execute(String siteUrl, String session, Element args, Inputs inputs, Outputs outputs, XmlWriter w)
			throws Throwable {
		String projectId = args.value("project");
		String subjectId = args.value("subject");
		String experimentId = args.value("experiment");
		Collection<String> scanIds = args.values("scan");
		outputs.output(0).setData(
				XnatRestClient.getStreamForDownloadScans(siteUrl, projectId, subjectId, experimentId, scanIds, session),
				-1, "application/zip");
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

}
