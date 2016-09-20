package daris.essentials;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetContentStringReplace extends PluginService {

    public static final String SERVICE_NAME = "nig.asset.content.string.replace";

    public static final int MAX_FILE_SIZE = 65536; // maximum file size
                                                   // supported.

    private Interface _defn;

    public SvcAssetContentStringReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The path to the asset. If not specified, then a citeable identifier or an alterate identifier must be specified.",
                0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "he citeable identifier of the asset. If not specified, then a non-citeable 'id' or an alterate identifier must be specified.",
                0, 1));
        _defn.add(new Interface.Element("encoding", StringType.DEFAULT,
                "The character encoding of the text content. Defaults to \"UTF-8\". See also java.nio.charset.StandardCharsets.",
                0, 1));
        _defn.add(new Interface.Element("pattern", StringType.DEFAULT,
                "The regular expression to which this content string is to be matched.",
                1, 1));
        _defn.add(new Interface.Element("replacement", StringType.DEFAULT,
                "The string to be substituted for each match.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Replaces each substring of the string content of the specified asset that matches the given regular expression (pattern) with the given replacement.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new IllegalArgumentException(
                    "Either id or cid must be specified.");
        }
        String encoding = args.stringValue("encoding", "UTF-8");
        String pattern = args.value("pattern");
        String replacement = args.value("replacement");

        /*
         * get asset meta
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        XmlDoc.Element ae = executor().execute("asset.get", dm.root())
                .element("asset");
        if (id == null) {
            id = ae.value("@id");
        }

        // check if the asset has content
        if (!ae.elementExists("content")) {
            throw new Exception("asset " + (id == null ? cid : id)
                    + " does not have content.");
        }

        String ctype = ae.value("content/type");

        // check if the content size is bigger than 65536
        long csize = ae.longValue("content/size");
        if (csize == 0) {
            return;
        }
        if (csize > MAX_FILE_SIZE) {
            throw new Exception("asset " + (id == null ? cid : id)
                    + " content size is greater than 65536 bytes.");
        }

        // get asset content
        String contentString = getContentString(executor(), id, encoding);

        // replace and save
        if (contentString != null) {
            contentString = contentString.replaceAll(pattern, replacement);
            // save
            setContentString(executor(), id, contentString, encoding, ctype);
        }
    }

    static String getContentString(ServiceExecutor executor, String assetId,
            String encoding) throws Throwable {
        PluginService.Outputs out = new PluginService.Outputs(1);
        executor.execute("asset.content.get",
                "<args><id>" + assetId + "</id></args>", null, out);
        InputStream is = out.output(0).stream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toString("UTF-8");
        } finally {
            baos.close();
            is.close();
        }
    }

    static void setContentString(ServiceExecutor executor, String assetId,
            String contentString, String encoding, String ctype)
                    throws Throwable {
        byte[] b = contentString.getBytes(encoding);
        InputStream is = new ByteArrayInputStream(b);
        PluginService.Input input = new PluginService.Input(is, b.length, ctype,
                null);
        executor.execute("asset.set", "<args><id>" + assetId + "</id></args>",
                new PluginService.Inputs(input), null);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
