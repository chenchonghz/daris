package daris.client.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.client.model.exmethod.messages.ExMethodCreate;
import daris.client.model.exmethod.messages.ExMethodUpdate;
import daris.client.model.method.Method;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class ExMethod extends DObject {

    private Method _method;
    private XmlElement _methodElement;
    private List<ExMethodStep> _steps;
    private State _state;

    public ExMethod(XmlElement oe) throws Throwable {

        super(oe);

        try {
            _state = State.parse(oe.stringValue("state", State.incomplete.toString()));
        } catch (Throwable e) {
            _state = State.incomplete;
        }
        _method = null;
        _methodElement = oe.element("method");
        if (_methodElement != null) {
            _method = new Method(_methodElement);
        }
        _steps = null;
        List<XmlElement> ses = oe.elements("step");
        if (ses != null) {
            _steps = new Vector<ExMethodStep>(ses.size());
            for (XmlElement se : ses) {
                String stepPath = se.value("@path");
                String state = se.value("state");
                String notes = se.value("notes");
                ExMethodStep ems = new ExMethodStep(id(), proute(), stepPath, null, state == null ? null
                        : State.parse(state), notes, false);
                _steps.add(ems);
            }
        }
    }

    public ExMethod(String id, String proute, String name, String description) {
        super(id, proute, name, description, false, 0, false);
    }

    public Method method() {

        return _method;
    }

    public XmlElement methodElement() {

        return _methodElement;
    }

    public void setNotesForStep(String stepPath, String notes) {

        ExMethodStep ems = step(stepPath);
        if (ems != null) {
            ems.setNotes(notes);
        }
    }

    public void setStateForStep(String stepPath, State state) {

        ExMethodStep ems = step(stepPath);
        if (ems != null) {
            ems.setState(state);
        }
    }

    public ExMethodStep step(String stepPath) {

        if (_steps == null) {
            return null;
        }

        for (ExMethodStep ems : _steps) {
            if (ems.stepPath().equals(stepPath)) {
                return ems;
            }
        }
        return null;
    }

    public List<ExMethodStep> steps() {

        return _steps;
    }

    public State state() {

        return _state;
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.ex_method;
    }

    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        return new ExMethodCreate(po, this);
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        return new ExMethodUpdate(this);
    }

}
