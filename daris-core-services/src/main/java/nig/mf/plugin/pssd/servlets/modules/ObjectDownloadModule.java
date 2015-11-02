package nig.mf.plugin.pssd.servlets.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.Transcode;
import nig.mf.plugin.pssd.servlets.AbstractServlet;
import nig.mf.plugin.pssd.servlets.Disposition;
import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.ObjectServlet;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import nig.mf.plugin.pssd.servlets.TranscodeCodec;
import arc.mf.plugin.http.HttpOutputStream;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpResponse.ContentAndCheckSum;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.streams.SizedInputStream;
import arc.streams.generator.InputToOutputStreamGenerator;
import arc.streams.generator.MultiOutputStreamGenerator;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ObjectDownloadModule implements Module {

    public static final ObjectDownloadModule INSTANCE = new ObjectDownloadModule();

    public static final String NAME = ObjectServlet.ModuleName.download.name();

    public static final long GB1 = 1073741824L;

    public static final long GB2 = 2147483648L;

    public static class Range {
        public final long from;
        public final long to;

        public Range(long from, long to) {
            this.from = from;
            this.to = to;
        }

        public final long offset() {
            return this.from;
        }

        public final long length() {
            return this.to - this.from + 1L;
        }

        public static List<Range> parse(List<XmlDoc.Element> res)
                throws Throwable {
            if (res != null && !res.isEmpty()) {
                List<Range> ranges = new ArrayList<Range>();
                for (XmlDoc.Element re : res) {
                    Range range = parse(re);
                    if (range != null) {
                        ranges.add(range);
                    }
                }
                if (!ranges.isEmpty()) {
                    return ranges;
                }
            }
            return null;
        }

        public static Range parse(XmlDoc.Element re) throws Throwable {
            if (re != null) {
                long offset = re.longValue("offset");
                long length = re.longValue("length");
                return new Range(offset, offset + length - 1L);
            }
            return null;
        }

        public static List<Range> parse(HttpRequest request) {
            if (request != null) {
                String rangeLine = request.headerField("Range");
                if (rangeLine != null) {
                    return parse(rangeLine);
                }
            }
            return null;
        }

        public static List<Range> parse(String rangeLine) {
            if (rangeLine != null) {
                int idx = rangeLine.indexOf('=');
                if (idx >= 0) {
                    String rs = rangeLine.substring(idx + 1);
                    List<Range> ranges = new ArrayList<Range>();
                    StringTokenizer tokens = new StringTokenizer(rs, ",");
                    while (tokens.hasMoreTokens()) {
                        String token = tokens.nextToken();
                        String[] parts = token.split("-");
                        long from = 0L;
                        long to = Long.MAX_VALUE;
                        if (parts.length == 2) {
                            try {
                                from = Long.parseLong(parts[0]);
                                to = Long.parseLong(parts[1]);
                            } catch (Throwable t) {
                                // Error parsing the ranges, just ignore range
                                // command
                                return null;
                            }
                        } else if (parts.length == 1) {
                            try {
                                from = Long.parseLong(parts[0]);
                            } catch (Throwable t) {
                                // Error parsing the ranges, just ignore range
                                // command
                                return null;
                            }
                        } else {
                            // Error parsing the ranges, just ignore range
                            // command
                            return null;
                        }
                        if (from == 0L && to == Long.MAX_VALUE) {
                            // Error parsing the ranges or full range, just
                            // ignore range command
                            return null;
                        }
                        ranges.add(new Range(from, to));
                    }
                    if (ranges.size() > 0) {
                        return ranges;
                    }
                }
            }
            return null;
        }
    }

    private static class DownloadArguments {

        public String cid;
        public boolean proceed;
        public boolean recursive;
        public List<Transcode> transcodes;
        public List<Range> ranges;
        public Disposition disposition;
        public String filename;
        public String token;

        public boolean destroySession() {
            return token != null;
        }

        public boolean transcodeExists() {
            return transcodes != null && !transcodes.isEmpty();
        }

        static DownloadArguments parse(HttpRequest request,
                String originalFileName) throws Throwable {

            DownloadArguments args = new DownloadArguments();
            args.cid = request.variableValue(ObjectServlet.ARG_CID);
            args.proceed = Boolean.parseBoolean(request.variableValue(
                    ObjectServlet.ARG_PROCEED, Boolean.toString(true)));
            args.recursive = Boolean.parseBoolean(request.variableValue(
                    ObjectServlet.ARG_RECURSIVE, Boolean.toString(false)));
            args.transcodes = TranscodeCodec.decodeTranscodes(request);
            args.ranges = Range.parse(request);
            args.disposition = Disposition.parse(
                    request.variableValue(ObjectServlet.ARG_DISPOSITION),
                    Disposition.attachment);
            args.filename = request.variableValue(ObjectServlet.ARG_FILENAME);
            if (args.filename == null) {
                if (originalFileName != null) {
                    args.filename = originalFileName;
                } else if (args.cid != null) {
                    String type = CIDUtil.getType(args.cid);
                    args.filename = type + "_" + args.cid;
                }
            }
            args.token = request.variableValue(ObjectServlet.ARG_TOKEN);
            return args;
        }
    }

    private ObjectDownloadModule() {
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // parse the cid from the http request
        String cid = request.variableValue(ObjectServlet.ARG_CID);
        // get the object metadata
        XmlDoc.Element oe = ObjectDescribeModule.describe(server, sessionKey,
                cid);
        // get the object original file name if any
        String originalFileName = oe.value("filename");
        // parse all arguments from the http request
        DownloadArguments args = DownloadArguments.parse(request,
                originalFileName);
        if (args.proceed) {
            download(server, sessionKey, args, request, response);
        } else {
            HtmlBuilder html = new HtmlBuilder(urlFor(args.cid, false));
            outputHtml(server, sessionKey, args, html);
            response.setContent(html.buildHtml(), "text/html");
        }
    }

    private static Set<String> getChildrenMimeTypes(HttpServer server,
            SessionKey sessionKey, String cid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "cid starts with '" + cid + "' and asset has content");
        dm.add("xpath", new String[] { "ename", "mtype" }, "type");
        dm.add("action", "get-value");
        dm.add("size", "infinity");
        Collection<String> mtypes = server
                .execute(sessionKey, "asset.query", dm.root())
                .values("asset/mtype");
        if (mtypes == null || mtypes.isEmpty()) {
            return null;
        }
        Set<String> mimeTypes = new HashSet<String>();
        for (String mtype : mtypes) {
            if (mtype != null) {
                mimeTypes.add(mtype);
            }
        }
        if (mimeTypes.isEmpty()) {
            return null;
        } else {
            return mimeTypes;
        }
    }

    private static void download(HttpServer server, SessionKey sessionKey,
            DownloadArguments args, HttpRequest request, HttpResponse response)
                    throws Throwable {
        if (args.recursive) {
            shoppingcartDownload(server, sessionKey, args.cid, args.filename,
                    args.transcodes, request, response);
        } else {
            if (!args.transcodeExists()) {
                // not recursive, no transcode, just a direct download
                // Note: transcodeFrom is ignored
                directDownload(server, sessionKey, args.cid, args.disposition,
                        args.filename, args.ranges, response,
                        args.destroySession());
            } else {
                // not recursive, do transcode then download
                String transcodeTo = args.transcodes.get(0).to;
                transcodeDownload(server, sessionKey, args.cid, transcodeTo,
                        args.disposition, args.filename, response,
                        args.destroySession());
            }
        }

    }

    private static boolean transcodeSelected(List<Transcode> selectedTranscodes,
            String from, String to) {
        if (selectedTranscodes == null || selectedTranscodes.isEmpty()) {
            return false;
        }
        for (Transcode transcode : selectedTranscodes) {
            if (transcode.to != null) {
                if (transcode.from != null) {
                    return transcode.from.equals(from)
                            && transcode.to.equals(to);
                } else {
                    return transcode.to.equals(to);
                }
            }
        }
        return false;
    }

    private static void outputHtml(HttpServer server, SessionKey sessionKey,
            DownloadArguments args, HtmlBuilder html) throws Throwable {
        XmlDoc.Element oe = ObjectDescribeModule.describe(server, sessionKey,
                args.cid);
        String cid = oe.value("id");
        String type = oe.value("@type");
        String mimeType = oe.value("type");
        Set<String> cMimeTypes = getChildrenMimeTypes(server, sessionKey, cid);
        boolean hasContent = mimeType != null
                && oe.longValue("data/size", 0) > 0;
        boolean containsDataSet = cMimeTypes != null;

        html.setTitle("DaRIS: " + type + " " + cid + " - download");
        html.addStyle("tr:nth-child(even) {background:#eee;}");
        html.addStyle("tr:nth-child(odd) {background:#eee;}");
        html.addStyle(
                "th {font-size:1em; line-height:1.5em; font-weight:bold;}");
        html.addStyle("td {font-size:1em; line-height:1.5em;}");
        html.addStyle("input {font-size:1em; line-height:1.5em; width:200px;}");
        html.addStyle(
                "select {font-size:1em; line-height:1.5em; width:200px;}");
        html.appendToHead("<script type=\"text/javascript\">\n");
        html.appendToHead("function start() {\n");
        html.appendToHead(
                "    var selects = document.getElementsByName('transcode_select');\n");
        html.appendToHead("    var transcodes = '';\n");
        html.appendToHead("    for(var i=0;i<selects.length;i++) {\n");
        html.appendToHead(
                "        if (selects[i].value=='none') { continue; }\n");
        html.appendToHead("        if (i>0) { transcodes += '"
                + ObjectServlet.TRANSCODE_TOKEN_SEPARATOR + "'; }\n");
        html.appendToHead("        transcodes += selects[i].value;\n");
        html.appendToHead("    }\n");
        html.appendToHead("    document.getElementById('"
                + ObjectServlet.ARG_TRANSCODE + "').value=transcodes;\n");
        html.appendToHead("    document.getElementById('"
                + ObjectServlet.ARG_PROCEED + "').value=true;\n");
        html.appendToHead(
                "    document.getElementById('download_form').submit()\n");
        html.appendToHead("}\n");
        html.appendToHead("</script>\n");

        // nav bar items
        boolean isRepository = cid == null;
        html.addNavItem("DaRIS",
                isRepository ? null : ObjectListModule.urlFor(null));
        if (!isRepository) {
            String projectId = CIDUtil.getProjectId(cid);
            boolean isProject = CIDUtil.isProjectId(cid);
            html.addNavItem("Project " + projectId,
                    isProject ? null : urlFor(projectId, false));
            if (!isProject) {
                String subjectId = CIDUtil.getSubjectId(cid);
                boolean isSubject = CIDUtil.isSubjectId(cid);
                html.addNavItem("Subject " + subjectId,
                        isSubject ? null : urlFor(subjectId, false));
                if (!isSubject) {
                    String exMethodId = CIDUtil.getExMethodId(cid);
                    boolean isExMethod = CIDUtil.isExMethodId(cid);
                    html.addNavItem("Ex-method " + exMethodId,
                            isExMethod ? null : urlFor(exMethodId, false));
                    if (!isExMethod) {
                        String studyId = CIDUtil.getStudyId(cid);
                        boolean isStudy = CIDUtil.isStudyId(cid);
                        html.addNavItem("Study " + studyId,
                                isStudy ? null : urlFor(studyId, false));
                        if (!isStudy) {
                            String dataSetId = CIDUtil.getDataSetId(cid);
                            boolean isDataSet = CIDUtil.isDataSetId(cid);
                            html.addNavItem("Dataset " + dataSetId, isDataSet
                                    ? null : urlFor(dataSetId, false));
                        }
                    }
                }
            }
        }

        // tab bar items
        html.addTabItem("Members",
                CIDUtil.isDataSetId(cid) ? "#" : ObjectListModule.urlFor(cid));
        html.addTabItem("Details",
                cid == null ? "#" : ObjectDescribeModule.urlFor(cid));
        html.addTabItem("Download", null);

        // content
        html.appendContent(
                "<div id=\"content_div\" width=\"100%\" height=\"100%\" style=\"background-color:#eee;\">\n");

        Set<String> mimeTypes = new HashSet<String>();
        boolean canChangeRecursive = false;
        if (!hasContent && !containsDataSet) {
            html.appendContent(
                    "<br/><br/><br/><br/><br/><p align=\"center\" style=\"margin:0px; padding:20px;\">Cannot download "
                            + type + " " + cid
                            + ". It have no content and contains no dataset.</p><br/><br/><br/><br/><br/>");
            return;
        } else if (hasContent && containsDataSet) {
            canChangeRecursive = true;
            mimeTypes.add(mimeType);
            mimeTypes.addAll(cMimeTypes);
        } else if (hasContent && !containsDataSet) {
            args.recursive = false;
            mimeTypes.add(mimeType);
        } else if (!hasContent && containsDataSet) {
            args.recursive = true;
            mimeTypes.addAll(cMimeTypes);
        }

        html.appendContent(
                "<form id=\"download_form\" method=\"post\" action=\""
                        + ObjectServlet.URL_BASE
                        + "\" enctype=\"multipart/form-data\">\n");
        html.appendContent("<input type=\"hidden\" name=\""
                + ObjectServlet.ARG_MODULE + "\" value=\"" + NAME + "\"/>\n");
        html.appendContent("<input type=\"hidden\" name=\""
                + ObjectServlet.ARG_CID + "\" value=\"" + cid + "\"/>\n");
        html.appendContent("<input type=\"hidden\" name=\""
                + ObjectServlet.ARG_PROCEED + "\" value=\""
                + Boolean.toString(args.proceed) + "\" id=\"proceed\"/>\n");
        html.appendContent("<input type=\"hidden\" name=\""
                + ObjectServlet.ARG_RECURSIVE + "\" value=\""
                + Boolean.toString(args.recursive) + "\" id=\"recursive\"/>\n");
        if (args.token != null) {
            html.appendContent(
                    "<input type=\"hidden\" name=\"" + ObjectServlet.ARG_TOKEN
                            + "\" value=\"" + args.token + "\"/>\n");
        }
        html.appendContent("<input type=\"hidden\" name=\""
                + ObjectServlet.ARG_TRANSCODE + "\" id=\"transcode\"/>\n");

        html.appendContent(
                "<table width=\"100%\" style=\"padding-top:20px; padding-bottom:20px;\">\n");
        html.appendContent("<tbody>\n");

        if (canChangeRecursive) {
            html.appendContent(
                    "<tr><th align=\"right\" width=\"50%\">Type:</th><td align=\"left\" width=\"50%\"><input id=\"recursive_checkbox\" type=\"checkbox\" value=\"Recursive\" checked=\""
                            + args.recursive
                            + "\" onclick=\"document.getElementById('recursive').value=document.getElementById('recursive_checkbox').checked; submit();\"/></td></tr>\n");
        }

        for (String fromMimeType : mimeTypes) {
            Set<String> toMimeTypes = getTranscodeToMimeTypes(server,
                    sessionKey, fromMimeType);
            if (toMimeTypes != null) {
                html.appendContent(
                        "<tr><th align=\"right\" width=\"50%\">Transcode '"
                                + fromMimeType
                                + "' to:</th><td align=\"left\" width=\"50%\"><select name=\"transcode_select\">\n");
                boolean hasSelected = false;
                for (String toMimeType : toMimeTypes) {
                    html.appendContent("<option value=\"" + fromMimeType
                            + ObjectServlet.TRANSCODE_VALUE_SEPARATOR
                            + toMimeType + "\"");
                    if (transcodeSelected(args.transcodes, fromMimeType,
                            toMimeType)) {
                        html.appendContent(" selected=\"true\"");
                        hasSelected = true;
                    }
                    html.appendContent(">" + toMimeType + "</option>\n");
                }
                html.appendContent("<option value=\"none\"");
                if (!hasSelected) {
                    html.appendContent(" selected=\"true\"");
                }
                html.appendContent(">none</option>\n");
                html.appendContent("</select></td></tr>\n");
            }
        }

        html.appendContent(
                "<tr><th align=\"right\" width=\"50%\">Disposition:</th><td align=\"left\" width=\"50%\"><select name=\""
                        + ObjectServlet.ARG_DISPOSITION + "\">");
        html.appendContent("<option value=\"" + Disposition.attachment + "\"");
        if (args.disposition == Disposition.attachment) {
            html.appendContent(" selected=\"true\"");
        }
        html.appendContent(">" + Disposition.attachment + "</option>");
        html.appendContent("<option value=\"" + Disposition.inline + "\"");
        if (args.disposition == Disposition.inline) {
            html.appendContent(" selected=\"true\"");
        }
        html.appendContent(">" + Disposition.inline + "</option>");
        html.appendContent("</select></td></tr>\n");

        html.appendContent(
                "<tr><th align=\"right\" width=\"50%\">File Name:</th><td align=\"left\" width=\"50%\"><input type=\"text\" name=\""
                        + ObjectServlet.ARG_FILENAME + "\"");
        if (args.filename != null) {
            html.appendContent(" value=\"" + args.filename + "\"");
        }
        html.appendContent(" style=\"width:580px\"/></td></tr>\n");

        html.appendContent("<tr><td colspan=\"2\" align=\"center\">");
        html.appendContent("&nbsp;");
        html.appendContent("</td></tr>\n");
        html.appendContent("<tr><td colspan=\"2\" align=\"center\">");
        html.appendContent(
                "<button type=\"button\" onclick=\"start();\" style=\"width:80px; font-size:1em; line-height:1.5em;\">Start</button>");
        html.appendContent("</td></tr>\n");
        html.appendContent("</tbody>\n");
        html.appendContent("</table>\n");
        html.appendContent("</form>\n");
        html.appendContent("</div>\n");
    }

    private static Set<String> getTranscodeToMimeTypes(HttpServer server,
            SessionKey sessionKey, String fromType) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("from", fromType);
        Collection<String> types = server
                .execute("transcode.describe", dm.root())
                .values("transcode/to");
        if (types == null || types.isEmpty()) {
            return null;
        }
        Set<String> toTypes = new HashSet<String>();
        for (String type : types) {
            if (type != null) {
                toTypes.add(type);
            }
        }
        if (toTypes.isEmpty()) {
            return null;
        }
        return toTypes;
    }

    public static void directDownload(HttpServer server, SessionKey sessionKey,
            String cid, Disposition disposition, String filename,
            List<Range> ranges, HttpResponse response, boolean destroySession)
                    throws Throwable {
        try {
            XmlDoc.Element oe = ObjectDescribeModule.describe(server,
                    sessionKey, cid);
            String assetId = oe.value("id/@asset");
            boolean hasContent = oe.elementExists("data");
            boolean ranged = ranges != null && !ranges.isEmpty();
            if (hasContent) {
                try {
                    String ctype = oe.value("data/type");
                    String ext = oe.value("data/type/@ext");
                    if (ctype != null) {
                        response.setHeaderField("Content-Type", ctype);
                    } else {
                        response.setHeaderField("Content-Type",
                                AbstractServlet.CONTENT_UNKNOWN);
                    }
                    if (filename == null) {
                        filename = cid;
                    }
                    if (ext != null && !filename.endsWith(ext)) {
                        filename = filename + "." + ext;
                    }
                    XmlDocMaker dm = new XmlDocMaker("args");
                    dm.add("id", assetId);
                    if (ranged) {
                        for (Range range : ranges) {
                            dm.push("range");
                            dm.add("offset", range.offset());
                            dm.add("length", range.length());
                            dm.pop();
                        }
                    }
                    XmlDoc.Element e = server.execute(sessionKey,
                            "asset.content.get", dm.root(), (HttpRequest) null,
                            response);
                    if (ranged) {
                        convertToRangeResponse(e, response);
                    } else {
                        response.setHeaderField("Content-Disposition",
                                disposition.name() + "; filename=\"" + filename
                                        + "\"");
                    }
                    response.setHeaderField("Accept-Ranges", "bytes");
                } catch (Throwable t) {
                    String error = "Unable to retrieve content of object " + cid
                            + ": " + t.getMessage();
                    response.setContent(error, "text/html");
                }
            } else {
                // no content, return the metadata xml.
                ObjectDescribeModule.describe(server, sessionKey, cid,
                        OutputFormat.xml, response);
            }
        } finally {
            if (destroySession) {
                server.destroySession(sessionKey);
            }
        }
    }

    private static void convertToRangeResponse(XmlDoc.Element xe,
            HttpResponse response) throws Throwable {
        response.setStatus(AbstractServlet.STATUS_PARTIAL_CONTENT);
        long fullRangeSize = xe.longValue("full-range-size");
        List<Range> ranges = Range.parse(xe.elements("range"));
        if (ranges != null) {
            int size = ranges.size();
            if (size == 1) {
                Range range = ranges.get(0);
                response.setHeaderField("Content-Range", "bytes " + range.from
                        + "-" + range.to + "/" + fullRangeSize);
                String ctype = response.contentType();
                if (ctype == null) {
                    response.setHeaderField("Content-Type",
                            AbstractServlet.CONTENT_UNKNOWN);
                }
            } else {
                ContentAndCheckSum cac = response.contentWithCheckSum(0);
                String ctype = cac.type();
                if (ctype == null) {
                    ctype = AbstractServlet.CONTENT_UNKNOWN;
                }
                String separator = "range_part_separator";
                MultiOutputStreamGenerator mosGen = new MultiOutputStreamGenerator(
                        "multipart/byteranges; boundary=" + separator);
                for (int i = 0; i < size; i++) {
                    Range range = ranges.get(i);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
                    HttpOutputStream hos = new HttpOutputStream(baos);
                    if (i == 0) {
                        hos.writeLine(null);
                    }
                    hos.writeLine("--" + separator);
                    hos.writeLine("Content-type: " + ctype);
                    hos.writeLine("Content-range: bytes " + range.from + "-"
                            + range.to + "/" + fullRangeSize);
                    hos.writeLine(null);
                    hos.flush();
                    byte[] bytes = baos.toByteArray();
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    mosGen.add(new InputToOutputStreamGenerator(
                            new SizedInputStream(bais, (long) bytes.length)));
                    mosGen.add(cac.generator());
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
                HttpOutputStream hos = new HttpOutputStream(baos);
                hos.writeLine(null);
                hos.writeLine("--" + separator + "--");
                hos.flush();
                byte[] bytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                mosGen.add(new InputToOutputStreamGenerator(
                        new SizedInputStream(bais, (long) bytes.length)));
                response.setContent(mosGen);
            }
        }

    }

    public static void transcodeDownload(HttpServer server,
            SessionKey sessionKey, String cid, String transcodeTo,
            Disposition disposition, String filename, HttpResponse response,
            boolean destroySession) throws Throwable {
        try {
            XmlDoc.Element oe = ObjectDescribeModule.describe(server,
                    sessionKey, cid);
            String assetId = oe.value("id/@asset");
            boolean hasContent = oe.elementExists("data");
            if (!hasContent) {
                throw new Exception("No content found in object " + cid + ".");
            }
            XmlDocMaker dm = new XmlDocMaker("args");

            dm.add("id", assetId);
            dm.push("transcode");
            dm.add("to", transcodeTo);
            dm.pop();
            server.execute(sessionKey, "asset.transcode", dm.root(),
                    (HttpRequest) null, response);

            if (response.contentType() == null) {
                response.setHeaderField("Content-Type",
                        AbstractServlet.CONTENT_UNKNOWN);
            }
            if (filename == null) {
                filename = cid;
            }
            String ext = extFromType(server, sessionKey,
                    response.contentType());
            if (ext != null && !filename.endsWith(ext)) {
                filename = filename + "." + ext;
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + filename + "\"");
        } finally {
            if (destroySession) {
                server.destroySession(sessionKey);
            }
        }
    }

    private static String extFromType(HttpServer server, SessionKey sessionKey,
            String contentType) throws Throwable {
        if (contentType == null) {
            return null;
        }
        if (AbstractServlet.CONTENT_UNKNOWN.equals(contentType)) {
            return null;
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", contentType);
        XmlDoc.Element re = server.execute(sessionKey, "type.describe",
                dm.root());
        return re.value("type/extension");
    }

    public static void shoppingcartDownload(HttpServer server,
            SessionKey sessionKey, String cid, String filename,
            List<Transcode> transcodes, HttpRequest request,
            HttpResponse response) throws Throwable {
        /*
         * create shopping cart
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        if (filename != null) {
            dm.add("name", filename);
        }
        dm.add("description", CIDUtil.getType(cid) + " " + cid);
        String sid = server
                .execute(sessionKey, "om.pssd.shoppingcart.create", dm.root())
                .value("sid");

        /*
         * add the object and its descendants into the cart
         */
        dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        dm.add("where", "(cid='" + cid + "' or cid starts with '" + cid
                + "') and asset has content");
        server.execute(sessionKey, "shopping.cart.content.add", dm.root());

        XmlDoc.Element ce = ShoppingCartDescribeModule.describe(server,
                sessionKey, sid);
        long contentSize = ce.longValue("content-statistics/item-size", 0);

        Collection<String> mimeTypes = ce
                .values("content-statistics/content-mimetype");
        if (contentSize == 0) {
            // No content, destroy the shopping cart
            ShoppingCartDestroyModule.destroy(server, sessionKey, sid);
            throw new Exception("No content found in shopping-cart " + sid
                    + ". The object and it descendants do not have contents.");
        }

        /*
         * set cart properties
         */
        dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        dm.add("delivery", "download");
        dm.push("packaging");
        dm.add("package-method",
                contentSize > GB2 ? Archive.Type.aar : Archive.Type.zip);
        dm.pop();
        if (transcodes != null && !transcodes.isEmpty()) {
            List<Transcode> validTranscodes = new ArrayList<Transcode>();
            if (mimeTypes != null && !mimeTypes.isEmpty()) {
                for (Transcode transcode : transcodes) {
                    if (transcode.from != null
                            && mimeTypes.contains(transcode.from)
                            && transcode.to != null) {
                        validTranscodes.add(transcode);
                    }
                }
            }
            if (!validTranscodes.isEmpty()) {
                dm.push("data-transformation");
                for (Transcode transcode : validTranscodes) {
                    dm.push("transform");
                    dm.add("from", transcode.from);
                    dm.add("to", transcode.to);
                    dm.pop();
                }
                dm.pop();
            }
        }
        server.execute(sessionKey, "shopping.cart.modify", dm.root());

        /*
         * order the cart
         */
        ShoppingCartOrderModule.order(server, sessionKey, sid);

        /*
         * display back the cart detail
         */
        response.redirectTo(ShoppingCartDescribeModule.urlFor(sid));
        // ShoppingCartDescribeModule.describe(server, sessionKey, sid,
        // OutputFormat.html, request,
        // response);
    }

    @Override
    public String name() {
        return NAME;
    }

    public static String urlFor(String cid, boolean proceed, SessionKey skey,
            String token) {
        return ObjectServlet.urlFor(ObjectServlet.ModuleName.download, cid,
                skey == null ? null : skey.key(), token, new String[] {
                        ObjectServlet.ARG_PROCEED, Boolean.toString(proceed) });
    }

    public static String urlFor(String cid, boolean proceed) {
        return urlFor(cid, proceed, null, null);
    }
}
