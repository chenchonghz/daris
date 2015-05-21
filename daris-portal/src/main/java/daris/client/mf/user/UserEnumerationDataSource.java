package daris.client.mf.user;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.model.authentication.Domain;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserCollectionRef;
import arc.mf.model.authentication.UserRef;
import arc.mf.model.authentication.filter.UserFilter;
import arc.mf.model.authentication.messages.UserExists;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;

public class UserEnumerationDataSource implements DynamicEnumerationDataSource<UserRef> {

    private DomainRef _domain;

    public UserEnumerationDataSource(DomainRef domain) {
        _domain = domain;
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {

        String user = value == null ? null : (value.indexOf(':') == -1 ? value : value
                .substring(value.indexOf(':') + 1));

        if (_domain == null) {
            handler.exists(value, false);
            return;
        }
        DomainRef domain = (_domain.authority() != null && _domain.authority().name() == null) ? new DomainRef(null,
                _domain.name(), Domain.Type.LOCAL, null) : _domain;
        new UserExists(domain, user).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean exists) {
                handler.exists(value, exists);
            }
        });

    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<UserRef> handler) {
        final String userPrefix = prefix == null ? null : (prefix.indexOf(':') == -1 ? prefix : prefix
                .substring(prefix.indexOf(':') + 1));
        if (_domain == null) {
            handler.process(0, 0, 0, null);
            return;
        }
        new UserCollectionRef(_domain, new UserFilter() {

            @Override
            public long startOffset() {
                return start;
            }

            @Override
            public String userPrefix() {
                return userPrefix;
            }
        }).resolve(start, end, new CollectionResolveHandler<UserRef>() {

            @Override
            public void resolved(List<UserRef> users) throws Throwable {
                if (users == null || users.isEmpty()) {
                    handler.process(0, 0, 0, null);
                    return;
                }
                List<Value<UserRef>> values = new ArrayList<Value<UserRef>>();
                for (UserRef user : users) {
                    values.add(new Value<UserRef>(user.name(), user.name(), user));
                }
                handler.process(start, end, values.size(), values);
            }
        });
    }

    public void setDomain(DomainRef domain) {
        _domain = domain;
    }

}
