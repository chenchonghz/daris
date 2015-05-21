package daris.client.model.exmethod;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class ExMethodStepRef extends ObjectRef<ExMethodStep> {

    private String _exMethodId;
    private String _exMethodProute;
    private String _stepPath;

    public ExMethodStepRef(String exMethodId, String exMethodProute, String stepPath) {

        _exMethodId = exMethodId;
        _exMethodProute = exMethodProute;
        _stepPath = stepPath;
    }

    public String exMethodId() {

        return _exMethodId;
    }

    public String exMethodProute() {
        return _exMethodProute;
    }

    public String stepPath() {

        return _stepPath;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {

        w.add("id", new String[] { "proute", _exMethodProute }, _exMethodId);
        w.add("step", _stepPath);
    }

    @Override
    protected String resolveServiceName() {

        return "om.pssd.ex-method.step.describe";
    }

    @Override
    protected ExMethodStep instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            if (xe.element("ex-method") != null) {
                String name = xe.value("ex-method/step/name");
                String state = xe.value("ex-method/step/status/state");
                String notes = xe.value("ex-method/step/status/notes");
                if (xe.element("ex-method/step/study") != null) {
                    String studyType = xe.value("ex-method/step/study/type");
                    List<XmlElement> meta = xe.elements("ex-method/step/study/metadata");
                    return new ExMethodStudyStep(_exMethodId, _exMethodProute, _stepPath, name, state == null ? null
                            : State.parse(state), notes, studyType, meta, true);
                } else if (xe.element("ex-method/step/subject") != null) {
                    XmlElement pse = xe.element("ex-method/step/subject[@type='p']");
                    List<XmlElement> psMeta = null;
                    if (pse != null) {
                        psMeta = pse.elements("metadata");
                    }
                    XmlElement rse = xe.element("ex-method/step/subject[@type='r']");
                    List<XmlElement> rsMeta = null;
                    if (rse != null) {
                        rsMeta = rse.elements("metadata");
                    }
                    return new ExMethodSubjectStep(_exMethodId, _exMethodProute, _stepPath, name, state == null ? null
                            : State.parse(state), notes, psMeta, rsMeta, true);
                } else if (xe.element("ex-method/step/transform") != null) {
                    XmlElement te = xe.element("ex-method/step/transform");
                    return new ExMethodTransformStep(te, _exMethodId, _exMethodProute, _stepPath, name,
                            state == null ? null : State.parse(state), notes, true);
                }
            }
        }
        throw new Exception("Failed to instantiate ExMethodStep: invalid XML element: " + xe == null ? "null"
                : xe.toString());
    }

    @Override
    public String referentTypeName() {

        return "ex-method.step";
    }

    @Override
    public String idToString() {

        return _exMethodId + "_" + _stepPath;
    }

}
