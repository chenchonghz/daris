package daris.client.model.nifti.messages;

import arc.mf.client.xml.XmlElement;
import daris.client.model.image.messages.ImageGet;
import daris.client.model.nifti.NiftiImage;
import daris.client.util.ClassUtil;

public class NiftiImageGet extends ImageGet<NiftiImage> {

    public NiftiImageGet(String assetId, int index, boolean lossless) {
        super(assetId, index, lossless);
    }

    @Override
    protected NiftiImage instantiate(XmlElement xe) throws Throwable {
        if(xe!=null){
            return new NiftiImage(assetId(), index(), null, lossless());
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return ClassUtil.simpleClassNameOf(NiftiImage.class);
    }

}
