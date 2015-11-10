package nig.mf.plugin.pssd.servlets.modules;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.servlets.DicomServlet;
import nig.mf.plugin.pssd.servlets.NiftiServlet;

public class NiftiViewModule implements Module {

    public static final NiftiViewModule INSTANCE = new NiftiViewModule();

    public static final String NAME = NiftiServlet.ModuleName.view.name();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // id
        String id = request.variableValue(DicomServlet.ARG_ID);
        // cid
        String cid = request.variableValue(DicomServlet.ARG_CID);
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        XmlDoc.Element ae = server
                .execute(sessionKey, "asset.get", dm.root(), null, null)
                .element("asset");
        if (id == null) {
            id = ae.value("@id");
        } else {
            cid = ae.value("cid");
        }

        NiftiFileGetModule.validate(ae);

        String cType = ae.value("content/type");
        String cExt = ae.value("content/type/@ext");

        List<String> imgUrls = new ArrayList<String>();
        if (NiftiFileGetModule.isNII(cExt)) {
            imgUrls.add(generateImgUrl(sessionKey, id, id + ".nii"));
        } else if (NiftiFileGetModule.isGZ(cExt)) {
            imgUrls.add(generateImgUrl(sessionKey, id, id + ".nii.gz"));
        } else if (NiftiFileGetModule.isArchive(cExt)) {
            List<XmlDoc.Element> ees = server.execute(sessionKey,
                    "daris.archive.content.list", dm.root(), null, null)
                    .elements("entry");
            imgUrls.addAll(generateImgUrls(sessionKey, id, ees));
        }
        if (imgUrls.isEmpty()) {
            throw new Exception("No image url is generated. Probably asset "
                    + id + " is not a valid NIFTI series. Content type: "
                    + cType);
        }

        StringBuilder html = new StringBuilder();
        generateResponseHtml(html, imgUrls, "NIFTI Image Series " + id);
        response.setContent(html.toString(), "text/html");
    }

    private static String generateImgUrl(SessionKey sessionKey, String assetId,
            String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(NiftiServlet.URL_BASE);
        sb.append("?_skey=");
        sb.append(sessionKey.key());
        sb.append("&module=file&disposition=attachment&id=");
        sb.append(assetId);
        sb.append("&filename=");
        sb.append(fileName);
        return sb.toString();
    }

    private static List<String> generateImgUrls(SessionKey sessionKey,
            String assetId, List<Element> ees) {
        List<String> urls = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        sb.append(NiftiServlet.URL_BASE);
        sb.append("?_skey=");
        sb.append(sessionKey.key());
        sb.append("&module=file&disposition=attachment&id=");
        sb.append(assetId);
        sb.append("&idx=");
        String base = sb.toString();
        if (ees != null && !ees.isEmpty()) {
            for (int i = 0; i < ees.size(); i++) {
                XmlDoc.Element ee = ees.get(i);
                String name = ee.value();
                if (isNiftiFileName(name)) {
                    StringBuilder url = new StringBuilder(base);
                    url.append(i + 1);
                    url.append("&filename=");
                    if (name.endsWith(".nii") || name.endsWith(".NII")) {
                        url.append(assetId + "_" + (i + 1) + ".nii");
                    } else {
                        url.append(assetId + "_" + (i + 1) + ".nii.gz");
                    }
                    urls.add(url.toString());
                }
            }
        }
        return urls;
    }

    private static boolean isNiftiFileName(String fileName) {
        return fileName != null && (fileName.endsWith(".nii")
                || fileName.endsWith(".nii.gz") || fileName.endsWith(".NII")
                || fileName.endsWith(".NII.GZ"));
    }

    private static void generateResponseHtml(StringBuilder html,
            List<String> imgUrls, String title) {
        html.append("<!DOCTYPE html>\n");
        html.append(
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">\n");
        html.append("<head>\n");
        html.append(
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n");
        html.append("<!-- iOS meta tags -->\n");
        html.append(
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"/>\n");
        html.append(
                "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n");
        html.append(
                "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black-translucent\">\n");
        html.append(
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://rii.uthscsa.edu/mango/papaya/papaya.css?version=0.8&build=895\" />\n");
        html.append(
                "<script type=\"text/javascript\" src=\"http://rii.uthscsa.edu/mango/papaya/papaya.js?version=0.8&build=895\"></script>\n");
        html.append("<title>");
        html.append(title);
        html.append("</title>\n");
        html.append("<script type=\"text/javascript\">\n");
        html.append("var params=[];\n");
        html.append("params['images']=[");
        int size = imgUrls.size();
        for (int i = 0; i < size; i++) {
            String imgUrl = imgUrls.get(i);
            html.append("'");
            html.append(imgUrl);
            html.append("'");
            if (i < size - 1) {
                html.append(',');
            }
        }
        html.append("];\n");
        html.append("</script>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"papaya\" data-params=\"params\"></div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
    }
}
