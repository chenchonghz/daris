package nig.mf.plugin.analyzer;

import javax.imageio.ImageReader;

public class ImageReaderDetails {
    
    public final ImageReader imageReader;
    public final String logicalType;

    public ImageReaderDetails(ImageReader imageReader, String logicalType) {
        this.imageReader = imageReader;
        this.logicalType = logicalType;
    }
}