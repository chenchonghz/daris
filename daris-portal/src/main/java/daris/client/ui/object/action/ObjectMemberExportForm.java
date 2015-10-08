package daris.client.ui.object.action;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.window.Window;
import arc.mf.client.Output;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.EnumerationType;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.RepositoryRef;

public class ObjectMemberExportForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    private DObjectRef _parent;
    private Form _form;
    private Field<DObject.Type> _ctype;
    private Field<Boolean> _toXML;
    private Field<Boolean> _toCSV;

    private static DObject.Type[] childTypesOf(DObjectRef parent) {
        if (parent == null || parent.id() == null) {
            return new DObject.Type[] { DObject.Type.project,
                    DObject.Type.subject, DObject.Type.study,
                    DObject.Type.dataset };
        }
        if (parent.isProject()) {
            return new DObject.Type[] { DObject.Type.subject,
                    DObject.Type.study, DObject.Type.dataset };
        }
        if (parent.isSubject() || parent.isExMethod()) {
            return new DObject.Type[] { DObject.Type.study,
                    DObject.Type.dataset };
        }
        if (parent.isStudy()) {
            return new DObject.Type[] { DObject.Type.study,
                    DObject.Type.dataset };
        }
        return new DObject.Type[] { DObject.Type.dataset };
    }

    public ObjectMemberExportForm(DObjectRef parent) {
        _parent = parent;
        _form = new Form(FormEditMode.CREATE);
        _form.setShowLabels(true);
        _form.setShowHelp(false);

        DObject.Type[] ctypes = childTypesOf(parent);
        _ctype = new Field<DObject.Type>(new FieldDefinition(
                "Select type of object to export", "ctype",
                new EnumerationType<DObject.Type>(ctypes), null, null, 1, 1));
        _ctype.setInitialValue(ctypes[0]);
        _form.add(_ctype);
        FieldGroup fg = new FieldGroup(new FieldDefinition(
                "Select output format", "format", DocType.DEFAULT, null, null,
                1, 1));
        _toXML = new Field<Boolean>(new FieldDefinition(
                "XML format (.xml file)", "xml",
                BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
        _toXML.setInitialValue(true);
        _toXML.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _toCSV.setValue(!f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        fg.add(_toXML);
        _toCSV = new Field<Boolean>(new FieldDefinition(
                "CSV format (.csv file)", "csv",
                BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
        _toCSV.setInitialValue(false);
        _toCSV.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _toXML.setValue(!f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        fg.add(_toCSV);
        _form.add(fg);
        _form.setMarginTop(25);
        _form.setMarginLeft(25);
        _form.render();
        addMustBeValid(_form);
    }

    @Override
    public Widget gui() {
        return _form;
    }

    @Override
    public void execute(final ActionListener l) {
        StringBuilder sb = new StringBuilder();
        if (_parent != null && _parent.id() != null) {
            sb.append("cid starts with '");
            sb.append(_parent.id());
            sb.append("' and ");
        }
        sb.append("model='om.pssd." + _ctype.value().toString() + "'");
        XmlStringWriter w = new XmlStringWriter();
        w.add("where", sb.toString());
        w.add("size", "infinity");
        w.add("action", "get-value");
        w.add("output-format", _toXML.value() ? "xml" : "csv");
        w.add("xpath", new String[] { "ename", "type" },
                "meta/daris:pssd-object/type");
        w.add("xpath", new String[] { "ename", "id" }, "cid");
        w.add("xpath", new String[] { "ename", "name" },
                "meta/daris:pssd-object/name");
        w.add("xpath", new String[] { "ename", "description" },
                "meta/daris:pssd-object/description");

        Session.execute("asset.query", w.document(), 1,
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        if (outputs != null && !outputs.isEmpty()) {
                            System.out.println(outputs.size());
                            StringBuilder sb = new StringBuilder();
                            if (_parent == null
                                    || _parent instanceof RepositoryRef) {
                                sb.append("daris-projects");
                            } else {
                                String ptype = _parent.referentTypeName();
                                String ctype = IDUtil.childTypeFromId(
                                        _parent.id()).toString();
                                sb.append("daris-");
                                if (ctype.equals("study")) {
                                    sb.append("studies");
                                } else {
                                    sb.append(ctype);
                                    sb.append("s");
                                }
                                sb.append("-in-");
                                sb.append(ptype);
                                sb.append("-");
                                sb.append(_parent.id());
                            }
                            sb.append(".");
                            sb.append(_toXML.value() ? "xml" : "csv");
                            outputs.get(0).download(sb.toString());
                        }
                    }
                });
        if (l != null) {
            l.executed(true);
        }
    }

    public void show(Window owner, ActionListener al) {
        StringBuilder sb = new StringBuilder();
        sb.append("Export member list");
        if (_parent != null && _parent.id() != null) {
            sb.append(" from ");
            sb.append(_parent.referentTypeName());
            sb.append(" ");
            sb.append(_parent.id());
        }
        DialogProperties dp = new DialogProperties(
                DialogProperties.Type.ACTION, sb.toString(), this);
        dp.setActionEnabled(false);
        dp.setButtonAction(this);
        dp.setCancelLabel("Cancel");
        dp.setButtonLabel("Export");
        dp.setOwner(owner);
        dp.setModal(true);
        dp.setScrollPolicy(ScrollPolicy.AUTO);
        dp.setSize(380, 200);
        Dialog dlg = Dialog.postDialog(dp, al);
        dlg.show();
    }

}
