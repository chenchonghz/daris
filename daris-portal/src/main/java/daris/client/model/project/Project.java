package daris.client.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.project.messages.ProjectCreate;
import daris.client.model.project.messages.ProjectMemberReplace;
import daris.client.model.project.messages.ProjectMetadataDescribe;
import daris.client.model.project.messages.ProjectRoleMemberReplace;
import daris.client.model.project.messages.ProjectUpdate;

public class Project extends DObject {

    public static final String SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT = "daris:pssd.project.admin";
    public static final String SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT = "daris:pssd.project.subject.admin";
    public static final String SPECIFIC_MEMBER_ROLE_NAME_ROOT = "daris:pssd.project.member";
    public static final String SPECIFIC_GUEST_ROLE_NAME_ROOT = "daris:pssd.project.guest";

    public static final String CID_ROOT_NAME_DICTIONARY = "daris:pssd.project.cid.rootnames";

    public static final String CID_ROOT_NAME_DEFAULT = "pssd.project";

    public static final String ASSET_NAMESPACE_DICTIONARY = "daris:pssd.project.asset.namespaces";

    private DataUse _dataUse;
    private List<MethodRef> _methods;
    private List<ProjectMember> _members;
    private List<ProjectRoleMember> _roleMembers;

    public Project() {

        this(null, null, null, null);
    }

    public Project(String id, String proute, String name, String description) {
        super(id, proute, name, description, false, 0, false);
        _cidRootName = Project.CID_ROOT_NAME_DEFAULT;
    }

    public Project(XmlElement oe) throws Throwable {

        super(oe);

        /*
         * data-use
         */
        _dataUse = DataUse.parse(oe.value("data-use"));
        /*
         * methods
         */
        List<XmlElement> mthdes = oe.elements("method");
        if (mthdes != null) {
            if (!mthdes.isEmpty()) {
                _methods = new Vector<MethodRef>(mthdes.size());
                for (XmlElement mde : mthdes) {
                    _methods.add(new MethodRef(mde.value("id"), mde
                            .value("name"), mde.value("description")));
                }
            }
        }
        /*
         * members
         */
        List<XmlElement> mbes = oe.elements("member");
        if (mbes != null) {
            if (!mbes.isEmpty()) {
                _members = new Vector<ProjectMember>(mbes.size());
                for (XmlElement mbe : mbes) {
                    _members.add(new ProjectMember(mbe));
                }
            }
        }
        /*
         * role-members
         */
        List<XmlElement> rmbes = oe.elements("role-member");
        if (rmbes != null) {
            if (!rmbes.isEmpty()) {
                _roleMembers = new Vector<ProjectRoleMember>(rmbes.size());
                for (XmlElement rmbe : rmbes) {
                    _roleMembers.add(new ProjectRoleMember(rmbe));
                }
            }
        }
    }

    public List<MethodRef> methods() {

        return _methods;
    }

    public void setMethods(List<MethodRef> methods) {

        _methods = methods;
    }

    public boolean hasMethods() {

        if (_methods == null) {
            return false;
        }
        return !_methods.isEmpty();
    }

    public void setNamespace(String namespace) {
        super.setNamespace(namespace);
    }

    public DataUse dataUse() {

        return _dataUse;
    }

    public void setDataUse(DataUse dataUse) {

        _dataUse = dataUse;
    }

    private String _cidRootName;

    public String cidRootName() {
        return _cidRootName;
    }

    public void setCidRootName(String cidRootName) {
        _cidRootName = cidRootName;
    }

    public List<ProjectMember> members() {

        return _members;
    }

    public void addMember(ProjectMember pm) {

        if (_members == null) {
            _members = new Vector<ProjectMember>();
        }
        int index = -1;
        for (int i = 0; i < _members.size(); i++) {
            ProjectMember member = _members.get(i);
            if (member.user().equals(pm.user())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            _members.remove(index);
        }
        _members.add(pm);
    }

    public void removeMember(ProjectMember pm) {

        if (pm == null) {
            return;
        }
        if (_members != null) {
            ProjectMember rm = null;
            for (ProjectMember m : _members) {
                if (m.user().equals(pm.user())) {
                    rm = m;
                    break;
                }
            }
            if (rm != null) {
                _members.remove(rm);
            }
        }
    }

    public List<ProjectRoleMember> roleMembers() {

        return _roleMembers;
    }

    public void addRoleMember(ProjectRoleMember prm) {

        if (_roleMembers == null) {
            _roleMembers = new Vector<ProjectRoleMember>();
        }
        int index = -1;
        for (int i = 0; i < _roleMembers.size(); i++) {
            ProjectRoleMember rm = _roleMembers.get(i);
            if (rm.member().equals(prm.member())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            _roleMembers.remove(index);
        }
        _roleMembers.add(prm);
    }

    public void removeRoleMember(ProjectRoleMember prm) {

        if (prm == null) {
            return;
        }
        if (_roleMembers != null) {
            ProjectRoleMember rrm = null;
            for (ProjectRoleMember rm : _roleMembers) {
                if (rm.member().equals(prm.member())) {
                    rrm = rm;
                    break;
                }
            }
            if (rrm != null) {
                _roleMembers.remove(rrm);
            }
        }
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.project;
    }

    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        return new ProjectCreate(this);
    }

    @Override
    public void createServiceArgs(XmlWriter w) {

        super.createServiceArgs(w);
        if (namespace() != null) {
            w.add("namespace", namespace());
        }
        w.add("data-use", _dataUse);
        if (_methods != null) {
            for (MethodRef m : _methods) {
                w.push("method");
                w.add("id", m.id());
                w.pop();
            }
        }
        if (_members != null) {
            for (ProjectMember pm : _members) {
                w.push("member");
                if (pm.user().domain().authority().name() != null) {
                    if (pm.user().domain().authority().protocol() != null) {
                        w.add("authority", new String[] { "protocol",
                                pm.user().domain().authority().protocol() }, pm
                                .user().domain().authority().name());
                    } else {
                        w.add("authority", pm.user().domain().authority()
                                .name());
                    }
                }
                w.add("domain", pm.user().domain());
                w.add("user", pm.user().name());
                w.add("role", pm.role());
                if (pm.dataUse() != null) {
                    w.add("data-use", pm.dataUse());
                }
                w.pop();
            }
        }
        if (_roleMembers != null) {
            for (ProjectRoleMember prm : _roleMembers) {
                w.push("role-member");
                w.add("member", prm.member().name());
                w.add("role", prm.role());
                if (prm.dataUse() != null) {
                    w.add("data-use", prm.dataUse());
                }
                w.pop();
            }
        }
        if (_cidRootName != null) {
            w.add("cid-root-name", _cidRootName);
        }
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        return new ProjectUpdate(this);
    }

    @Override
    public void updateServiceArgs(XmlWriter w) {

        super.updateServiceArgs(w);
        // w.add("namespace", _nameSpace);
        w.add("data-use", _dataUse);
        if (_methods != null) {
            for (MethodRef m : _methods) {
                w.push("method");
                w.add("id", m.id());
                w.pop();
            }
        }
        if (_members != null) {
            for (ProjectMember pm : _members) {
                w.push("member");
                if (pm.user().domain().authority() != null
                        && pm.user().domain().authority().name() != null) {
                    if (pm.user().domain().authority().protocol() != null) {
                        w.add("authority", new String[] { "protocol",
                                pm.user().domain().authority().protocol() }, pm
                                .user().domain().authority().name());
                    } else {
                        w.add("authority", pm.user().domain().authority()
                                .name());
                    }
                }
                w.add("domain", pm.user().domain());
                w.add("user", pm.user().name());
                w.add("role", pm.role());
                if (pm.dataUse() != null) {
                    w.add("data-use", pm.dataUse());
                }
                w.pop();
            }
        }
        if (_roleMembers != null) {
            for (ProjectRoleMember prm : _roleMembers) {
                w.push("role-member");
                w.add("member", prm.member().name());
                w.add("role", prm.role());
                if (prm.dataUse() != null) {
                    w.add("data-use", prm.dataUse());
                }
                w.pop();
            }
        }
    }

    public boolean hasMembers() {

        if (_members != null) {
            return !_members.isEmpty();
        }
        return false;
    }

    public boolean hasRoleMembers() {

        if (_roleMembers != null) {
            return !_roleMembers.isEmpty();
        }
        return false;
    }

    public boolean hasMembersOrRoleMembers() {

        return hasMembers() || hasRoleMembers();
    }

    public boolean hasAdminMember() {

        if (hasMembers()) {
            for (ProjectMember pm : _members) {
                if (pm.role().equals(ProjectRole.PROJECT_ADMINISTRATOR)) {
                    return true;
                }
            }
        }
        if (hasRoleMembers()) {
            for (ProjectRoleMember rm : _roleMembers) {
                if (rm.role().equals(ProjectRole.PROJECT_ADMINISTRATOR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void commitMembers(ObjectMessageResponse<Boolean> rh) {

        new ProjectMemberReplace(this).send(rh);
    }

    public void commitRoleMembers(ObjectMessageResponse<Boolean> rh) {

        new ProjectRoleMemberReplace(this).send(rh);
    }

    /**
     * Set the meta data definition of a project object (to be created).
     * 
     * @param o
     *            the project to be created.
     */
    public static void setMetaForEdit(final Project o, final ActionListener al) {

        new ProjectMetadataDescribe()
                .send(new ObjectMessageResponse<XmlElement>() {

                    @Override
                    public void responded(XmlElement metaForEdit) {

                        o.setMetaForEdit(metaForEdit);
                        al.executed(true);
                    }
                });
    }

    public static String adminRoleFromId(String cid) {
        String projectCid = IDUtil.getProjectId(cid);
        if (projectCid == null) {
            return null;
        }
        return Project.SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT + "." + projectCid;
    }

    public static String subjectAdminRoleFromId(String cid) {
        String projectCid = IDUtil.getProjectId(cid);
        if (projectCid == null) {
            return null;
        }
        return Project.SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT + "."
                + projectCid;
    }

    public static String memberRoleFromeId(String cid) {
        String projectCid = IDUtil.getProjectId(cid);
        if (projectCid == null) {
            return null;
        }
        return Project.SPECIFIC_MEMBER_ROLE_NAME_ROOT + "." + projectCid;
    }

    public static String guestRoleFromeId(String cid) {
        String projectCid = IDUtil.getProjectId(cid);
        if (projectCid == null) {
            return null;
        }
        return SPECIFIC_GUEST_ROLE_NAME_ROOT + "." + projectCid;
    }

}
