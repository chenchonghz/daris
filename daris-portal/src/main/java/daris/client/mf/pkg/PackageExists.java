package daris.client.mf.pkg;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class PackageExists extends ObjectMessage<Boolean> {

    private String _pkg;

    public PackageExists(String pkg) {
        _pkg = pkg;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("package", _pkg);
    }

    @Override
    protected String messageServiceName() {
        return "package.exists";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return xe.booleanValue("exists");
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "package";
    }

    @Override
    protected String idToString() {
        return _pkg;
    }

}
