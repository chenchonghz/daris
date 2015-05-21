package daris.client.model.object.metadata;

import java.util.ArrayList;
import java.util.List;

import arc.mf.model.asset.document.MetadataDocumentRef;
import daris.client.model.object.DObject;

public class ObjectDicomMetadata {
    private DObject.Type _type;
    private List<MetadataDocumentRef> _docs;

    public ObjectDicomMetadata(DObject.Type type) {
        _type = type;
        _docs = new ArrayList<MetadataDocumentRef>();
        switch (_type) {
        case project:
            _docs.add(new MetadataDocumentRef("mf-dicom-project"));
            break;
        case subject:
            _docs.add(new MetadataDocumentRef("mf-dicom-subject"));
            _docs.add(new MetadataDocumentRef("mf-dicom-patient"));
            break;
        case study:
            _docs.add(new MetadataDocumentRef("mf-dicom-study"));
            break;
        case dataset:
            _docs.add(new MetadataDocumentRef("mf-dicom-series"));
            break;
        default:
            break;
        }
    }

    public DObject.Type type() {
        return _type;
    }

    public List<MetadataDocumentRef> documents() {

        return _docs;
    }
}