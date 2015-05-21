package daris.client.model.query;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.options.QueryOptions;

public class AssetResultCollectionRef extends ResultCollectionRef<arc.mf.model.asset.AssetRef> {

    public AssetResultCollectionRef(AssetQuery query) {
        super(query);
    }

    @Override
    protected arc.mf.model.asset.AssetRef instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            AssetRef a = new AssetRef(query().options().action() == QueryOptions.Action.get_id ? xe.longValue()
                    : xe.longValue("@id"), xe.intValue("@version"));
            addXPathValues(a, xe);
            return a;
        }
        throw new AssertionError("Failed to instantiate " + xe);
    }

}
