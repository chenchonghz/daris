package daris.client.ui.dicom;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.FieldSet;
import arc.gui.form.FieldSetListener;
import arc.gui.form.FieldValidHandler;
import arc.gui.form.FieldValueValidator;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.Validity;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.object.BackgroundObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.DicomAEEnum;
import daris.client.model.dicom.LocalAESetRef;
import daris.client.model.dicom.LocalAETitleEnumDataSource;
import daris.client.model.dicom.messages.DicomAEList;
import daris.client.model.dicom.messages.DicomSend;
import daris.client.model.dicom.messages.DicomSend.ElementAction;
import daris.client.model.dicom.messages.DicomSend.GenericElement;
import daris.client.model.object.DObjectRef;

public class DicomSendForm extends ValidatedInterfaceComponent implements
        AsynchronousAction {
    private VerticalPanel _vp;
    private Form _form;
    private HTML _status;
    private DicomSend _ds;

    public DicomSendForm(DObjectRef parent) {
        this(new DicomSend(parent));
    }

    public DicomSendForm(String where) {
        this(new DicomSend(where));
    }

    private DicomSendForm(DicomSend ds) {
        _ds = ds;
        _vp = new VerticalPanel();
        _vp.fitToParent();
        _form = new Form(FormEditMode.CREATE);
        _form.fitToParent();
        if (_ds.pid() != null) {
            Field<String> rootIdField = new Field<String>(
                    new FieldDefinition(
                            "Root",
                            ConstantType.DEFAULT,
                            "The id of the root/parent object that contains DICOM datasets to send.",
                            null, 1, 1));
            rootIdField.setValue(_ds.pid(), false);
            _form.add(rootIdField);
        } else {
            Field<String> whereField = new Field<String>(
                    new FieldDefinition(
                            "Root Query",
                            TextType.DEFAULT,
                            "The query to find the root objects which may contain DICOM datasets to send.",
                            null, 1, 1));
            whereField.setValue(_ds.where(), false);
            whereField.setReadOnly();
            _form.add(whereField);
        }
        /*
         * local application entity
         */
        FieldGroup localAEFieldGroup = new FieldGroup(new FieldDefinition(
                "Local Application Entity", ConstantType.DEFAULT, null, null,
                1, 1));
        final Field<String> localAETField = new Field<String>(
                new FieldDefinition("AE Title", new EnumerationType<String>(
                        new LocalAETitleEnumDataSource()), null, null, 1, 1));
        localAETField.setRenderOptions(new FieldRenderOptions().setWidth(0.6));
        localAETField.addListener(new FormItemListener<String>() {
            @Override
            public void itemValueChanged(FormItem<String> f) {
                _ds.setLocalAET(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    Property property) {
            }
        });
        localAEFieldGroup.add(localAETField);
        _form.add(localAEFieldGroup);
        /*
         * remote application entity
         */
        FieldGroup remoteAEFieldGroup = new FieldGroup(new FieldDefinition(
                "Remote Application Entity", ConstantType.DEFAULT, null, null,
                1, 1));
        Field<DicomAE> remoteAESelectField = new Field<DicomAE>(
                new FieldDefinition("Select AE", new EnumerationType<DicomAE>(
                        new DicomAEEnum(DicomAEList.Type.REMOTE,
                                DicomAEList.Access.ALL)), null, null, 0, 1));
        remoteAESelectField.setRenderOptions(new FieldRenderOptions()
                .setWidth(0.6));
        remoteAEFieldGroup.add(remoteAESelectField);
        final Field<String> remoteAETField = new Field<String>(
                new FieldDefinition("AE Title", StringType.DEFAULT, null, null,
                        1, 1));
        remoteAETField.addListener(new FormItemListener<String>() {
            @Override
            public void itemValueChanged(FormItem<String> f) {
                _ds.setRemoteAET(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    Property property) {
            }
        });
        remoteAEFieldGroup.add(remoteAETField);
        final Field<String> remoteHostField = new Field<String>(
                new FieldDefinition("Host", StringType.DEFAULT, null, null, 1,
                        1));
        remoteHostField.addListener(new FormItemListener<String>() {
            @Override
            public void itemValueChanged(FormItem<String> f) {
                _ds.setRemoteHost(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    Property property) {
            }
        });
        remoteAEFieldGroup.add(remoteHostField);
        final Field<Integer> remotePortField = new Field<Integer>(
                new FieldDefinition("Port", new IntegerType(0, 65535), null,
                        null, 1, 1));
        remotePortField.addListener(new FormItemListener<Integer>() {
            @Override
            public void itemValueChanged(FormItem<Integer> f) {
                _ds.setRemotePort(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Integer> f,
                    Property property) {
            }
        });
        remoteAEFieldGroup.add(remotePortField);
        remoteAESelectField.addListener(new FormItemListener<DicomAE>() {
            @Override
            public void itemValueChanged(FormItem<DicomAE> f) {
                DicomAE ae = f.value();
                if (ae != null) {
                    remoteAETField.setValue(ae.aet());
                    remoteHostField.setValue(ae.host());
                    remotePortField.setValue(ae.port());
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<DicomAE> f,
                    Property property) {
            }
        });
        _form.add(remoteAEFieldGroup);
        /*
         * override metadata
         */
        FieldGroup overrideFieldGroup = new FieldGroup(new FieldDefinition(
                "Override", ConstantType.DEFAULT,
                "Override DICOM header elements.", null, 1, 1));
        // Patient Name
        FieldGroup patientNameFG = new FieldGroup(new FieldDefinition(
                "Patient Name", ConstantType.DEFAULT,
                "The patient name element(0010,0010).", null, 0, 1));
        Field<ElementAction> patientNameAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        patientNameAction.setValue(ElementAction.unchanged, false);
        patientNameFG.add(patientNameAction);
        Field<String> patientNameValue = new Field<String>(new FieldDefinition(
                "Value", StringType.DEFAULT, null, null, 0, 1));
        patientNameValue.setVisible(false);
        ;
        patientNameFG.add(patientNameValue);
        patientNameFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setPatientName(action, value);
            }
        });
        overrideFieldGroup.add(patientNameFG);
        // Patient Id
        FieldGroup patientIdFG = new FieldGroup(new FieldDefinition(
                "Patient ID", ConstantType.DEFAULT,
                "The patient id element(0010,0020).", null, 0, 1));
        Field<ElementAction> patientIdAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        patientIdAction.setValue(ElementAction.unchanged, false);
        patientIdFG.add(patientIdAction);
        Field<String> patientIdValue = new Field<String>(new FieldDefinition(
                "Value", StringType.DEFAULT, null, null, 0, 1));
        patientIdValue.setVisible(false);
        patientIdFG.add(patientIdValue);
        patientIdFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setPatientId(action, value);
            }
        });
        overrideFieldGroup.add(patientIdFG);
        // Study Id
        FieldGroup studyIdFG = new FieldGroup(new FieldDefinition("Study ID",
                ConstantType.DEFAULT, "The study id element(0020,0010).", null,
                0, 1));
        Field<ElementAction> studyIdAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        studyIdAction.setValue(ElementAction.unchanged, false);
        studyIdFG.add(studyIdAction);
        Field<String> studyIdValue = new Field<String>(new FieldDefinition(
                "Value", StringType.DEFAULT, null, null, 0, 1));
        studyIdValue.setVisible(false);
        studyIdFG.add(studyIdValue);
        studyIdFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setStudyId(action, value);
            }
        });
        overrideFieldGroup.add(studyIdFG);
        // Performing Physician Name
        FieldGroup performingPhysicianNameFG = new FieldGroup(
                new FieldDefinition("Performing Physician Name",
                        ConstantType.DEFAULT,
                        "The performing physician name element(0008,1050).",
                        null, 0, 1));
        Field<ElementAction> performingPhysicianNameAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        performingPhysicianNameAction.setValue(ElementAction.unchanged, false);
        performingPhysicianNameFG.add(performingPhysicianNameAction);
        Field<String> performingPhysicianNameValue = new Field<String>(
                new FieldDefinition("Value", StringType.DEFAULT, null, null, 0,
                        1));
        performingPhysicianNameValue.setVisible(false);
        performingPhysicianNameFG.add(performingPhysicianNameValue);
        performingPhysicianNameFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setPerformingPhysicianName(action, value);
            }
        });
        overrideFieldGroup.add(performingPhysicianNameFG);
        // Referring Physician Name
        FieldGroup referringPhysicianNameFG = new FieldGroup(
                new FieldDefinition("Referring Physician Name",
                        ConstantType.DEFAULT,
                        "The referring physician name element(0008,0090).",
                        null, 0, 1));
        Field<ElementAction> referringPhysicianNameAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        referringPhysicianNameAction.setValue(ElementAction.unchanged, false);
        referringPhysicianNameFG.add(referringPhysicianNameAction);
        Field<String> referringPhysicianNameValue = new Field<String>(
                new FieldDefinition("Value", StringType.DEFAULT, null, null, 0,
                        1));
        referringPhysicianNameValue.setVisible(false);
        referringPhysicianNameFG.add(referringPhysicianNameValue);
        referringPhysicianNameFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setReferringPhysicianName(action, value);
            }
        });
        overrideFieldGroup.add(referringPhysicianNameFG);
        // Referring Physician Phone
        FieldGroup referringPhysicianPhoneFG = new FieldGroup(
                new FieldDefinition("Referring Physician Phone",
                        ConstantType.DEFAULT,
                        "The referring physician phone element(0008,0094).",
                        null, 0, 1));
        Field<ElementAction> referringPhysicianPhoneAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        referringPhysicianPhoneAction.setValue(ElementAction.unchanged, false);
        referringPhysicianPhoneFG.add(referringPhysicianPhoneAction);
        Field<String> referringPhysicianPhoneValue = new Field<String>(
                new FieldDefinition("Value", StringType.DEFAULT, null, null, 0,
                        1));
        referringPhysicianPhoneValue.setVisible(false);
        referringPhysicianPhoneFG.add(referringPhysicianPhoneValue);
        referringPhysicianPhoneFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
                _ds.setReferringPhysicianPhone(action, value);
            }
        });
        overrideFieldGroup.add(referringPhysicianPhoneFG);
        // Generic Element
        FieldGroup genericElementFG = new FieldGroup(new FieldDefinition(
                "Generic Element", ConstantType.DEFAULT,
                "A generic DICOM element.", null, 0, 255));
        FieldValueValidator<String> validator = new FieldValueValidator<String>() {
            @Override
            public void validate(Field<String> f, FieldValidHandler vh) {
                String v = f.value();
                if (v != null && v.matches("[0-9a-fA-F]{4}")) {
                    vh.setValid();
                } else {
                    vh.setInvalid("Must be a 4 digit hexadecimal number.");
                }
            }
        };
        Field<String> geGroup = new Field<String>(new FieldDefinition("group",
                new StringType(4, 4, 4),
                "The group part of the DICOM element tag.", null, 1, 1));
        geGroup.addValueValidator(validator);
        genericElementFG.add(geGroup);
        Field<String> geElement = new Field<String>(new FieldDefinition(
                "element", new StringType(4, 4, 4),
                "The element part of the DICOM element tag.", null, 1, 1));
        geElement.addValueValidator(validator);
        genericElementFG.add(geElement);
        Field<ElementAction> geAction = new Field<ElementAction>(
                new FieldDefinition("Action",
                        new EnumerationType<ElementAction>(
                                ElementAction.values()), null, null, 0, 1));
        genericElementFG.add(geAction);
        Field<String> geValue = new Field<String>(new FieldDefinition("Value",
                StringType.DEFAULT, null, null, 0, 1));
        geValue.setVisible(false);
        genericElementFG.add(geValue);
        genericElementFG.addListener(new DicomElementChangeListener() {
            @Override
            public void elementChanged(ElementAction action, String value) {
            }
        });
        overrideFieldGroup.add(genericElementFG);
        overrideFieldGroup.addListener(new FieldSetListener() {
            @Override
            public void addedField(FieldSet s, FormItem f, int idx,
                    boolean lastUpdate) {
            }

            @Override
            public void removedField(FieldSet s, FormItem f, int idx,
                    boolean lastUpdate) {
            }

            @Override
            public void updatedFields(FieldSet s) {
            }

            @Override
            public void updatedFieldValue(FieldSet overrideFG, FormItem f) {
                List<FormItem> geItems = overrideFG.fields("Generic Element");
                if (geItems == null || geItems.isEmpty()) {
                    return;
                }
                List<DicomSend.GenericElement> ges = new ArrayList<DicomSend.GenericElement>();
                for (FormItem geItem : geItems) {
                    FieldSet geFG = (FieldSet) geItem;
                    FormItem groupItem = geFG.field("group");
                    String gggg = (String) groupItem.value();
                    FormItem elementItem = geFG.field("element");
                    String eeee = (String) elementItem.value();
                    FormItem actionItem = geFG.field("Action");
                    ElementAction action = (ElementAction) actionItem.value();
                    FormItem valueItem = geFG.field("Value");
                    String value = (String) valueItem.value();
                    if (gggg == null || eeee == null || action == null) {
                        continue;
                    }
                    if (action == ElementAction.set) {
                        if (value == null) {
                            continue;
                        }
                    }
                    int group = -1;
                    int element = -1;
                    try {
                        group = Integer.parseInt(gggg, 16);
                        element = Integer.parseInt(eeee, 16);
                    } catch (Throwable e) {
                        continue;
                    }
                    ges.add(new GenericElement(group, element, action, value));
                }
                if (!ges.isEmpty()) {
                    _ds.setGenericElements(ges);
                }
            }

            @Override
            public void updatedFieldState(FieldSet s, FormItem f,
                    Property property) {
            }
        });
        _form.add(overrideFieldGroup);
        addMustBeValid(_form);
        // _form.fitToParent();
        _form.setMarginLeft(20);
        _form.setMarginRight(20);
        _form.render();
        _vp.add(new ScrollPanel(_form, ScrollPolicy.AUTO));
        _status = new HTML();
        _status.setVerticalAlign(VerticalAlign.MIDDLE);
        _status.setFontSize(11);
        _status.setHeight(22);
        _status.setMarginLeft(20);
        _status.setColour(RGB.RED);
        _vp.add(_status);
        notifyOfChangeInState();
    }

    private static abstract class DicomElementChangeListener implements
            FieldSetListener {
        @Override
        public void addedField(FieldSet s, FormItem f, int idx,
                boolean lastUpdate) {
        }

        @Override
        public void removedField(FieldSet s, FormItem f, int idx,
                boolean lastUpdate) {
        }

        @Override
        public void updatedFields(FieldSet s) {
        }

        @Override
        public void updatedFieldValue(FieldSet s, FormItem f) {
            FormItem actionItem = s.field("Action");
            ElementAction action = (ElementAction) actionItem.value();
            FormItem valueItem = s.field("Value");
            String value = (String) valueItem.value();
            if (f == s.field("Action")) {
                if (action != ElementAction.set) {
                    ((Field<String>) valueItem).setValue(null);
                }
                valueItem.setVisible(action == ElementAction.set);
            }
            elementChanged(action, value);
        }

        @Override
        public void updatedFieldState(FieldSet s, FormItem f, Property property) {
        }

        public abstract void elementChanged(ElementAction action, String value);
    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (v.valid()) {
            _status.clear();
        } else {
            _status.setHTML(v.reasonForIssue());
        }
        return v;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener l) {
        _ds.send(new BackgroundObjectMessageResponse() {
            @Override
            public void responded(Long id) {
                l.executed(id != null);
                new DicomSendMonitorDialog(id, _ds, _form.window()).show();
            }
        });
    }
}
