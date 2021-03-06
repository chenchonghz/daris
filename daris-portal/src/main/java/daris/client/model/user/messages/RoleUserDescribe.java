package daris.client.model.user.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.user.RoleUser;

public class RoleUserDescribe extends ObjectMessage<List<RoleUser>> {

    private Boolean _listProjects;

    protected RoleUserDescribe(Boolean listProjects) {
        _listProjects = listProjects;
    }

    public RoleUserDescribe() {
        this(null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_listProjects != null) {
            w.add("list-projects", _listProjects);
        }
    }

    @Override
    protected String messageServiceName() {

        return "daris.user.describe";
    }

    @Override
    protected List<RoleUser> instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            List<XmlElement> rues = xe.elements("role-user");
            if (rues != null && !rues.isEmpty()) {
                List<RoleUser> rus = new ArrayList<RoleUser>(rues.size());
                for (XmlElement rue : rues) {
                    rus.add(new RoleUser(rue.value("@member"),
                            rue.value("@id")));
                }
                if (!rus.isEmpty()) {
                    return rus;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {

        return "List of role-users";
    }

    @Override
    protected String idToString() {

        return null;
    }

}
