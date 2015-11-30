package daris.client.model.task;

import java.io.File;

import arc.file.matching.Profile;
import arc.mf.client.ServerClient;
import arc.mf.desktop.server.Session;
import arc.utils.FileUtil;
import arc.xml.XmlStringWriter;

public class UploadFCP {

    public static final String DICOM_INGEST_FCP = "pssd.dicom.ingest.fcp";
    public static final String IMPORT_FCP = "pssd.import.fcp";

    public static Profile retrieve(String where) throws Throwable {
        ServerClient.Connection cxn = Session.connection();
        XmlStringWriter w = new XmlStringWriter();
        w.add("where", where);
        w.add("size", 1);
        String assetId = cxn.execute("asset.query", w.document()).value("id");
        File tf = File.createTempFile("daris.", ".fcp");
        try {
            cxn.execute("asset.get", "<id>" + assetId + "</id>", null,
                    new ServerClient.FileOutput(tf));
            Profile profile = Profile.compile(tf);
            return profile;
        } finally {
            FileUtil.delete(tf);
        }
    }

    public static Profile getDicomIngestFCP() throws Throwable {
        StringBuilder sb = new StringBuilder(
                "type='application/arc-fcp' and name='");
        sb.append(DICOM_INGEST_FCP);
        sb.append("'");
        return retrieve(sb.toString());
    }

    public static Profile getDataImportFCP() throws Throwable {
        StringBuilder sb = new StringBuilder(
                "type='application/arc-fcp' and name='");
        sb.append(IMPORT_FCP);
        sb.append("'");
        return retrieve(sb.toString());
    }

}
