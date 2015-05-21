package daris.client.model.exmethod.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodStepDescribe extends ObjectMessage<List<XmlElement>> {

    private String _exMethodId;
    private String _exMethodProute;
    private String _stepPath;

    public ExMethodStepDescribe(String exMethodId, String exMethodProute, String stepPath) {
        _exMethodId = exMethodId;
        _exMethodProute = exMethodProute;
        _stepPath = stepPath;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("id", new String[] { "proute", _exMethodProute }, _exMethodId);
        w.add("step", _stepPath);
    }

    @Override
    protected String messageServiceName() {
        return "om.pssd.ex-method.step.describe";
    }

    @Override
    protected List<XmlElement> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return xe.elements("ex-method/step");
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return _exMethodId;
    }

}
