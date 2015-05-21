package daris.client.model.nifti.messages;

import daris.client.model.nifti.NiftiHeader;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class NiftiMetadataGet extends ObjectMessage<NiftiHeader> {

    public static final String SERVICE_NAME = "nig.image.metadata.get";

    private String _assetId;

    public NiftiMetadataGet(String assetId) {
        _assetId = assetId;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("id", _assetId);
        w.add("include-parent", true);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected NiftiHeader instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            XmlElement e = xe.element(NiftiHeader.DOC_TYPE);
            if (e != null) {
                return new NiftiHeader(e);
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "NIFTI series";
    }

    @Override
    protected String idToString() {
        return "asset_id=" + _assetId;
    }

}
