package daris.client.model.project;

public class ProjectRoleMember {

    private String _member;
    private ProjectSpecificRole _role;

    public ProjectRoleMember(String member, ProjectSpecificRole role) {
        _member = member;
        _role = role;
    }

    public String member() {
        return _member;
    }

    public ProjectSpecificRole role() {
        return _role;
    }

}
