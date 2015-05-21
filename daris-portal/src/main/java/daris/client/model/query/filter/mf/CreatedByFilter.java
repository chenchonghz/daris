package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class CreatedByFilter extends Filter {

    private String _domain;
    private String _user;

    private boolean _self = false;

    public CreatedByFilter(String domain, String user) {
        _domain = domain;
        _user = user;
        _self = false;
    }

    public CreatedByFilter() {
        _domain = null;
        _user = null;
        _self = true;
    }

    public CreatedByFilter(XmlElement xe) throws Throwable {
        _self = xe.booleanValue("self", false);
        if (!_self) {
            String actor = xe.value("actor");
            int idx = actor.lastIndexOf(':');
            _domain = actor.substring(0, idx);
            _user = actor.substring(idx + 1);
        }
    }

    public String domain() {
        return _domain;
    }

    public String user() {
        return _user;
    }

    public boolean self() {
        return _self;
    }

    public void setSelf(boolean self) {
        _self = self;
    }

    public void setActor(String domain, String user) {
        setDomain(domain);
        setUser(user);
    }

    public void setUser(String user) {
        _user = user;
        if (_user != null) {
            _self = false;
        }
    }

    public void setDomain(String domain) {
        _domain = domain;
        if (_domain != null) {
            _self = false;
        }
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("created by ");
        if (_self) {
            sb.append("me");
        } else {
            sb.append("'" + _domain + ":" + _user + "'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        if (_self) {
            w.add("self", true);
        } else {
            w.add("actor", _domain + ":" + _user);
        }
    }

    @Override
    public Validity valid() {
        if (!_self) {
            if (_domain == null) {
                return new IsNotValid("authentication domain is not set");
            }
            if (_user == null) {
                return new IsNotValid("authentication user is not set");
            }
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        if (self()) {
            return new CreatedByFilter();
        } else {
            return new CreatedByFilter(domain(), user());
        }
    }

}
