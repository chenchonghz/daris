package daris.transcode.services;

import java.io.File;

import org.apache.commons.io.FileUtils;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mime.NamedMimeType;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeRegistry;

public class SvcTranscode extends PluginService {

    private Interface _defn;

    /**
     * Constructor.
     */
    public SvcTranscode() {

        _defn = new Interface();
        _defn.add(new Element("id", AssetType.DEFAULT, "The asset id.", 0, 1));
        _defn.add(new Element("cid", CiteableIdType.DEFAULT,
                "The asset citeable id.", 0, 1));
        _defn.add(new Element("to", StringType.DEFAULT,
                "The mime type to convert/transcode to. "
                        + " e.g. analyze/series/nl", 1, 1));
        _defn.add(new Element(
                "provider",
                StringType.DEFAULT,
                "The specific transcode provider that you want to use if there are more than one available.",
                0, 1));

    }

    /**
     * Returns the service name.
     */
    public String name() {

        return "daris.transcode";

    }

    /**
     * Returns the description about this service.
     */
    public String description() {
        return "A service that transcode/covert image to a different format and output to external file system."
                + " Note: This service is normally for testing purpose. "
                + "To transcode/convert assets in Medialux system, you need to use asset.transcode service (transcode framework).";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        // Requires only ACCESS_ACCESS. Because it does not write to an asset.
        // It writes to a local file.
        return ACCESS_ACCESS;
    }

    public int minNumberOfOutputs() {
        return 1;
    }

    public int maxNumberOfOutputs() {
        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {
        String to = args.value("to");
        String provider = args.value("provider");
        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new Exception("Asset id or citeable id is not specified.");
        }

        if (id != null && cid != null) {
            throw new Exception(
                    "Both asset id and citeable id are specified. Only need one of them.");
        }

        XmlDocMaker doc = new XmlDocMaker("args");
        if (cid != null) {
            doc.add("cid", cid);
        }
        if (id != null) {
            doc.add("id", id);
        }
        Outputs outputs = new Outputs(1);
        XmlDoc.Element r = executor().execute("asset.get", doc.root(), null,
                outputs);
        Output output = outputs.output(0);
        String ctype = output.mimeType();
        if (ctype == null) {
            ctype = r.value("asset/content/type");
        }
        long csize = output.length();
        if (csize <= 0) {
            csize = r.longValue("asset/content/size", -1);
        }
        String cext = r.value("asset/content/type/@ext");
        if (cid == null) {
            cid = r.value("asset/cid");
        }
        if (id == null) {
            id = r.value("asset/@id");
        }
        if (output == null || r.element("asset/content") == null) {
            throw new Exception("No content found in asset(id=" + id + ", cid="
                    + cid + "). Nothing is done.");
        }
        String from = r.value("asset/type");
        if (from == null) {
            throw new Exception("No mime type is set for asset " + id + ".");
        }
        DarisTranscodeRegistry.initialize();
        DarisTranscodeImpl impl = null;
        File inFile = null;
        try {
            if (provider == null) {
                impl = DarisTranscodeRegistry
                        .getActiveTranscoderImpl(from, to);
                if (impl == null) {
                    throw new Exception("No daris transcoder found for " + from
                            + " to " + to + " transcodings.");
                }
            } else {
                impl = DarisTranscodeRegistry
                        .getTranscoder(from, to, provider);
                if (impl == null) {
                    throw new Exception("No daris transcoder found for " + from
                            + " to " + to
                            + " transcodings from transcode provider: "
                            + provider + ".");
                }
            }
            inFile = PluginService.createTemporaryFile(cext);
            StreamCopy.copy(output.stream(), inFile);
            File outFile = PluginService.createTemporaryFile();
            String outType = null;
            try {
                outType = impl.transcode(inFile, new NamedMimeType(from),
                        new NamedMimeType(ctype), new NamedMimeType(to),
                        outFile, null);
            } catch (Throwable e) {
                // Make sure the output temp file is deleted if exception
                // occurs.
                if (outFile.exists()) {
                    try {
                        FileUtils.forceDelete(outFile);
                    } catch (Throwable ex) {
                        FileUtils.forceDeleteOnExit(outFile);
                    }
                }
                throw e;
            }
            out.output(0).setData(
                    new PluginTask.DeleteOnCloseFileInputStream(outFile),
                    outFile.length(), outType);
        } finally {
            if (output != null && output.stream() != null) {
                output.stream().close();
            }
            if (inFile != null && inFile.exists()) {
                try {
                    FileUtils.forceDelete(inFile);
                } catch (Throwable e) {
                    FileUtils.forceDeleteOnExit(inFile);
                }
            }
        }

    }
}