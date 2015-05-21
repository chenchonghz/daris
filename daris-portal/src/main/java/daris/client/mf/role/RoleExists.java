package daris.client.mf.role;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class RoleExists extends ObjectMessage<Boolean> {

    private String _role;

    public RoleExists(String role) {
        _role = role;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("role", _role);
    }

    @Override
    protected String messageServiceName() {
        return "authorization.role.exists";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {
        return xe != null && xe.booleanValue("exists");
    }

    @Override
    protected String objectTypeName() {
        return "role";
    }

    @Override
    protected String idToString() {
        return _role;
    }

}
