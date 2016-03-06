package daris.client.model.user.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.authentication.Authority;
import arc.mf.model.authentication.Domain;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;
import arc.mf.object.ObjectMessage;

public class UserDescribe extends ObjectMessage<List<UserRef>> {

    public static final String SERVICE_NAME = "daris.user.describe";

    private DomainRef _domain;
    private Boolean _excludeSystemDomain;
    private String _user;

    UserDescribe(DomainRef domain, Boolean excludeSystemDomain, String user) {
        _domain = domain;
        _excludeSystemDomain = excludeSystemDomain;
        _user = user;
    }

    public UserDescribe(DomainRef domain, String user) {
        this(domain,
                domain == null ? true
                        : (domain.name().equals("system") ? false : true),
                user);
    }

    public UserDescribe(DomainRef domain) {
        this(domain, null);
    }

    public UserDescribe(Boolean excludeSystemDomain) {
        this(null, excludeSystemDomain, null);
    }

    public UserDescribe() {
        this(null, true, null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_domain != null) {
            if (_domain.authority() != null
                    && _domain.authority().name() != null) {
                if (_domain.authority().protocol() != null) {
                    w.add("authority",
                            new String[] { "protocol",
                                    _domain.authority().protocol() },
                            _domain.authority().name());
                } else {
                    w.add("authority", _domain.authority().name());
                }
            }
            w.add("domain", _domain.name());
        }
        if (_excludeSystemDomain != null) {
            w.add("exclude-system-domain", _excludeSystemDomain);
        }
        if (_user != null) {
            w.add("user", _user);
        }
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected List<UserRef> instantiate(XmlElement xe) throws Throwable {
        List<XmlElement> ues = xe.elements("user");
        if (ues != null && !ues.isEmpty()) {
            List<UserRef> us = new ArrayList<UserRef>(ues.size());
            for (XmlElement ue : ues) {
                Authority authority = null;
                if (ue.value("@authority") != null) {
                    authority = new Authority(ue.value("@protocol"),
                            ue.value("@authority"));
                }
                DomainRef domain = new DomainRef(authority, ue.value("@domain"),
                        parseDomainType(ue.value("@domain-type")), null);
                UserRef u = new UserRef(domain, ue.value("@user"));
                String email = ue.value("email");
                if (email != null) {
                    u.setEmail(email);
                }
                String fullName = getFullName(ue);
                if (fullName != null) {
                    u.setPersonName(fullName);
                }
                us.add(u);
            }
            if (!us.isEmpty()) {
                return us;
            }
        }
        return null;
    }

    static String getFullName(XmlElement ue) {
        String firstName = ue.value("name[@type='first']");
        String middleName = ue.value("name[@type='middle']");
        String lastName = ue.value("name[@type='last']");
        String fullName = "";
        if (firstName != null) {
            fullName += firstName;
        }
        if (middleName != null) {
            fullName += " " + middleName;
        }
        if (lastName != null) {
            fullName += " " + lastName;
        }
        fullName = fullName.trim();
        if (fullName.isEmpty()) {
            return null;
        }
        return fullName;
    }

    static Domain.Type parseDomainType(String type) {
        Domain.Type[] vs = Domain.Type.values();
        for (Domain.Type v : vs) {
            if (v.name().equalsIgnoreCase(type)) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String idToString() {
        // TODO Auto-generated method stub
        return null;
    }

}
