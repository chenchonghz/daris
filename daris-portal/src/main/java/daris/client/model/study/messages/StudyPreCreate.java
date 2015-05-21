package daris.client.model.study.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

/**
 * Wrapper for om.pssd.ex-method.study.precreate service
 * 
 * @author nebk
 *
 */
public class StudyPreCreate extends ObjectMessage<List<DObjectRef>> {

    private DObjectRef _exm;
    private Boolean _methodMeta;
    private List<String> _steps;

    public StudyPreCreate(DObjectRef exm) {
        _exm = exm;
    }

    public boolean containsStep(String step) {
        if (_steps != null) {
            return _steps.contains(step);
        }
        return false;
    }

    public void setMethodMeta(boolean methodMeta) {
        _methodMeta = methodMeta;
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.ex-method.studies.precreate";
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("pid", new String[] { "proute", _exm.proute() }, _exm.id());
        if (_methodMeta != null) {
            w.add("method-meta", _methodMeta);
        }
        if (_steps != null) {
            for (String step : _steps) {
                w.add("step", step);
            }
        }
    }

    @Override
    protected List<DObjectRef> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<XmlElement> ides = xe.elements("id");
            if (ides != null) {
                List<DObjectRef> os = new ArrayList<DObjectRef>(ides.size());
                for (XmlElement ide : ides) {
                    os.add(new DObjectRef(ide.value(), 0));
                }
                if (!os.isEmpty()) {
                    return os;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return DObject.Type.study.toString();
    }

    @Override
    protected String idToString() {
        return _exm.id();
    }

    public void setSteps(List<String> steps) {
        _steps = steps;

    }

}
