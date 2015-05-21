package daris.client.mf.role;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.model.authentication.filter.RoleFilter;
import arc.mf.model.authorization.RoleRef;
import arc.mf.model.authorization.RoleRefCollection;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;

public class RoleEnumerationDataSource implements DynamicEnumerationDataSource<RoleRef> {

    @Override
    public boolean supportPrefix() {
        return true;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (value == null) {
            handler.exists(value, false);
            return;
        }
        new RoleExists(value).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean exists) {
                handler.exists(value, exists);
            }
        });
    }

    @Override
    public void retrieve(final String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<RoleRef> handler) {
        new RoleRefCollection(new RoleFilter() {

            @Override
            public long startOffset() {
                return start;
            }

            @Override
            public String rolePrefix() {
                return prefix;
            }
        }).resolve(start, end, new CollectionResolveHandler<RoleRef>() {
            @Override
            public void resolved(List<RoleRef> roles) throws Throwable {
                if (roles == null || roles.isEmpty()) {
                    handler.process(0, 0, 0, null);
                    return;
                }
                List<Value<RoleRef>> values = new ArrayList<Value<RoleRef>>();
                for (RoleRef role : roles) {
                    values.add(new Value<RoleRef>(role.name(), role.name(), role));
                }
                handler.process(start, end, values.size(), values);
            }
        });
    }

}
