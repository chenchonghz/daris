package nig.mf.plugin.analyzer;

import javax.imageio.ImageReader;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcMetadataGet extends PluginService {

    public static final String SERVICE_NAME = "nig.image.metadata.get";

    private Interface _defn;

    /**
     * Constructor.
     */
    public SvcMetadataGet() {

        _defn = new Interface();
        _defn.add(new Element("id", AssetType.DEFAULT, "The asset id.", 0, 1));
        _defn.add(new Element("include-parent", BooleanType.DEFAULT,
                "Include the document type as the root (defaults to true)", 0, 1));
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
        return "Gets image meta-data from images of various formats (primarily nifti).";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        // Requires ACCESS. Because it does not write to an asset in Mediaflux
        // system.
        // Instead, it outputs to an external file.
        return ACCESS_ACCESS;
    }

    @Override
    public int minNumberOfInputs() {
        return 0;
    }

    @Override
    public int maxNumberOfInputs() {
        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs inputs, Outputs out, XmlWriter w) throws Throwable {

        // Register readers
        ImageIOUtil.registerImageReaders();

        // Parse arguments
        String id = args.value("id");
        Boolean includeParent = args.booleanValue("include-parent", true);

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

        /*
         * Get a proper image reader and set the input for the reader
         */
        // The input is either from an asset or a service input
        ImageReaderDetails imageReaderAndLogicalType = id != null ? ImageIOUtil.getImageReader(
                executor(), id) : ImageIOUtil.getImageReader(inputs.input(0));
        ImageReader reader = imageReaderAndLogicalType.imageReader;
        String lctype = imageReaderAndLogicalType.logicalType;
        if (reader == null) {
            throw new Exception("No image reader is found for " + desc.toString());
        }

        /*
         * Read and extract the metadata
         */
        XmlDoc.Element metadata = null;
        try {
            metadata = ImageIOUtil.getMetadata(reader, lctype);
        } finally {
            reader.dispose();
        }
        /*
         * Service response
         */
        if (metadata != null) {
            w.add(metadata, includeParent);
        }

    }
}