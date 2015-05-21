package daris.client.model.exmethod.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObject;

public class ExMethodSubjectStepFind extends ObjectMessage<List<String>> {

    private String _exMethodId;
    private String _exMethodProute;

    public ExMethodSubjectStepFind(String exMethodId, String exMethodProute) {

        _exMethodId = exMethodId;
        _exMethodProute = exMethodProute;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("id", new String[] { "proute", _exMethodProute }, _exMethodId);
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.ex-method.subject.step.find";
    }

    @Override
    protected List<String> instantiate(XmlElement xe) throws Throwable {

        return xe.values("ex-method/step");
    }

    @Override
    protected String objectTypeName() {

        return DObject.Type.ex_method.toString();
    }

    @Override
    protected String idToString() {

        return _exMethodId;
    }

}
