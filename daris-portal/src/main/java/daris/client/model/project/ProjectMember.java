package daris.client.model.project;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.model.authentication.Authority;
import arc.mf.model.authentication.Domain;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;

public class ProjectMember implements Comparable<ProjectMember> {

    private UserRef _user;
    private ProjectRole _role;
    private DataUse _dataUse;

    public ProjectMember(XmlElement me) {

        long userId = -1;
        try {
            userId = me.longValue("@id");
        } catch (Throwable e) {
        }
        String authorityName = me.value("@authority");
        String authorityProtocol = me.value("@protocol");
        Authority authority = authorityName == null ? Domain.AUTHORITY_MEDIAFLUX
                : new Authority(authorityProtocol, authorityName);
        DomainRef domain = new DomainRef(authority, me.value("@domain"),
                Domain.Type.LOCAL, null);
        _user = new UserRef(userId, domain, me.value("@user"));
        String userEmail = me.value("email");
        if (userEmail != null) {
            _user.setEmail(userEmail);
        }
        String userName = me.value("name");
        if (userName != null) {
            _user.setPersonName(userName);
        }
        _role = ProjectRole.parse(me.value("@role"));
        _dataUse = DataUse.parse(me.value("@data-use"));
    }

    public ProjectMember(UserRef user, ProjectRole role, DataUse dataUse) {

        _user = user;
        assert role != null;
        _role = role;
        if (_role.equals(ProjectRole.PROJECT_ADMINISTRATOR)
                || _role.equals(ProjectRole.SUBJECT_ADMINISTRATOR)) {
            _dataUse = null;
        } else {
            _dataUse = dataUse;
        }
    }

    public UserRef user() {

        return _user;
    }

    public ProjectRole role() {

        return _role;
    }

    public DataUse dataUse() {

        return _dataUse;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (!(o instanceof ProjectMember)) {
            return false;
        }
        ProjectMember pm = (ProjectMember) o;
        return _user.equals(pm.user()) && _role.equals(pm.role())
                && ObjectUtil.equals(_dataUse, pm.dataUse());
    }

    public boolean isSameUser(ProjectMember pm) {

        if (pm == null) {
            return false;
        }
        return _user.equals(pm.user());
    }

    @Override
    public String toString() {

        String s = ":member";
        if (_user.domain().authority() != null
                && _user.domain().authority().name() != null) {
            s += " -authority \"" + _user.domain().authority().name() + "\"";
        }
        s += " -domain \"" + _user.domain() + "\"";
        s += " -user \"" + _user.name() + "\"";
        s += " -role \"" + _role.toString() + "\"";
        if (_dataUse != null) {
            s += " -data-use \"" + _dataUse.toString() + "\"";
        }
        return s;
    }

    public String toHTML() {

        String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Member</th></tr><thead>";
        html += "<tbody>";

        if (_user.domain().authority() != null
                && _user.domain().authority().name() != null) {
            html += "<tr><td><b>authority:</b></td><td>"
                    + _user.domain().authority().name() + "</td></tr>";
        }

        html += "<tr><td><b>domain:</b></td><td>" + _user.domain()
                + "</td></tr>";
        html += "<tr><td><b>user:</b></td><td>" + _user.name() + "</td></tr>";
        html += "<tr><td><b>role:</b></td><td>" + _role + "</td></tr>";
        if (_dataUse != null) {
            html += "<tr><td><b>dataUse:</b></td><td>" + _dataUse
                    + "</td></tr>";
        }
        html += "</tbody></table>";
        return html;
    }

    @Override
    public int compareTo(ProjectMember o) {

        if (o == null) {
            return 1;
        }
        if (_role.ordinal() > o.role().ordinal()) {
            return 1;
        }
        if (_role.ordinal() < o.role().ordinal()) {
            return -1;
        }
        return _user.actorName().compareTo(o.user().actorName());
    }

    public void setRole(ProjectRole role) {

        _role = role;
    }

    public void setDataUse(DataUse dataUse) {

        if (_role.equals(ProjectRole.PROJECT_ADMINISTRATOR)
                || _role.equals(ProjectRole.SUBJECT_ADMINISTRATOR)) {
            _dataUse = null;
        } else {
            _dataUse = dataUse;
        }
    }
}
