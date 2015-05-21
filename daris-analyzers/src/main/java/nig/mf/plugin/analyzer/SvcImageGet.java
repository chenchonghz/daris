package nig.mf.plugin.analyzer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.image.ImageRegistry;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcImageGet extends PluginService {

    public static final String SERVICE_NAME = "nig.image.get";

    public static final String MIME_TYPE_LOSSLESS = "image/png";
    public static final String MIME_TYPE_LOSSY = "image/jpeg";

    private Interface _defn;

    /**
     * Constructor.
     */
    public SvcImageGet() {

        _defn = new Interface();
        _defn.add(new Element("id", AssetType.DEFAULT, "The asset id.", 0, 1));
        _defn.add(new Element("idx", IntegerType.DEFAULT, "Image slice index (defaults to 0).", 0, 1));
        _defn.add(new Element("lossless", BooleanType.DEFAULT,
                "Lossless image encoding (PNG) or lossy (JPEG). Defaults to true.", 0, 1));
    }

    /**
     * Returns the service name.
     */
    public String name() {

        return SERVICE_NAME;
    }

    /**
     * Returns the description about this service.
     */
    public String description() {
        return "Gets image as png.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public int minNumberOfInputs() {
        return 0;
    }

    public int maxNumberOfInputs() {
        return 1;
    }

    public int minNumberOfOutputs() {
        return 1;
    }

    public int maxNumberOfOutputs() {
        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs inputs, Outputs out, XmlWriter w) throws Throwable {

        // Register readers
        ImageIOUtil.registerImageReaders();

        // Parse arguments
        String id = args.value("id");
        int idx = args.intValue("idx", 0);
        boolean lossless = args.booleanValue("lossless", true);
        String outputMimeType = lossless ? MIME_TYPE_LOSSLESS : MIME_TYPE_LOSSY;

        if (id == null && (inputs == null || inputs.size() <= 0)) {
            throw new IllegalArgumentException("Either asset id or a service input is required. Found none.");
        }

        if (id != null && inputs != null && inputs.size() > 0) {
            throw new IllegalArgumentException("Either asset id or a service input is required. Found both.");
        }

        StringBuilder desc = new StringBuilder();
        if (id != null) {
            desc.append("asset ");
            desc.append(id);
            desc.append(".");
        } else {
            desc.append("the service input.");
        }

        // TODO: Scrap this and use asset.icon.get as the standard SUN plugins
        // don't at all handle our kinds of BufferedImage

        /*
         * Get a proper image reader and set the input for the reader
         */
        // The input is either from an asset or a service input
        ImageReaderDetails imageReaderAndLogicalType = id != null ? ImageIOUtil.getImageReader(
                executor(), id) : ImageIOUtil.getImageReader(inputs.input(0));
        ImageReader reader = imageReaderAndLogicalType.imageReader;
        if (reader == null) {
            throw new Exception("No image reader is found for " + desc.toString());
        }

        /*
         * Read the specified slice of the image.
         */
        BufferedImage img1 = null;
        try {
            img1 = reader.read(idx);
        } finally {
            reader.dispose();
        }
        if (img1 == null) {
            throw new IOException("Failed to read image slice " + idx + " from " + desc.toString());
        }
        // Convert the buffered image so that it can be saved to png/jpeg.
        BufferedImage img2 = new BufferedImage(img1.getWidth(), img1.getHeight(),
                lossless ? BufferedImage.TYPE_USHORT_GRAY : BufferedImage.TYPE_BYTE_GRAY);
        img2.getGraphics().drawImage(img1, 0, 0, null);

        /*
         * Write the image slice to a output file (a temp file).
         */
        ImageWriter writer = ImageRegistry.writerForMIMEType(outputMimeType);
        if (writer == null) {
            throw new Exception("No image reader is found for mime type: " + outputMimeType);
        }
        File tf = PluginTask.createTemporaryFile();
        ImageOutputStream ios = new FileImageOutputStream(tf);
        try {
            writer.setOutput(ios);
            writer.write(img2);
        } finally {
            ios.close();
            writer.dispose();
        }

        /*
         * Set the service output.
         */
        out.output(0).setData(PluginTask.deleteOnCloseInputStream(tf), tf.length(), outputMimeType);
    }
}