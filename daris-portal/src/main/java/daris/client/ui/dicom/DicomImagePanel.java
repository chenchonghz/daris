package daris.client.ui.dicom;

import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomImage;
import daris.client.model.dicom.messages.DicomImageGet;
import daris.client.ui.image.ImageSeriesPanel;

public class DicomImagePanel extends ImageSeriesPanel<DicomImage> {

    private int _frame;

    public DicomImagePanel(String assetId) {
        this(assetId, 0);
    }

    public DicomImagePanel(String assetId, int index) {
        super(assetId, index);
        _frame = 0;
    }

    public int frame() {
        return _frame;
    }

    protected void setFrame(int frame) {
        _frame = frame;
    }

    public void seek(int index, int frame, ActionListener al) {
        setIndex(index);
        setFrame(frame);
        retrieveImage(al);
    }

    @Override
    protected ObjectMessage<DicomImage> retrieveService() {
        return new DicomImageGet(assetId(), index(), frame());
    }
}
