package daris.client.model.dicom;

import daris.client.model.image.RemoteImage;

public class DicomImage extends RemoteImage {

    private int _frame;

    public DicomImage(String assetId, int index, int frame, String url, boolean lossless) {

        super(assetId, index, url, lossless);
        _frame = frame;
    }

    public DicomImage(String assetId, int index, int frame, boolean lossless) {

        this(assetId, index, frame, null, lossless);
    }

    public int frame() {
        return _frame;
    }

}
