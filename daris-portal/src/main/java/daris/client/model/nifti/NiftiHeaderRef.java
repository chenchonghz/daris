package daris.client.model.nifti;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class NiftiHeaderRef extends ObjectRef<NiftiHeader> {

    public static final String SERVICE_NAME = "nig.image.metadata.get";

    private String _assetId;

    public NiftiHeaderRef(String assetId) {
        _assetId = assetId;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("id", _assetId);
        w.add("include-parent", true);
    }

    @Override
    protected String resolveServiceName() {
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
    public String referentTypeName() {
        return "NIFTI header";
    }

    @Override
    public String idToString() {
        return _assetId;
    }

    public String assetId() {
        return _assetId;
    }

}
