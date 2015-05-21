package daris.client.model.object.metadata;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.object.ObjectRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class ObjectDomainMetadataRef extends ObjectRef<List<MetadataDocumentRef>> {

    private DObject.Type _type;
    private DObjectRef _project;

    public ObjectDomainMetadataRef(DObject.Type type, DObjectRef project) {
        _type = type;
        _project = project;
    }

    public DObject.Type type() {
        return _type;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("type", _type.toString());
        if (_project != null) {
            w.add("project", _project.id());
        }
    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.type.metadata.list";
    }

    @Override
    protected List<MetadataDocumentRef> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<String> types = xe.values("metadata/definition");
            if (types != null && !types.isEmpty()) {
                List<MetadataDocumentRef> mds = new ArrayList<MetadataDocumentRef>();
                for (String type : types) {
                    mds.add(new MetadataDocumentRef(type));
                }
                return mds;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "domain specific metadata documents";
    }

    @Override
    public String idToString() {
        return null;
    }

}
