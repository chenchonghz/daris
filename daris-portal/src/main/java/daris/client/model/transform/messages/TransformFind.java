package daris.client.model.transform.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.transform.Transform;

public class TransformFind extends ObjectMessage<List<Transform>> {

    public static final String SERVICE_NAME = "om.pssd.transform.find";

    public long _defnId;
    public int _defnVersion;
    public String _scopeId;

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("definition", new String[] { "version", _defnVersion > 0 ? Integer.toString(_defnVersion) : null },
                _defnId);
        if (_scopeId != null) {
            w.add("scope", _scopeId);
        }
        w.add("size", "infinity");
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected List<Transform> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<XmlElement> tes = xe.elements("transform");
            if (tes != null && !tes.isEmpty()) {
                List<Transform> ts = new ArrayList<Transform>(tes.size());
                for (XmlElement te : tes) {
                    ts.add(new Transform(te));
                }
                if (!ts.isEmpty()) {
                    return ts;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {

        return "List of transform";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
