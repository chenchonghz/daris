package daris.client.model.query.options;

import java.util.List;

import arc.mf.client.util.ListUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.tree.Tree;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.metadata.tree.ObjectMetadataTree;
import daris.client.model.query.filter.pssd.ProjectAware;

public class ObjectQueryOptions extends QueryOptions implements ProjectAware {

    public static final XPath XPATH_ID = new XPath("id", "cid");
    public static final XPath XPATH_TYPE = new XPath("type", "meta/daris:pssd-object/type");
    public static final XPath XPATH_NAME = new XPath("name", "meta/daris:pssd-object/name");
    public static final XPath XPATH_DESCRIPTION = new XPath("description", "meta/daris:pssd-object/description");
    public static final XPath XPATH_DATASET_TYPE = new XPath("dataset-type", "meta/daris:pssd-dataset/type");
    public static final XPath XPATH_DATASET_PROCESSED = new XPath("dataset-processed", "meta/daris:pssd-derivation/processed");
    public static final XPath XPATH_MIME_TYPE = new XPath("mime-type", "type");

    private DObjectRef _project;

    public ObjectQueryOptions(DObjectRef project) {
        super(Entity.object, Action.get_value);
        _project = project;
    }

    protected ObjectQueryOptions(XmlElement xe) throws Throwable {
        super(xe);
        _project = new DObjectRef(xe.value("project"));
    }

    @Override
    public Tree metadataTree() {
        return new ObjectMetadataTree(_project);
    }

    @Override
    public DObjectRef project() {
        return _project;
    }

    @Override
    public void setProject(DObjectRef project) {
        _project = project;
    }

    @Override
    public List<XPath> defaultXPaths(Purpose purpose) {
        switch (purpose) {
        case QUERY:
            return ListUtil.list(XPATH_ID, XPATH_TYPE, XPATH_NAME, XPATH_DESCRIPTION, XPATH_DATASET_TYPE,
                    XPATH_DATASET_PROCESSED, XPATH_MIME_TYPE);
        case EXPORT:
            return ListUtil.list(XPATH_ID, XPATH_TYPE, XPATH_NAME, XPATH_DESCRIPTION);
        default:
            return null;
        }
    }

    @Override
    public void save(XmlWriter w, Purpose purpose) {
        if (purpose == Purpose.SERIALIZE) {
            w.add("project", _project.id());
        }
        super.save(w, purpose);
    }

}
