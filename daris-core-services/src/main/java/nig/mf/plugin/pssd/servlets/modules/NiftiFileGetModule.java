package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.servlets.DicomServlet;
import nig.mf.plugin.pssd.servlets.Disposition;
import nig.mf.plugin.pssd.servlets.NiftiServlet;

public class NiftiFileGetModule implements Module {

    public static final NiftiFileGetModule INSTANCE = new NiftiFileGetModule();

    public static final String NAME = NiftiServlet.ModuleName.file.name();

    public static final String MIME_TYPE_NII = "image/x-nifti";

    public static final String MIME_TYPE_NII_GZ = "image/x-nifti-gz";

    public static final String MIME_TYPE_NIFTI_SERIES = "nifti/series";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // id
        String id = request.variableValue(NiftiServlet.ARG_ID);
        // cid
        String cid = request.variableValue(NiftiServlet.ARG_CID);
        // idx
        String idxStr = request.variableValue(NiftiServlet.ARG_IDX);
        long idx = idxStr == null ? 1 : Long.parseLong(idxStr);
        // disposition
        Disposition disposition = Disposition.parse(
                request.variableValue(DicomServlet.ARG_DISPOSITION),
                Disposition.attachment);
        // filename
        String fileName = request.variableValue(DicomServlet.ARG_FILENAME);
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        XmlDoc.Element ae = server
                .execute(sessionKey, "asset.get", dm.root(), null, null)
                .element("asset");

        validate(ae);

        String cType = ae.value("content/type");
        String cExt = ae.value("content/type/@ext");
        if (id == null) {
            id = ae.value("@id");
        } else {
            cid = ae.value("cid");
        }
        String idStr = (cid != null ? cid : id);
        if (fileName == null) {
            fileName = ae.value("meta/daris:pssd-filename/original");
        }
        if (isNII(cExt)) {
            response.setHeaderField("Content-Type", MIME_TYPE_NII);
            if (fileName == null) {
                fileName = idStr + ".nii";
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + fileName + "\"");
            server.execute(sessionKey, "asset.get", dm.root(),
                    (HttpRequest) null, response);
        } else if (isGZ(cExt)) {
            response.setHeaderField("Content-Type", MIME_TYPE_NII_GZ);
            if (fileName == null) {
                fileName = idStr + ".nii.gz";
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + fileName + "\"");
            server.execute(sessionKey, "asset.get", dm.root(),
                    (HttpRequest) null, response);
        } else if (isArchive(cExt)) {
            response.setHeaderField("Content-Type", MIME_TYPE_NII);
            if (fileName == null) {
                fileName = idStr + "_" + idx + ".nii";
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + fileName + "\"");
            dm.add("idx", idx);
            server.execute(sessionKey, "daris.archive.content.get", dm.root(),
                    (HttpRequest) null, response);
        } else {
            throw new Exception("Unsupported content type: " + cType);
        }
    }

    static void validate(XmlDoc.Element ae) throws Throwable {
        String cid = ae.value("cid");
        String idStr = cid == null ? ae.value("@id") : cid;
        if (!ae.elementExists("content")) {
            throw new Exception("Asset " + idStr
                    + " is not a valid NIFTI series. No content.");
        }
        String ctype = ae.value("content/type");
        String cext = ae.value("content/type/@ext");
        if (!isNII(cext) && !isGZ(cext) && !isArchive(cext)) {
            throw new Exception("Asset " + idStr
                    + " is not a valid NIFTI series. Unsupported content mime type: "
                    + ctype);
        }
        String type = ae.value("type");
        String lctype = ae.value("content/ltype");
        if (!(MIME_TYPE_NIFTI_SERIES.equals(type)
                || MIME_TYPE_NIFTI_SERIES.equals(lctype)
                || MIME_TYPE_NII_GZ.equals(type)
                || MIME_TYPE_NII_GZ.equals(lctype) || MIME_TYPE_NII.equals(type)
                || MIME_TYPE_NII.equals(lctype)
                || ae.elementExists("meta/daris:nifti-1"))) {
            throw new Exception("Asset " + idStr
                    + " is not a valid NIFTI series. Unsupported asset mime type: "
                    + type + " and logical content type: " + lctype);
        }
    }

    static boolean isNII(String ext) {
        return "nii".equalsIgnoreCase(ext);
    }

    static boolean isGZ(String ext) {
        return "gz".equalsIgnoreCase(ext);
    }

    static boolean isZIP(String ext) {
        return "zip".equalsIgnoreCase(ext);
    }

    static boolean isAAR(String ext) {
        return "aar".equalsIgnoreCase(ext);
    }

    static boolean isJAR(String ext) {
        return "jar".equalsIgnoreCase(ext);
    }

    static boolean isTAR(String ext) {
        return "tar".equalsIgnoreCase(ext);
    }

    static boolean isArchive(String ext) {
        return isZIP(ext) || isJAR(ext) || isAAR(ext) || isTAR(ext);
    }

}
