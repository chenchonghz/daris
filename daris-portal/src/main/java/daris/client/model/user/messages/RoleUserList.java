package daris.client.model.user.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.user.RoleUser;

public class RoleUserList extends ObjectMessage<List<RoleUser>> {

    public RoleUserList() {
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
    }

    @Override
    protected String messageServiceName() {

        return "daris.role-user.list";
    }

    @Override
    protected List<RoleUser> instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            List<XmlElement> rues = xe.elements("role-user");
            if (rues != null && !rues.isEmpty()) {
                List<RoleUser> rus = new ArrayList<RoleUser>(rues.size());
                for (XmlElement rue : rues) {
                    rus.add(new RoleUser(rue.value(), rue.value("@id")));
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
