package daris.client.model.query;

import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.options.AssetQueryOptions;

public class AssetQuery extends Query {

    public AssetQuery() {
        super(new CompositeFilter(), new AssetQueryOptions());
    }
    
    public AssetQuery(QueryAsset qa){
        super(qa);
    }

}
