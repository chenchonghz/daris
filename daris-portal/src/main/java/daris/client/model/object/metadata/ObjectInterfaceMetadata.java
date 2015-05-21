package daris.client.model.object.metadata;

import java.util.ArrayList;
import java.util.List;

import arc.mf.model.asset.document.MetadataDocumentRef;
import daris.client.model.object.DObject;

public class ObjectInterfaceMetadata {

    private DObject.Type _type;
    private List<MetadataDocumentRef> _docs;

    public ObjectInterfaceMetadata(DObject.Type type) {
        _type = type;
        _docs = new ArrayList<MetadataDocumentRef>();
        _docs.add(new MetadataDocumentRef("daris:pssd-object"));
        switch (_type) {
        case project:
            _docs.add(new MetadataDocumentRef("daris:pssd-project"));
            break;
        case subject:
            _docs.add(new MetadataDocumentRef("daris:pssd-subject"));
            break;
        case ex_method:
            _docs.add(new MetadataDocumentRef("daris:pssd-method"));
            _docs.add(new MetadataDocumentRef("daris:pssd-ex-method"));
            break;
        case study:
            _docs.add(new MetadataDocumentRef("daris:pssd-study"));
            break;
        case dataset:
            _docs.add(new MetadataDocumentRef("daris:pssd-dataset"));
            _docs.add(new MetadataDocumentRef("daris:pssd-acquisition"));
            _docs.add(new MetadataDocumentRef("daris:pssd-derivation"));
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
