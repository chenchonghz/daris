package daris.client.model.repository;

import arc.mf.client.xml.XmlWriterNe;
import arc.xml.XmlDoc.Element;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class RepositoryRef extends DObjectRef {

    private static RepositoryRef _instance;

    public static RepositoryRef get() {
        if (_instance == null) {
            _instance = new RepositoryRef();
        }
        return _instance;
    }

    private RepositoryRef() {
        super(null, -1);
    }

    @Override
    public String idToString() {
        return null;
    }

    @Override
    protected DObject instantiate(Element xe) throws Throwable {
        DObject o = DObject.create(xe.element("repository"));
        if (o != null) {
            setName(((Repository) o).name());
            setDescription(o.description());
            if (o.numberOfChildren() > -1) {
                setNumberOfChildren(o.numberOfChildren());
            }
        }
        return o;
    }

    @Override
    public String referentTypeName() {
        return DObject.Type.REPOSITORY.typeName();
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w) {
    }

    @Override
    protected String resolveServiceName() {
        return "daris.repository.describe";
    }
}
