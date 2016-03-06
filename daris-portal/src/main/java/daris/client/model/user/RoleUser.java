package daris.client.model.user;

public class RoleUser implements Comparable<RoleUser> {

    public static final String TYPE_NAME = "role";
    private String _id;
    private String _name;

    public RoleUser(String roleName, String id) {
        _id = id;
        _name = roleName;
    }

    public String id() {

        return _id;
    }

    public String name() {

        return _name;
    }

    @Override
    public int hashCode() {

        return _id.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o != null) {
            if (o instanceof RoleUser) {
                RoleUser ru = (RoleUser) o;
                return (ru.id().equals(_id) && ru.name().equals(_name));
            }
        }
        return false;
    }

    @Override
    public String toString() {

        return _name;
    }

    @Override
    public int compareTo(RoleUser o) {

        if (o == null) {
            return 1;
        }

        return String.CASE_INSENSITIVE_ORDER.compare(_name, o.name());
    }

    public String toHTML() {

        String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Role User</th></tr><thead>";
        html += "<tbody>";
        html += "<tr><td><b>id:</b></td><td>" + _id + "</td></tr>";
        html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
        html += "</tbody></table>";
        return html;
    }
}
