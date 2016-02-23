package daris.client.model.pkg;

public class PackageRef {

    private String _name;
    private String _version;

    public PackageRef(String name, String version) {
        _name = name;
        _version = version;
    }

    public String name() {
        return _name;
    }

    public String version() {
        return _version;
    }

}
