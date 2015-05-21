package daris.client.mf.pkg;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;


public class PackageList extends ObjectMessage<List<PackageRef>>{

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        
    }

    @Override
    protected String messageServiceName() {
        return "package.list";
    }

    @Override
    protected List<PackageRef> instantiate(XmlElement xe) throws Throwable {
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "packages";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
