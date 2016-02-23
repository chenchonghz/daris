package daris.client.model.pkg.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.pkg.PackageRef;

public class PackageList extends ObjectMessage<List<PackageRef>> {

    public PackageList() {

    }

    @Override
    protected String idToString() {
        return null;
    }

    @Override
    protected List<PackageRef> instantiate(Element xe) throws Throwable {
        List<XmlDoc.Element> pes = xe.elements("package");
        if (pes != null && !pes.isEmpty()) {
            List<PackageRef> pkgs = new ArrayList<PackageRef>();
            for (XmlDoc.Element pe : pes) {
                String name = pe.value();
                if (name.startsWith("daris")) {
                    String version = pe.value("@version");
                    pkgs.add(new PackageRef(name, version));
                }
            }
            if (!pkgs.isEmpty()) {
                return pkgs;
            }
        }
        return null;
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {

    }

    @Override
    protected String messageServiceName() {
        return "package.list";
    }

    @Override
    protected String objectTypeName() {
        // TODO Auto-generated method stub
        return null;
    }

}
