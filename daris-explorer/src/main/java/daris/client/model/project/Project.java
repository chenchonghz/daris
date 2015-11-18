package daris.client.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.DataUse;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;

public class Project extends DObject {

    private DataUse _dataUse;
    private List<MethodRef> _methods;
    private List<ProjectMember> _members;
    private List<ProjectRoleMember> _roleMembers;

    public Project(Element oe) throws Throwable {
        super(oe);
        _dataUse = DataUse.fromString(oe.value("data-use"));
        if (oe.elementExists("method")) {
            List<XmlDoc.Element> mes = oe.elements("method");
            _methods = new ArrayList<MethodRef>(mes.size());
            for (XmlDoc.Element me : mes) {
                String mCid = me.value("id");
                String mName = me.value("name");
                String mDesc = me.value("description");
                MethodRef method = new MethodRef(mCid, mName, mDesc);
                _methods.add(method);
            }
        }
        if (oe.elementExists("member")) {
            List<XmlDoc.Element> mes = oe.elements("member");
            _members = new ArrayList<ProjectMember>(mes.size());
            for (XmlDoc.Element me : mes) {
                String domain = me.value("@domain");
                String user = me.value("@user");
                ProjectSpecificRole role = ProjectSpecificRole
                        .fromString(me.value("@role"));
                ProjectMember member = new ProjectMember(domain, user, role);
                _members.add(member);
            }
        }
        if (oe.elementExists("role-member")) {
            List<XmlDoc.Element> rmes = oe.elements("role-member");
            _roleMembers = new ArrayList<ProjectRoleMember>(rmes.size());
            for (XmlDoc.Element rme : rmes) {
                String member = rme.value("@member");
                ProjectSpecificRole role = ProjectSpecificRole
                        .fromString(rme.value("@role"));
                ProjectRoleMember roleMember = new ProjectRoleMember(member,
                        role);
                _roleMembers.add(roleMember);
            }
        }
    }

    public List<MethodRef> methods() {
        if (_methods == null) {
            return null;
        }
        return Collections.unmodifiableList(_methods);
    }

    public List<ProjectMember> members() {
        if (_members == null) {
            return null;
        }
        return Collections.unmodifiableList(_members);
    }

    public DataUse dataUse() {
        return _dataUse;
    }

    @Override
    public final Type type() {
        return DObject.Type.PROJECT;
    }

}
