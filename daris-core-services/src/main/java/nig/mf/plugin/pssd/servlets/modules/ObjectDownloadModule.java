package nig.mf.plugin.pssd.servlets.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.Transcode;
import nig.mf.plugin.pssd.servlets.AbstractServlet;
import nig.mf.plugin.pssd.servlets.ObjectServlet;
import nig.mf.plugin.pssd.servlets.TranscodeCodec;

public class ObjectDownloadModule implements Module {

    public static final ObjectDownloadModule INSTANCE = new ObjectDownloadModule();

    public static final String NAME = ObjectServlet.ModuleName.download.name();

    public static final long GB1 = 1073741824L;

    public static final long GB2 = 2147483648L;

    public static enum ArchiveType {
        aar, zip, tgz;

        public String mimeType() {
            switch (this) {
            case aar:
                return "application/arc-archive";
            case zip:
                return "application/zip";
            case tgz:
                return "application/x-gzip";
            default:
                return null;
            }
        }

        public static ArchiveType parse(String s, ArchiveType def) {
            if (s != null) {
                ArchiveType[] vs = values();
                for (ArchiveType v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return def;
        }

        public static ArchiveType parse(HttpRequest r, ArchiveType def) {
            return parse(r.variableValue(ObjectServlet.ARG_ATYPE), def);
        }
    }

    public static enum AssetParts {
        meta, content, all;
        public static AssetParts parse(String s, AssetParts def) {
            if (s != null) {
                AssetParts[] vs = values();
                for (AssetParts v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return def;
        }

        public static AssetParts parse(HttpRequest r, AssetParts def) {
            return parse(r.variableValue(ObjectServlet.ARG_PARTS), def);
        }
    }

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

        public String token;
        public String cid;
        public AssetParts parts;
        public ArchiveType archiveType;
        public boolean decompress;
        public boolean includeAttachments;

        public String filename;
        public List<Transcode> transcodes;

        public boolean destroySession() {
            return token != null;
        }

        public boolean transcodeExists() {
            return transcodes != null && !transcodes.isEmpty();
        }

        static DownloadArguments parse(HttpRequest request,
                String originalFileName) throws Throwable {

            DownloadArguments args = new DownloadArguments();
            args.token = request.variableValue(ObjectServlet.ARG_TOKEN);
            args.cid = request.variableValue(ObjectServlet.ARG_CID);
            args.parts = AssetParts.parse(request, AssetParts.all);
            args.archiveType = ArchiveType.parse(request, ArchiveType.aar);
            args.decompress = Boolean.parseBoolean(request
                    .variableValue(ObjectServlet.ARG_DECOMPRESS, "true"));
            args.includeAttachments = Boolean
                    .parseBoolean(request.variableValue(
                            ObjectServlet.ARG_INCLUDE_ATTACHEMENTS, "true"));
            args.filename = request.variableValue(ObjectServlet.ARG_FILENAME);
            args.transcodes = TranscodeCodec.decodeTranscodes(request);

            if (args.filename == null) {
                if (originalFileName != null) {
                    args.filename = originalFileName;
                } else if (args.cid != null) {
                    String type = CIDUtil.getType(args.cid);
                    args.filename = type + "_" + args.cid;
                }
            }

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
        download(server, sessionKey, args, request, response);
    }

    private static void download(HttpServer server, SessionKey sessionKey,
            DownloadArguments args, HttpRequest request, HttpResponse response)
                    throws Throwable {

        XmlDocMaker w = new XmlDocMaker("args");
        w.add("cid", args.cid);
        w.add("parts", args.parts);
        w.add("format", args.archiveType);
        w.add("include-attachments", args.includeAttachments);
        w.add("decompress", args.decompress);
        if (args.transcodeExists()) {
            for (Transcode transcode : args.transcodes) {
                w.push("transcode");
                w.add("from", transcode.from);
                w.add("to", transcode.to);
                w.pop();
            }
        }
        server.execute(sessionKey, "daris.collection.archive.create", w.root(),
                (HttpRequest) null, response);
        if (response.contentType() == null) {
            response.setHeaderField("Content-Type",
                    args.archiveType.mimeType());
        }
        String filename = args.filename;
        String ext = extFromType(server, sessionKey, response.contentType());
        if (ext != null && !filename.endsWith(ext)) {
            filename = filename + "." + ext;
        }
        response.setHeaderField("Content-Disposition",
                "attachment; filename=\"" + filename + "\"");

        if (args.destroySession()) {
            server.destroySession(sessionKey);
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
