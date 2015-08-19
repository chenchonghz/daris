package daris.client.model.dicom;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class LocalAESetRef extends ObjectRef<List<DicomAE>> {

    private LocalAESetRef() {

    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {

        w.add("type", "local");

    }

    @Override
    protected String resolveServiceName() {

        return "om.pssd.dicom.ae.list";
    }

    @Override
    protected List<DicomAE> instantiate(XmlElement xe) throws Throwable {

        if (xe != null && xe.element("ae[@type='local']") != null) {
            List<XmlElement> aes = xe.elements("ae[@type='local']");
            if (aes != null && !aes.isEmpty()) {
                List<DicomAE> ats = new ArrayList<DicomAE>();
                for (XmlElement ae : aes) {
                    ats.add(new DicomAE(ae));
                }
                return ats;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {

        return null;
    }

    @Override
    public String idToString() {

        return null;
    }

}
