package daris.client.model.exmethod.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.exmethod.ExMethodSubjectStep;

public class ExMethodSubjectStepUpdate extends ObjectMessage<Boolean> {

    private String _exMethodId;
    private String _stepPath;
    private String _state;
    private String _notes;
    private XmlElement _psMeta;
    private XmlElement _rsMeta;

    public ExMethodSubjectStepUpdate(ExMethodSubjectStep step) {

        this(step.exMethodId(), step.stepPath(),
                step.state() == null ? null : step.state().toString(),
                step.notes(), step.psPublicMetadata(), step.rsPublicMetadata());
    }

    public ExMethodSubjectStepUpdate(String exMethodId, String stepPath,
            String state, String notes, XmlElement psMeta, XmlElement rsMeta) {

        _exMethodId = exMethodId;
        _stepPath = stepPath;
        _state = state;
        _notes = notes;
        _psMeta = psMeta;
        _rsMeta = rsMeta;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("id", _exMethodId);
        w.add("step", _stepPath);
        if (_state != null) {
            w.add("state", _state);
        }
        if (_notes != null) {
            w.add("notes", _notes);
        }
        if (_psMeta != null) {
            w.add(_psMeta);
        }
        if (_rsMeta != null) {
            w.add(_psMeta);
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.ex-method.subject.step.update";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {

        return xe != null;
    }

    @Override
    protected String objectTypeName() {

        return "ex-method.step";
    }

    @Override
    protected String idToString() {

        return _exMethodId + "_" + _stepPath;
    }

}
