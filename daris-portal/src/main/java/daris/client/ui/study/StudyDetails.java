package daris.client.ui.study;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import daris.client.model.exmethod.StepEnum;
import daris.client.model.exmethod.StepItem;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.Study;
import daris.client.model.study.StudyTypeEnumDataSource;
import daris.client.model.util.BooleanEnum;
import daris.client.ui.form.XmlMetaFormGroup;
import daris.client.ui.object.DObjectDetails;

public class StudyDetails extends DObjectDetails {

    public static final String TAB_NAME_METHOD_METADATA = "Method Metadata";
    public static final String TAB_DESC_METHOD_METADATA = "Method Metadata";
    private MethodMetaFormGroup _methodMetaFormGroup;
    private Field<String> _studyTypeField;
    private Field<StepItem> _methodStepField;
    private Field<BooleanEnum> _processedField;

    public StudyDetails(DObjectRef po, Study o, FormEditMode mode) {

        super(po, o, mode);

        updateMethodMetaTab();

        setActiveTab();
    }
    
    protected void allowIncompleteMetaChanged(boolean allowIncompleteMeta){
        if(_methodMetaFormGroup!=null){
            _methodMetaFormGroup.setAllowMissingMandatory(allowIncompleteMeta);
        }
    }

    private void updateMethodMetaTab() {

        final Study so = (Study) object();
        if (so.stepPath() == null) {
            return;
        }

        if (mode() == FormEditMode.READ_ONLY) {
            if (so.methodMeta() == null) {
                removeTab(TAB_NAME_METHOD_METADATA);
                return;
            }
            _methodMetaFormGroup = new MethodMetaFormGroup(so.methodMeta(), mode(), so.allowIncompleteMeta());
            setTab(TAB_NAME_METHOD_METADATA, TAB_DESC_METHOD_METADATA, new ScrollPanel(_methodMetaFormGroup.gui(),
                    ScrollPolicy.AUTO));
        } else {
            if (_methodMetaFormGroup != null) {
                removeMustBeValid(_methodMetaFormGroup);
            }
            Study.setMetaForEdit(so, mode() == FormEditMode.CREATE ? true : false, new ActionListener() {

                @Override
                public void executed(boolean succeeded) {
                    if (!succeeded || so.methodMetaForEdit() == null) {
                        removeTab(TAB_NAME_METHOD_METADATA);
                        return;
                    }
                    _methodMetaFormGroup = new MethodMetaFormGroup(so.methodMetaForEdit(), mode(), so
                            .allowIncompleteMeta());
                    addMustBeValid(_methodMetaFormGroup);
                    saveMethodMeta(_methodMetaFormGroup);
                    _methodMetaFormGroup.addChangeListener(new StateChangeListener() {

                        @Override
                        public void notifyOfChangeInState() {
                            saveMethodMeta(_methodMetaFormGroup);
                        }
                    });
                    setTab(TAB_NAME_METHOD_METADATA, TAB_DESC_METHOD_METADATA,
                            new ScrollPanel(_methodMetaFormGroup.gui(), ScrollPolicy.AUTO));
                }
            });
        }

    }

    private void saveMethodMeta(XmlMetaFormGroup f) {
        Study study = (Study) object();
        assert study.stepPath() != null;
        assert study.exMethodId() != null;

        XmlStringWriter w = new XmlStringWriter();
        if (mode().equals(FormEditMode.CREATE)) {
            w.push("meta");
        } else {
            w.push("meta", new String[] { "action", "replace" });
        }
        f.save(w);
        w.pop();
        study.setMethodMeta(w);
    }

    protected void addToInterfaceForm(Form interfaceForm) {

        super.addToInterfaceForm(interfaceForm);

        final Study so = (Study) object();
        /*
         * study type
         */
        if (FormEditMode.READ_ONLY == mode() || FormEditMode.UPDATE == mode()) {
            _studyTypeField = new Field<String>(new FieldDefinition("type", ConstantType.DEFAULT, "Study Type", null,
                    1, 1));
            _studyTypeField.setInitialValue(so.studyType());
        } else {
            _studyTypeField = new Field<String>(new FieldDefinition("type", new EnumerationType<String>(
                    new StudyTypeEnumDataSource(so.exMethodId())), "Study Type", null, 1, 1));
            _studyTypeField.setInitialValue(so.studyType());
            _studyTypeField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {

                    so.setStudyType(f.value());
                    if (_methodStepField != null) {
                        StepItem step = _methodStepField.value();
                        if (step != null && so.studyType() != null && ObjectUtil.equals(step.type(), so.studyType())) {
                            so.setStepPath(step.path());
                        } else {
                            so.setStepPath(null);
                        }
                    }
                    updateInterfaceTab();
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, FormItem.Property p) {

                }
            });
        }
        interfaceForm.add(_studyTypeField);

        /*
         * processed
         * 
         * If we want this to be editable, we would have to check that a Study
         * holds DataSets consistent with the new setting. For now it's not
         * editable.
         */
        if (FormEditMode.READ_ONLY == mode() || FormEditMode.UPDATE == mode()) {
            _processedField = new Field<BooleanEnum>(
                    new FieldDefinition(
                            "processed",
                            ConstantType.DEFAULT,
                            "Processed: is the Study intended to hold processed DataSets (true), non-processed DataSets (false) or unknown/mix (don't set).",
                            null, 0, 1));
            Boolean t = so.processed();
            if (t != null) {
                if (t) {
                    _processedField.setInitialValue(BooleanEnum.TRUE);
                } else {
                    _processedField.setInitialValue(BooleanEnum.FALSE);
                }
            }
        } else {
            _processedField = new Field<BooleanEnum>(
                    new FieldDefinition(
                            "processed",
                            BooleanEnum.asEnumerationType(),
                            "Processed: is the Study intended to hold processed DataSets (true), non-processed DataSets (false) or unknown/mix (don't set).",
                            null, 0, 1));
            Boolean t = so.processed();
            if (t != null) {
                if (t) {
                    _processedField.setInitialValue(BooleanEnum.TRUE);
                } else {
                    _processedField.setInitialValue(BooleanEnum.FALSE);
                }
            }
            _processedField.addListener(new FormItemListener<BooleanEnum>() {

                @Override
                public void itemValueChanged(FormItem<BooleanEnum> f) {
                    if (f.value() == BooleanEnum.TRUE) {
                        so.setProcessed(true);
                    } else if (f.value() == BooleanEnum.FALSE) {
                        so.setProcessed(false);
                    }
                    updateInterfaceTab();
                }

                @Override
                public void itemPropertyChanged(FormItem<BooleanEnum> f, FormItem.Property p) {

                }
            });
        }
        interfaceForm.add(_processedField);

        /*
         * method { id, step }
         */
        FieldGroup methodFieldGroup = new FieldGroup(new FieldDefinition("method", ConstantType.DEFAULT, "method",
                null, 1, 1));
        Field<String> methodIdField = new Field<String>(new FieldDefinition("id", ConstantType.DEFAULT, "id", null, 1,
                1));
        methodIdField.setValue(so.exMethodId());
        methodFieldGroup.add(methodIdField);

        if (FormEditMode.READ_ONLY == mode() || FormEditMode.UPDATE == mode()) {
            _methodStepField = new Field<StepItem>(
                    new FieldDefinition("step", ConstantType.DEFAULT, "step", null, 1, 1));
            _methodStepField.setInitialValue(StepItem.fromStudy(so));
        } else {
            _methodStepField = new Field<StepItem>(new FieldDefinition("step", new EnumerationType<StepItem>(
                    new StepEnum(so)), "step", null, 1, 1));
            _methodStepField.setInitialValue(StepItem.fromStudy(so));
            _methodStepField.addListener(new FormItemListener<StepItem>() {

                @Override
                public void itemValueChanged(FormItem<StepItem> f) {

                    so.setStepPath(f.value().path());
                    so.setStudyType(f.value().type());
                    updateInterfaceTab();
                    updateMethodMetaTab();
                }

                @Override
                public void itemPropertyChanged(FormItem<StepItem> f, FormItem.Property p) {

                }
            });
        }
        methodFieldGroup.add(_methodStepField);
        interfaceForm.add(methodFieldGroup);
    }

}
