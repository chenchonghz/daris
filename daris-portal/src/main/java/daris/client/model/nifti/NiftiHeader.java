package daris.client.model.nifti;

import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlElement;

public class NiftiHeader {

    public static final String DOC_TYPE = "daris:nifti-1";

    private XmlElement _de;

    public NiftiHeader(XmlElement de) {
        _de = de;
    }

    public int dimZ() {
        try {
            int dim3 = _de.intValue("dim_3", 0);
            return dim3;
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
            return 0;
        }
    }

    public XmlElement xml() {
        return _de;
    }

}
