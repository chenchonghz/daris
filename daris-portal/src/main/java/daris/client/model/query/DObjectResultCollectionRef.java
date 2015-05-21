package daris.client.model.query;

import arc.mf.client.xml.XmlElement;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.method.Method;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.query.filter.pssd.ObjectQuery;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public class DObjectResultCollectionRef extends ResultCollectionRef<DObjectRef> {

    protected DObjectResultCollectionRef(ObjectQuery query) {
        super(query);
    }

    @Override
    protected DObjectRef instantiate(XmlElement xe) throws Throwable {
        DObject.Type type = DObject.Type.parse(xe.value("type"));
        String assetId = xe.value("@id");
        int version = xe.intValue("@version", 0);
        String id = xe.value("id");
        String name = xe.value("name");
        String description = xe.value("description");
        String datasetType = xe.value("dataset-type");
        boolean datasetProcessed = xe.booleanValue("dataset-processed", false);
        String mimeType = xe.value("mime-type");
        // TODO: proute?
        String proute = null;
        DObject o = null;
        switch (type) {
        case project:
            o = new Project(id, proute, name, description);
            break;
        case subject:
            o = new Subject(id, proute, name, description);
            break;
        case ex_method:
            o = new ExMethod(id, proute, name, description);
            break;
        case study:
            o = new Study(id, proute, name, description);
            break;
        case dataset:
            if ("derivation".equals(datasetType)) {
                if ("dicom/series".equals(mimeType)) {
                    o = new DicomDataSet(id, proute, name, description);
                } else {
                    o = new DerivedDataSet(id, proute, name, description);
                    ((DerivedDataSet) o).setProcessed(datasetProcessed);
                }
            } else if ("primary".equals(datasetType)) {
                o = new PrimaryDataSet(id, proute, name, description);
            }
            break;
        case data_object:
            o = new DataObject(id, proute, name, description);
            break;
        case method:
            o = new Method(id, proute, name, description);
            break;
        default:
            break;
        }
        if (o == null) {
            throw new Exception("Failed to instantiate pssd object: " + xe);
        }
        o.setAssetId(assetId);
        o.setVersion(version);
        DObjectRef ref = new DObjectRef(o, false, false);
        addXPathValues(ref, xe);
        return ref;
    }
}
