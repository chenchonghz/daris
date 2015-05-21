package daris.client.model.exmethod.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.exmethod.StepItem;

public class ExMethodStudyStepFind extends ObjectMessage<List<StepItem>> {

    private String _exMethodId;
    private String _exMethodProute;
    private String _type;

    public ExMethodStudyStepFind(String exMethodId, String exMethodProute, String type) {

        _exMethodId = exMethodId;
        _exMethodProute = exMethodProute;
        _type = type;
    }

    public ExMethodStudyStepFind(String exMethodId, String exMethodProute) {

        this(exMethodId, exMethodProute, null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        if (_type != null) {
            w.add("type", _type);
        }
        w.add("id", new String[] { "proute", _exMethodProute }, _exMethodId);
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.ex-method.study.step.find";
    }

    @Override
    protected List<StepItem> instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            List<XmlElement> ses = xe.elements("ex-method/step");
            if (ses != null) {
                List<StepItem> ss = new Vector<StepItem>(ses.size());
                for (XmlElement se : ses) {
                    ss.add(new StepItem(se));
                }
                if (!ss.isEmpty()) {
                    return ss;
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
