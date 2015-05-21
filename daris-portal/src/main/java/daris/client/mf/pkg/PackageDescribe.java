package daris.client.mf.pkg;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class PackageDescribe extends
        ObjectMessage<List<daris.client.mf.pkg.Package>> {

    private String _pkg;

    public PackageDescribe(String pkg) {
        _pkg = pkg;
    }

    public PackageDescribe() {
        this(null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_pkg != null) {
            w.add("package", _pkg);
        }
    }

    @Override
    protected String messageServiceName() {
        return "package.describe";
    }

    @Override
    protected List<daris.client.mf.pkg.Package> instantiate(XmlElement xe)
            throws Throwable {
        if (xe != null) {
            List<XmlElement> pes = xe.elements("package");
            if (pes != null) {
                List<daris.client.mf.pkg.Package> pkgs = new ArrayList<daris.client.mf.pkg.Package>(
                        pes.size());
                for (XmlElement pe : pes) {
                    pkgs.add(new daris.client.mf.pkg.Package(pe));
                }
                if (!pkgs.isEmpty()) {
                    return pkgs;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return null;
    }

}
