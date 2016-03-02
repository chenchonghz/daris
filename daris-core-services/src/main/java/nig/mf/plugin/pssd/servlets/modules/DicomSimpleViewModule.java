package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import nig.mf.plugin.pssd.servlets.DicomServlet;

public class DicomSimpleViewModule implements Module {

    public static final DicomSimpleViewModule INSTANCE = new DicomSimpleViewModule();

    public static final String NAME = DicomServlet.ModuleName.simpleview.name();

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
        // idx
        String idxStr = request.variableValue(DicomServlet.ARG_IDX);
        int idx = idxStr == null ? 1 : Integer.parseInt(idxStr);
        // asset meta
        XmlDoc.Element ae = server
                .execute(sessionKey, "asset.get",
                        cid == null ? ("<id>" + id + "</id>")
                                : ("<cid>" + cid + "</cid>"),
                        null, null)
                .element("asset");
        if (id == null) {
            id = ae.value("@id");
        }
        // frame
        String frameStr = request.variableValue(DicomServlet.ARG_FRAME);
        int frame = frameStr == null ? 1 : Integer.parseInt(frameStr);

        int size = ae.intValue("meta/mf-dicom-series/size");
        // dicom meta
        XmlDoc.Element dcmm = DicomMetadataGetModule.getDicomMetadata(server,
                sessionKey, id, idx, true);
        int nbFrames = ae.intValue("de[@tag='00280008']/value", 1);
        String headerHtml = DicomMetadataGetModule.toHtmlString(dcmm, true);

        // generate response HTML
        String html = makeHtml(server, sessionKey, id, idx, size, frame,
                nbFrames, headerHtml);
        //
        response.setContent(html, "text/html");
    }

    private static String generateImageUrl(SessionKey sessionKey, String id,
            int idx, int frame, int nbFrames) {
        StringBuilder sb = new StringBuilder();
        sb.append(DicomServlet.URL_BASE);
        sb.append("?_skey=").append(sessionKey.key());
        sb.append("&module=").append(DicomImageGetModule.NAME);
        sb.append("&disposition=inline");
        sb.append("&id=").append(id);
        sb.append("&idx=").append(idx);
        if (nbFrames > 1) {
            sb.append("&frame=").append(frame);
        }
        sb.append("&filename=")
                .append(id + "_" + idx + (frame > 0 ? ("_" + frame) : ""))
                .append("_dcm.png");
        return sb.toString();
    }

    private static String generateViewerUrl(SessionKey sessionKey, String id,
            int idx, int frame, int nbFrames) {
        StringBuilder sb = new StringBuilder();
        sb.append(DicomServlet.URL_BASE);
        sb.append("?_skey=").append(sessionKey.key());
        sb.append("&module=").append(NAME);
        sb.append("&id=").append(id);
        sb.append("&idx=").append(idx);
        if (nbFrames > 1) {
            sb.append("&frame=").append(frame);
        }
        return sb.toString();
    }

    private static String makeHtml(HttpServer server, SessionKey sessionKey,
            String id, int idx, int size, int frame, int nbFrames,
            String headerHtml) throws Throwable {
        String imageUrl = generateImageUrl(sessionKey, id, idx, frame,
                nbFrames);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<title>DaRIS Simple DICOM Viewer</title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append(
                "html, body { font-size:9pt; font-family:'Lucida Grande',Verdana,Arial,Sans-Serif; width:100%; height:100%; margin:0; padding:0; }\n");
        sb.append(
                "div#container { display:flex; flex-flow:column; height:100%; }\n");
        sb.append("div#content { flex: 1 1 auto; overflow:hidden; }\n");
        sb.append("div#footer  { flex: 0 1 30px; }\n");
        sb.append(
                "ul#tabs { list-style-type: none; margin: 10px 0 0 0; padding: 0 0 0.3em 0; }\n");
        sb.append("ul#tabs li { display: inline; }\n");
        sb.append(
                "ul#tabs li a { color: #42454a; background-color: #dedbde; border: 1px solid #c9c3ba; border-bottom: none; border-radius:5px 5px 0 0; padding:0.3em; text-decoration: none; }\n");
        sb.append("ul#tabs li a:hover { background-color: #f1f0ee; }\n");
        sb.append(
                "ul#tabs li a.selected { color:#000; background-color:#f1f0ee; font-weight:bold; padding:0.3em 0.3em 0.38em 0.3em; }\n");
        sb.append(
                "div.tabContent { border:1px solid #c9c3ba; border-radius:0 0 5px 5px; background-color:#f1f0ee; height:93%; }\n");
        sb.append("div.tabContent.hide { display:none; }\n");
        sb.append("</style>\n");
        sb.append("<script type=\"text/javascript\">\n");
        sb.append("//<![CDATA[\n");
        sb.append("var tabLinks = new Array();\n");
        sb.append("var contentDivs = new Array();\n");
        sb.append("function init() {\n");
        sb.append(
                "    var tabListItems = document.getElementById('tabs').childNodes;\n");
        sb.append("    for ( var i = 0; i < tabListItems.length; i++ ) {\n");
        sb.append("        if ( tabListItems[i].nodeName == \"LI\" ) {\n");
        sb.append(
                "            var tabLink = getFirstChildWithTagName( tabListItems[i], 'A' );\n");
        sb.append(
                "            var id = getHash( tabLink.getAttribute('href') );\n");
        sb.append("            tabLinks[id] = tabLink;\n");
        sb.append(
                "            contentDivs[id] = document.getElementById( id );\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    var i = 0;\n");
        sb.append("    for ( var id in tabLinks ) {\n");
        sb.append("        tabLinks[id].onclick = showTab;\n");
        sb.append(
                "        tabLinks[id].onfocus = function() { this.blur() };\n");
        sb.append(
                "        if ( i == 0 ) tabLinks[id].className = 'selected';\n");
        sb.append("        i++;\n");
        sb.append("    }\n");
        sb.append("    var i = 0;\n");
        sb.append("    for ( var id in contentDivs ) {\n");
        sb.append(
                "        if ( i != 0 ) contentDivs[id].className = 'tabContent hide';\n");
        sb.append("        i++;\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("function showTab() {\n");
        sb.append(
                "    var selectedId = getHash( this.getAttribute('href') );\n");
        sb.append("    for ( var id in contentDivs ) {\n");
        sb.append("        if ( id == selectedId ) {\n");
        sb.append("            tabLinks[id].className = 'selected';\n");
        sb.append("            contentDivs[id].className = 'tabContent';\n");
        sb.append("        } else {\n");
        sb.append("            tabLinks[id].className = '';\n");
        sb.append(
                "            contentDivs[id].className = 'tabContent hide';\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return false;\n");
        sb.append("}\n");
        sb.append("function getFirstChildWithTagName( element, tagName ) {\n");
        sb.append(
                "    for ( var i = 0; i < element.childNodes.length; i++ ) {\n");
        sb.append(
                "        if ( element.childNodes[i].nodeName == tagName ) return element.childNodes[i];\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("function getHash( url ) {\n");
        sb.append("    var hashPos = url.lastIndexOf ( '#' );\n");
        sb.append("    return url.substring( hashPos + 1 );\n");
        sb.append("}\n");
        sb.append("//]]>\n");
        sb.append("</script>\n");
        sb.append("</head>\n");
        sb.append("<body onload=\"init()\">\n");
        sb.append("  <div id=\"container\">\n");
        sb.append("    <div id=\"content\">\n");
        sb.append("      <ul id=\"tabs\">\n");
        sb.append(
                "        <li><a href=\"#image\">&nbsp;DICOM Image&nbsp;</a></li>\n");
        sb.append(
                "        <li><a href=\"#metadata\">&nbsp;Metadata Header&nbsp;</a></li>\n");
        sb.append("      </ul>\n");
        sb.append("      <div class=\"tabContent\" id=\"image\">\n");
        sb.append(
                "        <div style=\"vertical-align:middle; text-align:center; width:100%; height:100%;\">\n");
        sb.append(
                "          <img style=\"max-width:100%; max-height:100%; width:auto; height:100%;\" src=\"")
                .append(imageUrl).append("\"/>\n");
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append(
                "      <div class=\"tabContent\" style=\"overflow:scroll;\" id=\"metadata\">\n");
        sb.append(headerHtml);
        sb.append("      </div>\n");
        sb.append("    </div>\n");
        sb.append(
                "  <div id=\"actions\"  style=\"vertical-align:middle; text-align:center;\">\n");
        addButtons(sessionKey, sb, id, idx, size, frame, nbFrames);
        sb.append("  </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    private static void addButtons(SessionKey sessionKey, StringBuilder sb,
            String id, int idx, int size, int frame, int nbFrames) {
        sb.append("<p>");
        if (idx > 1) {
            // |<
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, 1, frame,
                            nbFrames))
                    .append("'\">").append("|&lt;").append("</button>");
            // <<
            int step = size / 10;
            int idx1 = idx - step;
            if (idx1 < 1) {
                idx1 = 1;
            }
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, idx1, frame,
                            nbFrames))
                    .append("'\">").append("&lt;&lt;").append("</button>");
            // <
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, idx - 1, frame,
                            nbFrames))
                    .append("'\">").append("&lt;").append("</button>");
        } else {
            sb.append("<button disabled>").append("|&lt;").append("</button>");
            sb.append("<button disabled>").append("&lt;&lt;")
                    .append("</button>");
            sb.append("<button disabled>").append("&lt;").append("</button>");
        }
        sb.append("<b>[").append(idx).append("/").append(size).append("]</b>");
        if (idx < size) {
            // >
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, idx + 1, frame,
                            nbFrames))
                    .append("'\">").append("&gt;").append("</button>");
            // >>
            int step = size / 10;
            int idx2 = idx + step;
            if (idx2 > size) {
                idx2 = size;
            }
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, idx2, frame,
                            nbFrames))
                    .append("'\">").append("&gt;&gt;").append("</button>");
            // >|
            sb.append("<button onclick=\"window.location.href='")
                    .append(generateViewerUrl(sessionKey, id, size, frame,
                            nbFrames))
                    .append("'\">").append("&gt;|").append("</button>");
        } else {
            sb.append("<button disabled>").append("&gt;").append("</button>");
            sb.append("<button disabled>").append("&gt;&gt;")
                    .append("</button>");
            sb.append("<button disabled>").append("&gt;|").append("</button>");
        }
        sb.append("</p>\n");
        if (nbFrames > 1) {
            sb.append("<p>");
            if (frame > 1) {
                // |<
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx, 1,
                                nbFrames))
                        .append("'\">").append("|&lt;").append("</button>");
                // <<
                int step = nbFrames / 10;
                int frame1 = frame - step;
                if (frame1 < 1) {
                    frame1 = 1;
                }
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx, frame1,
                                nbFrames))
                        .append("'\">").append("&lt;&lt;").append("</button>");
                // <
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx,
                                frame - 1, nbFrames))
                        .append("'\">").append("&lt;").append("</button>");
            } else {
                sb.append("<button disabled>").append("|&lt;")
                        .append("</button>");
                sb.append("<button disabled>").append("&lt;")
                        .append("</button>");
                sb.append("<button disabled>").append("&lt;")
                        .append("</button>");
            }
            sb.append("<b>[frame: ").append(frame).append("/").append(nbFrames)
                    .append("]</b>");
            if (frame < nbFrames) {
                // >
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx,
                                frame + 1, nbFrames))
                        .append("'\">").append("&gt;").append("</button>");
                // >>
                int step = frame / 10;
                int frame2 = frame + step;
                if (frame2 > nbFrames) {
                    frame2 = nbFrames;
                }
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx, frame2,
                                nbFrames))
                        .append("'\">").append("&gt;&gt;").append("</button>");
                // >|
                sb.append("<button onclick=\"window.location.href='")
                        .append(generateViewerUrl(sessionKey, id, idx, nbFrames,
                                nbFrames))
                        .append("'\">").append("&gt;|").append("</button>");

            } else {
                sb.append("<button disabled>").append("&gt;")
                        .append("</button>");
                sb.append("<button disabled>").append("&gt;&gt;")
                        .append("</button>");
                sb.append("<button disabled>").append("&gt;|")
                        .append("</button>");
            }
            sb.append("</p>\n");
        }
    }
}
