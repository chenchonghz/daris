package daris.client.model.query.filter.pssd;

import daris.client.model.object.DObjectRef;
import daris.client.model.query.Query;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.options.ObjectQueryOptions;

public class ObjectQuery extends Query {

    public ObjectQuery(DObjectRef project) {
        super(new ObjectCompositeFilter(project), new ObjectQueryOptions(project));
    }
    
    public ObjectQuery(QueryAsset qa){
        super(qa);
    }

}
