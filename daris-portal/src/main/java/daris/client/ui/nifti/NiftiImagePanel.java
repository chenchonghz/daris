package daris.client.ui.nifti;

import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessage;
import daris.client.model.nifti.NiftiImage;
import daris.client.model.nifti.messages.NiftiImageGet;
import daris.client.ui.image.ImageSeriesPanel;

public class NiftiImagePanel extends ImageSeriesPanel<NiftiImage> {

    public NiftiImagePanel(String assetId){
        this(assetId, 0);
    }
    
    public NiftiImagePanel(String assetId, int index) {
        super(assetId, index);
    }

    @Override
    protected ObjectMessage<NiftiImage> retrieveService() {
        return new NiftiImageGet(assetId(), index(), false);
    }
    
    public void seek(int index, ActionListener al){
        setIndex(index);
        retrieveImage(al);
    }

}
