package daris.client.ui.subject;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.method.MethodEnum;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.DataUse;
import daris.client.model.project.Project;
import daris.client.model.subject.Subject;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.DObjectDetails;

public class SubjectDetails extends DObjectDetails {

    public static final String TAB_NAME_PUBLIC_METADATA = "Public Metadata";
    public static final String TAB_DESC_PUBLIC_METADATA = "Public Metadata";
    public static final String TAB_NAME_PRIVATE_METADATA = "Private Metadata";
    public static final String TAB_DESC_PRIVATE_METADATA = "Private Metadata";

    Form _privateMetaForm;

    Form _publicMetaForm;

    public SubjectDetails(DObjectRef po, Subject o, FormEditMode mode) {

        super(po, o, mode);

        updatePublicMetaTab();

        updatePrivateMetaTab();

        setActiveTab();
    }

    @Override
    protected void addToInterfaceForm(Form interfaceForm) {

        super.addToInterfaceForm(interfaceForm);

        final Subject so = (Subject) object();

        /*
         * virtual: if the subject is a virtual subject
         */
        Field<Boolean> virtualField = new Field<Boolean>(new FieldDefinition("virtual",
                mode() == FormEditMode.CREATE ? BooleanType.DEFAULT_TRUE_FALSE : ConstantType.DEFAULT,
                "If the subject is a virtual subject.", null, 1, 1));
        virtualField.setValue(so.virtual());
        if (mode() == FormEditMode.CREATE) {
            virtualField.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    so.setVirtual(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                }
            });
        }
        interfaceForm.add(virtualField);

        /*
         * method
         */
        if (mode() == FormEditMode.READ_ONLY || mode() == FormEditMode.UPDATE) {

            // We do not allow to change subject method at the moment.
            Field<MethodRef> methodField = new Field<MethodRef>(new FieldDefinition("method", ConstantType.DEFAULT,
                    "Specifies the Method to be used in creating this subject.", null, 1, 1));
            methodField.setValue(so.method());
            interfaceForm.add(methodField);

        } else {
            final Field<MethodRef> methodField = new Field<MethodRef>(new FieldDefinition("method",
                    new EnumerationType<MethodRef>(new MethodEnum(parentObject())), "Method", null, 1, 1));
            FieldRenderOptions fro = new FieldRenderOptions();
            fro.setWidth(500);
            methodField.setRenderOptions(fro);
            methodField.addListener(new FormItemListener<MethodRef>() {

                @Override
                public void itemValueChanged(FormItem<MethodRef> f) {

                    if (f.value() != null) {
                        so.setMethod(f.value());
                        Subject.setMetaForEdit(so, parentObject().id(), f.value().id(), new ActionListener() {

                            @Override
                            public void executed(boolean succeeded) {

                                if (succeeded) {
                                    updatePublicMetaTab();
                                    updatePrivateMetaTab();
                                }
                            }
                        });
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<MethodRef> f, FormItem.Property p) {

                }

            });
            interfaceForm.add(methodField);
            if (parentObject().needToResolve()) {
                parentObject().reset();
            }
            parentObject().resolve(new ObjectResolveHandler<DObject>() {
                @Override
                public void resolved(DObject o) {

                    if (o != null) {
                        Project po = (Project) o;
                        List<MethodRef> methods = po.methods();
                        if (methods != null) {
                            if (methods.size() == 1) {
                                methodField.setValue(methods.get(0));
                            }
                        }
                    }

                }
            });
        }

        /*
         * data-use
         */
        Field<DataUse> dataUseField = new Field<DataUse>(
                new FieldDefinition(
                        "data-use",
                        DataUse.asEnumerationType(),
                        "Specifies whether this Subject requires over-riding of the Project data-use specification.  Can only narrow (e.g. extended to specific). "
                                + " 1) 'specific' means use the data only for the original specific intent, "
                                + " 2) 'extended' means use the data for related projects and "
                                + " 3) 'unspecified' means use the data for any research", null, 0, 1));
        dataUseField.setValue(so.dataUse());
        if (mode() != FormEditMode.READ_ONLY) {
            dataUseField.addListener(new FormItemListener<DataUse>() {

                @Override
                public void itemValueChanged(FormItem<DataUse> f) {

                    so.setDataUse(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<DataUse> f, FormItem.Property p) {

                }
            });
        }
        interfaceForm.add(dataUseField);

        /*
         * fill-in
         */
        if (mode() == FormEditMode.CREATE) {
            Field<Boolean> fillInField = new Field<Boolean>(new FieldDefinition("fillin",
                    BooleanType.DEFAULT_TRUE_FALSE,
                    "Set to true to fill in the Subject allocator space (re-use allocated CIDs with no assets).", null,
                    0, 1));
            fillInField.setValue(so.fillIn(), false);

            fillInField.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    so.setFillIn(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                }
            });
            interfaceForm.add(fillInField);
        }
    }

    protected void allowIncompleteMetaChanged(boolean allowIncompleteMeta) {
        if (_publicMetaForm != null) {
            _publicMetaForm.setAllowMissingMandatory(allowIncompleteMeta);
        }
        if (_privateMetaForm != null) {
            _privateMetaForm.setAllowMissingMandatory(allowIncompleteMeta);
        }
        super.allowIncompleteMetaChanged(allowIncompleteMeta);
    }

    private void updatePublicMetaTab() {

        Subject so = (Subject) object();
        if ((mode() == FormEditMode.READ_ONLY && so.publicMeta() == null)
                || (mode() != FormEditMode.READ_ONLY && so.publicMetaForEdit() == null)) {
            removeTab(TAB_NAME_PUBLIC_METADATA);
            return;
        }

        if (mode() != FormEditMode.READ_ONLY) {
            if (_publicMetaForm != null) {
                removeMustBeValid(_publicMetaForm);
            }
            _publicMetaForm = XmlMetaForm.formFor(so.publicMetaForEdit(), mode());
            _publicMetaForm.setAllowMissingMandatory(so.allowIncompleteMeta());
            savePublicMeta(_publicMetaForm);
            _publicMetaForm.addListener(new FormListener() {

                @Override
                public void rendering(Form f) {

                }

                @Override
                public void rendered(Form f) {
                    BaseWidget.resized(f);
                }

                @Override
                public void formValuesUpdated(Form f) {

                    savePublicMeta(f);
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(_publicMetaForm);
        } else {
            _publicMetaForm = XmlMetaForm.formFor(so.publicMeta(), mode());
        }
        _publicMetaForm.render();
        setTab(TAB_NAME_PUBLIC_METADATA, TAB_DESC_PUBLIC_METADATA, new ScrollPanel(_publicMetaForm, ScrollPolicy.AUTO));
    }

    private void savePublicMeta(Form f) {
        XmlStringWriter w = new XmlStringWriter();
        w.push("public");
        f.save(w);
        w.pop();
        ((Subject) object()).setPublicMeta(w);
    }

    private void updatePrivateMetaTab() {

        Subject so = (Subject) object();
        if ((mode() == FormEditMode.READ_ONLY && so.privateMeta() == null)
                || (mode() != FormEditMode.READ_ONLY && so.privateMetaForEdit() == null)) {
            removeTab(TAB_NAME_PRIVATE_METADATA);
            return;
        }

        if (mode() != FormEditMode.READ_ONLY) {
            if (_privateMetaForm != null) {
                removeMustBeValid(_privateMetaForm);
            }
            _privateMetaForm = XmlMetaForm.formFor(so.privateMetaForEdit(), mode());
            _privateMetaForm.setAllowMissingMandatory(so.allowIncompleteMeta());
            savePrivateMeta(_privateMetaForm);
            _privateMetaForm.addListener(new FormListener() {

                @Override
                public void rendering(Form f) {

                }

                @Override
                public void rendered(Form f) {
                    BaseWidget.resized(f);
                }

                @Override
                public void formValuesUpdated(Form f) {

                    savePrivateMeta(f);
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(_privateMetaForm);
        } else {
            _privateMetaForm = XmlMetaForm.formFor(so.privateMeta(), mode());
        }
        _privateMetaForm.render();
        setTab(TAB_NAME_PRIVATE_METADATA, TAB_DESC_PRIVATE_METADATA, new ScrollPanel(_privateMetaForm,
                ScrollPolicy.AUTO));
    }

    private void savePrivateMeta(Form f) {
        XmlStringWriter w = new XmlStringWriter();
        w.push("private");
        f.save(w);
        w.pop();
        ((Subject) object()).setPrivateMeta(w);
    }

}
