package daris.client.mf.citeable;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class CiteableNameList extends ObjectMessage<List<String>> {

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        
    }

    @Override
    protected String messageServiceName() {
        return "citeable.name.list";
    }

    @Override
    protected List<String> instantiate(XmlElement xe) throws Throwable {
        if(xe!=null){
            return xe.values("name");
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
