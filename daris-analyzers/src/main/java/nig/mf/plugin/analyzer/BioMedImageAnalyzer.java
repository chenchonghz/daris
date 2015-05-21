package nig.mf.plugin.analyzer;

import java.awt.image.BufferedImage;

import javax.imageio.ImageReader;

import arc.mf.plugin.content.PluginContentAnalyzer;
import arc.xml.XmlDoc;

public class BioMedImageAnalyzer implements PluginContentAnalyzer {

    /*
     * The string array below contains the asset content logical mime types
     * supported by this analyzer.
     */
    public static final String[] MIME_TYPES = { "nifti/series", "minc/series", "image/nii", "image/mnc" };

    @Override
    public String[] mimeTypes() {

        return MIME_TYPES;
    }

    @Override
    public int contentSourceType() {

        return PluginContentAnalyzer.SOURCE_TYPE_STREAM;
    }

    /**
     * Will the analyzer extract metadata from the content?
     * 
     * @return true if metadata extracted, and false if not.
     */
    @Override
    public boolean willExtractMetadata() {
        return true;
    }

    /**
     * Will the analyzer extract text from the content?
     * 
     * @return true if it will extract text and false if not.
     */
    @Override
    public boolean willExtractText() {
        return false;
    }

    /**
     * Will the analyzer extract thumbnails from the content?
     * 
     * @return true if it will extract text and false if not.
     */
    @Override
    public boolean willExtractIcon() {
        return true;
    }

    /**
     * Analyze the input. This analyzer can locate meta-data on an asset because
     * the mime-types image/{nii.mnc,dcm} mean that the asset content is just a
     * single file. This analyzer does not handle encapsulated containers.
     * 
     * @param in
     *            The input - either a StreamInput or a UrlInput. We asked for
     *            the data as a stream. A UrlInput is a reference to some
     *            external content.
     * @param mimeType
     *            The MIME type of the input. This is the logical mime type
     *            (asset.get :content < :ltype >) of the content (asset.create
     *            :lctype)
     * @param extractMeta
     *            If true, the analyzer should extract metadata, if any. This
     *            metadata will be attached to the asset.
     * @param extractDetailedMeta
     *            If true, the analyzer should extract detailed metadata, if
     *            any. This metadata will not be attached to the asset, but will
     *            be returned by the service asset.content.meta.get
     * @param extractText
     *            If true, the analyzer should extract text, if any, from the
     *            content.
     * @param extractIcon
     *            If true, the analyzer should extract or generate a thumbnail
     *            image (say no larger than 200x200 pixels).
     * 
     */
    @Override
    public Analysis analyze(Input in, String mimeType, boolean extractMeta, boolean extractDetailedMeta,
            boolean extractText, boolean extractIcon) throws Throwable {

        /*
         * Get a proper reader for the logical mime type, and set the input for
         * the reader.
         */
        ImageReaderDetails readerDetails = ImageIOUtil.getImageReader((StreamInput) in, mimeType);
        ImageReader reader = readerDetails.imageReader;
        // TODO: test
        // mimeType = readerDetails.logicalType;
        // assert mimeType.equals(readerDetails.logicalType);
        if (reader == null) {
            // Could not find a proper reader.
            return null;
        }

        /*
         * Read the image using the reader.
         */
        XmlDoc.Element metaInfo = null;
        XmlDoc.Element detailedMetaInfo = null;
        BufferedImage bufferedImage = null;
        if (extractMeta || extractDetailedMeta) {
            // Get the meta data from the image (and the analyzer will put it
            // onto the asset)
            XmlDoc.Element metadata = ImageIOUtil.getMetadata(reader, mimeType);
            // The summary and detailed are the same for this analyzer
            if (extractMeta) {
                metaInfo = metadata;
            }
            if (extractDetailedMeta) {
                detailedMetaInfo = metadata;
            }
        }
        if (extractIcon) {
            // Read the first image slice (as the asset icon) as there is no
            // control over slice index at this time.
            bufferedImage = reader.read(0);
        }

        // There are other aspects we can set on the analysis, such as geoshape
        // (for content that has spatial attributes), etc.
        return new Analysis(metaInfo, detailedMetaInfo, null, bufferedImage);
    }

}
