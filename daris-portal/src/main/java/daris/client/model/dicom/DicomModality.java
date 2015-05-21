package daris.client.model.dicom;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class DicomModality {

    public static final String DICTIONARY = "daris:pssd.dicom.modality";

    public final String name;
    public final String description;

    public DicomModality(String name, String description) {
        assert name != null;
        assert !name.trim().equals("");
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof DicomModality) {
            return ((DicomModality) o).name.equals(this.name);
        }
        return false;
    }

    public static void list(final ObjectResolveHandler<List<DicomModality>> rh) {
        Session.execute("dictionary.entries.describe", "<dictionary>" + DICTIONARY
                + "</dictionary>", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (rh == null) {
                    return;
                }
                List<DicomModality> ms = new ArrayList<DicomModality>();
                if (xe != null) {
                    List<XmlElement> ees = xe.elements("entry");
                    if (ees != null && !ees.isEmpty()) {
                        for (XmlElement ee : ees) {
                            String name = ee.value("term");
                            String description = ee.value("definition");
                            ms.add(new DicomModality(name, description));
                        }
                    }
                }
                rh.resolved(ms.isEmpty() ? null : ms);
            }
        });
    }

    public static void exists(String name, final ObjectMessageResponse<Boolean> rh) {
        Session.execute("dictionary.entry.exists", "<dictionary>" + DICTIONARY
                + "</dictionary><term>" + name + "</term>", new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (rh != null) {
                    rh.responded(xe.booleanValue("exists"));
                }
            }
        });
    }
}
