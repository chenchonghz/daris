package daris.client.ui.form;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

import com.google.gwt.user.client.ui.Widget;

public abstract class XmlMetaFormGroup extends ValidatedInterfaceComponent {

    private FormEditMode _mode;

    private BaseWidget _w;
    private Map<String, Form> _forms;

    public XmlMetaFormGroup(XmlElement xe, FormEditMode mode, boolean allowMissingMandatory) {
        _mode = mode;
        List<XmlElement> es = xe.elements();
        _forms = new TreeMap<String, Form>();
        for (XmlElement e : es) {
            String group = group(e);
            Form form = _forms.get(group);
            if (form == null) {
                form = new Form(_mode);
                form.setAllowMissingMandatory(allowMissingMandatory);
                _forms.put(group, form);
                addMustBeValid(form);
            }
            XmlMetaForm.addDoc(form, e, _mode);
        }
    }
    
    public void setAllowMissingMandatory(boolean allowMissingMandatory){
        if(_forms!=null){
            for(Form f : _forms.values()){
                f.setAllowMissingMandatory(allowMissingMandatory);
            }
        }
    }

    @Override
    public Widget gui() {
        if (_w == null) {
            if (_forms.size() == 1) {
                Form f = _forms.values().iterator().next();
                f.render();
                _w = f;
            } else {
                TabPanel tp = new TabPanel();
                tp.fitToParent();
                for (String group : _forms.keySet()) {
                    Form form = _forms.get(group);
                    form.render();
                    tp.addTab(group, null, new ScrollPanel(form, ScrollPolicy.AUTO));
                }
                tp.setActiveTab(0);
                _w = tp;
            }
        }
        return _w;
    }

    public FormEditMode mode() {
        return _mode;
    }

    protected abstract String group(XmlElement e);

    public void save(XmlWriter w) {
        for (Form form : _forms.values()) {
            form.save(w);
        }
    }
}
