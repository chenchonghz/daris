package nig.mf.plugin.pssd.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class SvcArchiveContentImageGet extends PluginService {

    public static final String SERVICE_NAME = "daris.archive.content.image.get";

    public static final String SERVICE_DESCRIPTION = "retrieve a file entry from the specified image series asset, which must have zip/aar archive as content and a mf-dicom-series docment in the metadata.";

    private static final String[] _supportedTypes = { "image/series" };

    private static final String[] _supportedImageFormats = { "bmp", "tif",
            "tiff", "gif", "png", "jpg", "jpeg" };

    private static boolean isAssetMimeTypeSupported(String type) {
        for (int i = 0; i < _supportedTypes.length; i++) {
            if (_supportedTypes[i].equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isImageFormatSupported(String fileExt) {
        for (int i = 0; i < _supportedImageFormats.length; i++) {
            if (_supportedImageFormats[i].equalsIgnoreCase(fileExt)) {
                return true;
            }
        }
        return false;
    }

    private Interface _defn;

    public SvcArchiveContentImageGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The id of the DICOM series asset", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the DICOM series asset", 0, 1));
        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE,
                "The ordinal position. Defaults to 1.", 0, 1));
        _defn.add(new Interface.Element("auto-convert", BooleanType.DEFAULT,
                "Convert the image to .png format, if it is not supported by browsers. Defaults to false.",
                0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return SERVICE_DESCRIPTION;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        /*
         * parse & validate arguments
         */
        String id = args.value("id");
        String cid = args.value("cid");
        long idx = args.longValue("idx", 1);
        boolean autoConvert = args.booleanValue("auto-convert", false);
        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }
        if (outputs == null) {
            throw new Exception("Expect 1 out. Found none.");
        }
        if (outputs.size() != 1) {
            throw new Exception("Expect 1 out. Found " + outputs.size() + ".");
        }

        /*
         * get asset metadata & content
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        Outputs sos = new Outputs(1);
        XmlDoc.Element ae = executor()
                .execute("asset.get", dm.root(), null, sos).element("asset");
        String type = ae.value("type");
        if (!isAssetMimeTypeSupported(type)) {
            throw new Exception("Unsupported asset mime type: " + type);
        }
        ImageArchiveEntry entry = getImageArchiveEntry(executor(), id, cid, idx,
                ae, sos.output(0));
        String fileName = entry.fileName;
        if (fileName == null) {
            fileName = String.format("%05d.%s", entry.idx, entry.extension);
        }
        if (!autoConvert || entry.isSupportedByBrowsers()) {
            outputs.output(0).setData(
                    new SizedInputStream(entry.stream, entry.length),
                    entry.length, entry.mimeType());
            w.add("entry", new String[] { "idx", String.valueOf(idx), "size",
                    String.valueOf(entry.length) }, fileName);
        } else {
            String originalFormat = entry.extension;
            File pngFile = PluginTask.createTemporaryFile(".png");
            scanForImageIOPlugins();
            BufferedImage bufferedImage = ImageIO.read(entry.stream);
            ImageIO.write(bufferedImage, "png", pngFile);
            fileName = fileName + ".png";
            outputs.output(0)
                    .setData(new SizedInputStream(
                            PluginTask.deleteOnCloseInputStream(pngFile),
                            pngFile.length()), pngFile.length(), "image/png");
            w.add("entry",
                    new String[] { "idx", String.valueOf(idx), "size",
                            String.valueOf(pngFile.length()), "original-format",
                            originalFormat },
                    fileName);
        }
    }

    private static boolean _scannedImageIOPlugins = false;

    private static void scanForImageIOPlugins() {
        if (!_scannedImageIOPlugins) {
            ImageIO.scanForPlugins();
            _scannedImageIOPlugins = true;
        }
    }

    private static class ImageArchiveEntry {
        public final long idx;
        public final String fileName;
        public final String extension;
        public final long length;
        public final InputStream stream;

        public ImageArchiveEntry(long idx, String fileName, long length,
                InputStream stream) {
            this.idx = idx;
            this.fileName = fileName;
            int dotIdx = this.fileName.lastIndexOf('.');
            this.extension = dotIdx == -1 ? null
                    : this.fileName.substring(dotIdx + 1);
            this.length = length;
            this.stream = stream;
        }

        boolean isSupportedByBrowsers() {
            return "jpg".equalsIgnoreCase(extension)
                    || "jpeg".equalsIgnoreCase(extension)
                    || "png".equalsIgnoreCase(extension)
                    || "gif".equalsIgnoreCase(extension);
        }

        String mimeType() {
            if ("bmp".equalsIgnoreCase(extension)) {
                return "image/bmp";
            } else if ("tif".equalsIgnoreCase(extension)
                    || "tiff".equalsIgnoreCase(extension)) {
                return "image/tiff";
            } else if ("png".equalsIgnoreCase(extension)) {
                return "image/png";
            } else if ("gif".equalsIgnoreCase(extension)) {
                return "image/gif";
            } else if ("jpg".equalsIgnoreCase(extension)
                    || "jpeg".equalsIgnoreCase(extension)) {
                return "image/jpeg";
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported format: " + extension);
            }
        }

    }

    private static ImageArchiveEntry getImageArchiveEntry(
            ServiceExecutor executor, String id, String cid, long idx,
            XmlDoc.Element ae, Output so) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("result");
        XmlDocWriter w = new XmlDocWriter(dm);
        Outputs outputs = new Outputs(1);
        SvcArchiveContentGet.getArchiveEntry(executor, id, cid, idx, ae, so,
                outputs, w);
        String name = dm.root().value("entry");
        long length = dm.root().longValue("entry/@size");
        int dotIdx = name.lastIndexOf('.');
        String ext = dotIdx == -1 ? null : name.substring(dotIdx + 1);
        if (!isImageFormatSupported(ext)) {
            throw new Exception("Unsupported image type: " + ext + " (file: "
                    + name + ").");
        }
        ext = ext.toLowerCase();
        return new ImageArchiveEntry(idx, name, length,
                outputs.output(0).stream());
    }

}
