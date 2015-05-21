package daris.client.model.nifti;

import daris.client.model.image.RemoteImage;

public class NiftiImage extends RemoteImage{

    public NiftiImage(String assetId, int index, String url, boolean lossless) {
        super(assetId, index, url, lossless);
    }
    
    public NiftiImage(String assetId, int index, boolean lossless){
        this(assetId, index, null, lossless);
    }

}
