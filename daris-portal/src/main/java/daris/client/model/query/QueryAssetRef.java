package daris.client.model.query;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class QueryAssetRef extends ObjectRef<QueryAsset> {

    private String _id;
    private String _name;
    private String _description;

    public QueryAssetRef(String id, String name, String description) {
        _id = id;
        _name = name;
        _description = description;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("id", _id);
        w.add("content-as-xml", true);
    }

    @Override
    protected String resolveServiceName() {
        return "asset.get";
    }

    @Override
    protected QueryAsset instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            XmlElement ae = xe.element("asset");
            if (ae != null) {
                QueryAsset q = new QueryAsset(ae);
                _name = q.name();
                _description = q.description();
                return q;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return QueryAsset.class.getName();
    }

    @Override
    public String idToString() {
        return _id;
    }

    public String id() {
        return _id;
    }

}
