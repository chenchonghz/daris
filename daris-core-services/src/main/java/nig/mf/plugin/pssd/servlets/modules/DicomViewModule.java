package nig.mf.plugin.pssd.servlets.modules;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import nig.mf.plugin.pssd.services.SvcArchiveContentGet;
import nig.mf.plugin.pssd.servlets.DicomServlet;

public class DicomViewModule implements Module {

    public static final DicomViewModule INSTANCE = new DicomViewModule();

    public static final String NAME = DicomServlet.ModuleName.view.name();

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

        XmlDoc.Element ae = null;
        if (id == null) {
            ae = server.execute(sessionKey, "asset.get",
                    "<cid>" + cid + "</cid>", null, null).element("asset");
            id = ae.value("@id");
        } else {
            ae = server.execute(sessionKey, "asset.get", "<id>" + id + "</id>",
                    null, null).element("asset");
            cid = ae.value("cid");
        }
        String cType = ae.value("content/type");
        String cExt = ae.value("content/type/@ext");
        if (!ae.elementExists("meta/mf-dicom-series")
                || !SvcArchiveContentGet.isArchiveTypeSupported(cExt, cType)) {
            throw new Exception(
                    "Asset " + id + " is not a valid DICOM series.");
        }
        int size = ae.intValue("meta/mf-dicom-series/size", 0);
        if (size <= 0) {
            throw new Exception("Asset " + id
                    + " is not a valid DICOM series. Contains no DICOM file.");
        }
        List<String> imgUrls = generateDicomImageUrls(sessionKey, id, size);
        StringBuilder html = new StringBuilder();
        generateResponseHtml(html, imgUrls, "DICOM Image Series " + id);
        response.setContent(html.toString(), "text/html");
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
        html.append("params['images']=[[");
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
        html.append("]];\n");
        html.append("</script>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"papaya\" data-params=\"params\"></div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
    }

    private static List<String> generateDicomImageUrls(SessionKey sessionKey,
            String assetId, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append(DicomServlet.URL_BASE);
        sb.append("?_skey=");
        sb.append(sessionKey.key());
        sb.append("&module=file&disposition=attachment&id=");
        sb.append(assetId);
        sb.append("&idx=");
        String baseUrl = sb.toString();
        List<String> urls = new ArrayList<String>(size);
        for (int i = 1; i <= size; i++) {
            StringBuilder url = new StringBuilder();
            url.append(baseUrl);
            url.append(i);
            url.append("&filename=");
            url.append(String.format("%05d.dcm", i));
            urls.add(url.toString());
        }
        return urls;
    }

}