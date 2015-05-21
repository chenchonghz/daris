package daris.client.mf.pkg;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class PackageRef extends ObjectRef<Package> {

    private String _name;
    private String _version;

    public PackageRef(String name) {
        this(name, null);
    }

    protected PackageRef(String name, String version) {
        _name = name;
        _version = version;
    }

    public String name() {
        return _name;
    }

    public String version() {
        return _version;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("package", _name);
    }

    @Override
    protected String resolveServiceName() {
        return "package.describe";
    }

    @Override
    protected Package instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            XmlElement pe = xe.element("package");
            if (pe != null) {
                return new Package(pe);
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "package";
    }

    @Override
    public String idToString() {
        return _name;
    }

}
