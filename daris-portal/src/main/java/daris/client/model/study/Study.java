package daris.client.model.study;

import java.util.List;

import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlAttribute;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.study.messages.StudyCreate;
import daris.client.model.study.messages.StudyMetadataDescribe;
import daris.client.model.study.messages.StudyUpdate;

public class Study extends DObject {

    private String _exMethodId;
    private String _stepPath;
    private String _studyType;
    private XmlElement _methodMeta;
    private XmlElement _methodMetaForEdit;
    private Boolean _processed;

    public Study(String exMethodId) {

        super(null, null, null, null, false, 0, false);
        _exMethodId = exMethodId;
    }

    public Study(XmlElement xe) throws Throwable {

        super(xe);
        _studyType = xe.value("type");
        // processed is optional
        _processed = null;
        XmlElement t = xe.element("processed");
        if (t!=null) _processed = t.booleanValue();
        //
        _exMethodId = xe.value("method/id");
        _stepPath = xe.value("method/step");
        XmlElement mme = xe.element("method/meta");
        if (mme != null) {
            if (mme.element("metadata") != null) {
                _methodMetaForEdit = mme;
            } else {
                _methodMeta = mme;
            }
        }
    }

    public Study(String id, String proute, String name, String description) {
        super(id, proute, name, description, false, 0, false);
    }

    public String studyType() {

        return _studyType;
    }

    public void setStudyType(String studyType) {

        _studyType = studyType;
    }

    public String exMethodId() {

        return _exMethodId;
    }

    public String stepPath() {

        return _stepPath;
    }

    public void setStepPath(String stepPath) {

        _stepPath = stepPath;
    }

    public XmlElement methodMeta() {

        return _methodMeta;
    }
    
    public Boolean processed() {
    	return _processed;
    }
    
    public void setProcessed (Boolean processed) {
    	_processed = processed;
    }

    public void setMethodMeta(XmlStringWriter w) {

        assert _exMethodId != null;
        assert _stepPath != null;
        String namespace = _exMethodId + "_" + _stepPath;
        try {
            _methodMeta = XmlDoc.parse(w.document());
            if (_methodMeta != null && _methodMeta.elements() != null) {
                XmlStringWriter nw = new XmlStringWriter("meta");
                List<XmlElement> es = _methodMeta.elements();
                for (XmlElement e : es) {
                    String ns = e.value("@ns");
                    String[] attrs;
                    if (e.hasAttributes()) {
                        List<XmlAttribute> as = e.attributes();
                        int idx;
                        if (ns == null) {
                            attrs = new String[(as.size() + 1) * 2];
                            attrs[0] = "ns";
                            attrs[1] = namespace;
                            idx = 2;
                        } else {
                            attrs = new String[as.size() * 2];
                            idx = 0;
                        }
                        for (XmlAttribute xa : as) {
                            attrs[idx++] = xa.name();
                            if (xa.name().equals("ns")) {
                                attrs[idx++] = namespace;
                            } else {
                                attrs[idx++] = xa.value();
                            }
                        }
                    } else {
                        attrs = new String[] { "ns", namespace };
                    }
                    if (e.hasElements()) {
                        if (e.value() == null) {
                            nw.push(e.name(), attrs);
                        } else {
                            nw.push(e.name(), attrs, e.value());
                        }
                        nw.add(e, false);
                        nw.pop();
                    } else {
                        if (e.value() == null) {
                            nw.add(e.name(), attrs);
                        } else {
                            nw.add(e.name(), attrs, e.value());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
    }

    public XmlElement methodMetaForEdit() {

        return _methodMetaForEdit;
    }

    public void setMethodMetaForEdit(XmlElement methodMetaForEdit, boolean overwrite) {

        if (_methodMetaForEdit != null && !overwrite) {
            return;
        }
        _methodMetaForEdit = methodMetaForEdit;
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.study;
    }

    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        return new StudyCreate(po, this);
    }

    @Override
    public void createServiceArgs(XmlWriter w) {

        w.add("pid", _exMethodId);
        if (name() != null) {
            w.add("name", name());
        }
        if (description() != null) {
            w.add("description", description());
        }
        w.add("type", _studyType);
        if (_processed!=null) w.add("processed", _processed);
        //
        if (_stepPath != null) {
            w.add("step", _stepPath);
        }
        boolean hasMeta = hasMeta();
        boolean hasMethodMeta = hasMethodMeta();
        if (hasMeta || hasMethodMeta) {
            w.push("meta");
            if (hasMeta) {
                List<XmlElement> es = meta().elements();
                for (XmlElement e : es) {
                    w.add(e, true);
                }
            }
            if (hasMethodMeta) {
                // ns=<ex_method_id>_<step_path>
                List<XmlElement> es = _methodMeta.elements();
                for (XmlElement e : es) {
                    w.add(e, true);
                }
            }
            w.pop();
        }
    }

    public boolean hasMethodMeta() {

        if (_methodMeta != null) {
            if (_methodMeta.hasElements()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        return new StudyUpdate(this);
    }

    @Override
    public void updateServiceArgs(XmlWriter w) {

        w.add("id", id());
        if (name() != null) {
            w.add("name", name());
        }
        if (description() != null) {
            w.add("description", description());
        }
        if (_studyType != null) {
            w.add("type", _studyType);
        }
        if (_processed!=null) {
        	w.add("processed", _processed);
        }
        if (_stepPath != null) {
            w.push("method");
            w.add("id", _exMethodId);
            w.add("step", _stepPath);
            w.pop();
        }
        boolean hasMeta = hasMeta();
        boolean hasMethodMeta = hasMethodMeta();
        if (hasMeta || hasMethodMeta) {
            w.push("meta", new String[] { "action", "replace" });
            if (hasMeta) {
                List<XmlElement> es = meta().elements();
                for (XmlElement e : es) {
                    w.add(e, true);
                }
            }
            if (hasMethodMeta) {
                // ns=<ex_method_id>_<step_path>
                List<XmlElement> es = _methodMeta.elements();
                for (XmlElement e : es) {
                    w.add(e, true);
                }
            }
            w.pop();
        }
    }

    public static void setMetaForEdit(final Study study, final boolean overwrite, final ActionListener al) {

        new StudyMetadataDescribe(study).send(new ObjectMessageResponse<XmlElement>() {

            @Override
            public void responded(XmlElement xe) {

                if (xe == null) {
                    study.setMetaForEdit((XmlElement) null);
                    study.setMethodMetaForEdit(null, overwrite);
                } else {
                    study.setMetaForEdit(xe.element("meta"));
                    study.setMethodMetaForEdit(xe.element("method"), overwrite);
                }
                al.executed(true);
            }
        });
    }

}
