package daris.client.model.exmethod.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.transform.Transform;

public class ExMethodStepTransformFind extends ObjectMessage<List<Transform>> {

    public static final String SERVICE_NAME = "om.pssd.ex-method.step.transform.find";

    private String _exMethodId;
    private String _step;

    public ExMethodStepTransformFind(String exMethodId, String step) {
        _exMethodId = exMethodId;
        _step = step;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("id", _exMethodId);
        w.add("step", _step);
        w.add("size", "infinity");
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected List<Transform> instantiate(XmlElement xe) throws Throwable {
        List<XmlElement> tes = xe.elements("transform");
        if (tes != null && !tes.isEmpty()) {
            List<Transform> ts = new ArrayList<Transform>();
            for (XmlElement te : tes) {
                ts.add(new Transform(te));
            }
            return ts;
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "ex-method step";
    }

    @Override
    protected String idToString() {
        return _exMethodId + "_" + _step;
    }

}
