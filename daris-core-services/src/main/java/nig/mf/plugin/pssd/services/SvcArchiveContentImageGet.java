package nig.mf.plugin.pssd.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.util.PathUtils;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

public class SvcArchiveContentImageGet extends PluginService {

    public static final String SERVICE_NAME = "daris.archive.content.image.get";

    public static final String[] IMAGE_FORMATS = { "bmp", "tif", "tiff", "gif", "png", "jpg", "jpeg", "dcm" };

    private Interface _defn;

    public SvcArchiveContentImageGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The id of the image series asset", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the image series asset", 0,
                1));
        Interface.Element idx = new Interface.Element("idx", LongType.POSITIVE_ONE,
                "The ordinal position. Defaults to one.", 0, 1);
        idx.add(new Interface.Attribute("frame", LongType.POSITIVE,
                "This specifies the frame ordinal of a DICOM image. Can only be greater than one for multi-frame data - that is, (0028,0008) set and greater than 1. Defaults to one.",
                0));
        _defn.add(idx);
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The entry file name. If specified, it will not try to retrieve the entry name from the archive, therefore, it may significantly improve the performance for sequential archives.",
                0, 1));
        _defn.add(new Interface.Element("lossless", BooleanType.DEFAULT,
                "If set to false, will generate JPEG image with lossy compression. By default, it generates a lossless encoded PNG image.",
                0, 1));
        _defn.add(new Interface.Element("size", IntegerType.POSITIVE,
                "If specified, set the size (in pixels) of the largest dimension (width or height). The image will only be resized if it is larger than this size.",
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
        return "retrieve a file entry from the specified image series asset, which must have zip/aar archive as content and a mf-dicom-series docment in the metadata.";
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
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        /*
         * parse & validate arguments
         */
        long idx = args.longValue("idx", 1);
        long frame = args.longValue("idx/frame", 1);
        String entryName = args.value("name");
        boolean lossless = args.booleanValue("lossless", true);
        Integer size = args.intValue("size", null);

        String id = args.value("id");
        String cid = args.value("cid");

        if (id == null && cid == null) {
            throw new Exception("id or cid is expected. Found none.");
        }
        if (id != null && cid != null) {
            throw new Exception("id or cid is expected. Found both.");
        }

        XmlDoc.Element ae = getAssetMeta(executor(), id, cid);
        if (id == null) {
            id = ae.value("@id");
        }
        if (cid == null) {
            cid = ae.value("cid");
        }

        if (entryName == null) {
            // retrieve the entry name from the archive by calling
            // asset.archive.content.list
            SimpleEntry<String, Long> entryInfo = getArchiveEntryInfo(executor(), id, idx);
            entryName = entryInfo.getKey();
            // Long entrySize = entryInfo.getValue();
        }

        if (!isImageFile(entryName)) {
            throw new Exception("Entry (idx=" + idx + ") " + entryName + " is not a supported image.");
        }

        if (entryName.toLowerCase().endsWith(".dcm")) {
            getDicomImageEntry(executor(), id, idx, frame, lossless, null, outputs.output(0));
        } else if (entryName.toLowerCase().endsWith(".png")) {
            if (lossless && size == null) {
                getImageEntry(executor(), id, idx, entryName, outputs.output(0));
            } else {
                getPNGImageEntry(executor(), id, idx, entryName, size, outputs.output(0));
            }
        } else if (entryName.toLowerCase().endsWith(".jpg") || entryName.toLowerCase().endsWith(".jpeg")) {
            if (!lossless && size == null) {
                getImageEntry(executor(), id, idx, entryName, outputs.output(0));
            } else {
                getJPGImageEntry(executor(), id, idx, entryName, size, outputs.output(0));
            }
        } else {
            if (lossless) {
                getPNGImageEntry(executor(), id, idx, entryName, size, outputs.output(0));
            } else {
                getJPGImageEntry(executor(), id, idx, entryName, size, outputs.output(0));
            }
        }
    }

    private static XmlDoc.Element getAssetMeta(ServiceExecutor executor, String id, String cid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        XmlDoc.Element ae = executor.execute("asset.get", dm.root()).element("asset");
        return ae;
    }

    private static String getMimeType(String name) {
        if (name != null) {
            String lcn = name.toLowerCase();
            if (lcn.endsWith(".dcm")) {
                return "application/dicom";
            }
            if (lcn.endsWith(".png")) {
                return "image/png";
            }
            if (lcn.endsWith(".gif")) {
                return "image/gif";
            }
            if (lcn.endsWith(".bmp")) {
                return "image/bmp";
            }
            if (lcn.endsWith(".jpg") || lcn.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            if (lcn.endsWith(".tif") || lcn.endsWith(".tiff")) {
                return "image/tiff";
            }
        }
        return null;
    }

    public static boolean isImageFile(String name) {
        int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            String ext = name.substring(idx + 1);
            return isImageFileExtension(ext);
        }
        return false;
    }

    public static boolean isImageFileExtension(String ext) {
        for (String format : IMAGE_FORMATS) {
            if (format.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    public static void getPNGImageEntry(ServiceExecutor executor, String assetId, long idx, String name, Integer size,
            PluginService.Output out) throws Throwable {
        String ext = PathUtils.getFileExtension(name);
        File f1 = PluginTask.createTemporaryFile("." + ext);
        try {
            getArchiveEntry(executor, assetId, idx, f1);
            File f2 = PluginTask.createTemporaryFile(".png");
            convertToPNG(f1, f2, size);
            out.setData(PluginTask.deleteOnCloseInputStream(f2), f2.length(), "image/png");
        } finally {
            PluginTask.deleteTemporaryFile(f1);
        }
    }

    public static void getJPGImageEntry(ServiceExecutor executor, String assetId, long idx, String name, Integer size,
            PluginService.Output out) throws Throwable {
        String ext = PathUtils.getFileExtension(name);
        File f1 = PluginTask.createTemporaryFile("." + ext);
        try {
            getArchiveEntry(executor, assetId, idx, f1);
            File f2 = PluginTask.createTemporaryFile(".jpg");
            convertToJPG(f1, f2, size);
            out.setData(PluginTask.deleteOnCloseInputStream(f2), f2.length(), "image/jpeg");
        } finally {
            PluginTask.deleteTemporaryFile(f1);
        }
    }

    public static void getDicomImageEntry(ServiceExecutor executor, String assetId, long idx, long frame,
            boolean lossless, Float size, PluginService.Output output) throws Throwable {
        // NOTE: idx and frame start from 1. However, idx and frame attribute in
        // dicom.image.get services starts from 0;
        XmlDocMaker dm = new XmlDocMaker("args");
        if (frame > 1) {
            dm.add("id", new String[] { "idx", String.valueOf(idx - 1), "frame", String.valueOf(frame - 1) }, assetId);
        } else {
            dm.add("id", new String[] { "idx", String.valueOf(idx - 1) }, assetId);
        }
        dm.add("lossless", lossless);
        // TODO dicom.image.get :size argument does not take effect.
        if (size != null) {
            // dm.add("size", size);
        }
        Outputs outputs = new Outputs(1);
        executor.execute("dicom.image.get", dm.root(), null, outputs);
        String mimeType = outputs.output(0).mimeType();
        if (mimeType == null) {
            mimeType = lossless ? "image/png" : "image/jpeg";
        }
        output.setData(outputs.output(0).stream(), outputs.output(0).length(), mimeType);
    }

    public static void getImageEntry(ServiceExecutor executor, String assetId, long idx, String entryName,
            PluginService.Output out) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("idx", idx);
        Outputs outputs = new Outputs(1);
        executor.execute("asset.archive.content.get", dm.root(), null, outputs);
        String mimeType = outputs.output(0).mimeType();
        if (mimeType == null) {
            mimeType = getMimeType(entryName);
        }
        out.setData(outputs.output(0).stream(), outputs.output(0).length(), mimeType);
    }

    public static void getArchiveEntry(ServiceExecutor executor, String assetId, long idx, OutputStream out)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("idx", idx);
        Outputs outputs = new Outputs(1);
        executor.execute("asset.archive.content.get", dm.root(), null, outputs);
        Output output = outputs.output(0);
        BufferedInputStream bis = new BufferedInputStream(output.stream());
        BufferedOutputStream bos = new BufferedOutputStream(out);
        try {
            StreamCopy.copy(bis, bos);
        } finally {
            bos.close();
            bis.close();
            output.close();
        }
    }

    public static void getArchiveEntry(ServiceExecutor executor, String assetId, long idx, File outputFile)
            throws Throwable {
        OutputStream os = new FileOutputStream(outputFile);
        try {
            getArchiveEntry(executor, assetId, idx, os);
        } finally {
            os.close();
        }
    }

    public static SimpleEntry<String, Long> getArchiveEntryInfo(ServiceExecutor executor, String assetId, long idx)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", assetId);
        dm.add("idx", idx);
        dm.add("size", 1);
        XmlDoc.Element ee = executor.execute("asset.archive.content.list", dm.root()).element("entry");
        if (ee == null) {
            throw new Exception("Entry " + idx + " is not found in archive asset " + assetId);
        }
        String name = ee.value();
        Long size = ee.longValue("@size", null);
        return new SimpleEntry<String, Long>(name, size);
    }

    public static void convertToPNG(File in, File out, Integer size) throws Throwable {
        convert(in, out, size, "PNG");
    }

    public static void convertToJPG(File in, File out, Integer size) throws Throwable {
        convert(in, out, size, "JPG");
    }

    public static void convert(File in, File out, Integer size, String toFormat) throws Throwable {
        ImagePlus img = IJ.openImage(in.getAbsolutePath());
        try {
            int w = img.getWidth();
            int h = img.getHeight();
            double ratio = ((double) w) / ((double) h);
            ImageProcessor ip = img.getProcessor();
            if (size != null && size > 0) {
                if (w >= h) {
                    if (w > size) {
                        img.setProcessor(ip.resize(size, (int) (size / ratio), true));
                    }
                } else {
                    if (h > size) {
                        img.setProcessor(ip.resize((int) (ratio * size), size, true));
                    }
                }
            }
            FileSaver fs = new FileSaver(img);
            if ("PNG".equalsIgnoreCase(toFormat)) {
                fs.saveAsPng(out.getAbsolutePath());
            } else if ("JPG".equalsIgnoreCase(toFormat) || "JPEG".equalsIgnoreCase(toFormat)) {
                fs.saveAsJpeg(out.getAbsolutePath());
            } else {
                throw new Exception("Unsupported format: " + toFormat);
            }
        } finally {
            img.close();
        }
    }

}
