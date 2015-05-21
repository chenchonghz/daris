package nig.mf.plugin.pssd.dicom;

import java.net.InetAddress;
import java.util.List;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class LocalDicomAE {

	public static void describe(ServiceExecutor executor, XmlWriter w) throws Throwable {
		XmlDoc.Element r = executor.execute("network.describe");
		List<XmlDoc.Element> ses = r.elements("service[@type='dicom']");
		if (ses != null) {
			for (XmlDoc.Element se : ses) {
				int port = se.intValue("@port");
				String aet = se.value("arg[@name='dicom.title']");
				if (port > 0 && aet != null) {
					w.push("ae", new String[] { "type", "local" });
					w.add("aet", aet);
					w.add("host", InetAddress.getLocalHost().getHostName());
					w.add("port", port);
					w.pop();
				}
			}
		}
	}

}
