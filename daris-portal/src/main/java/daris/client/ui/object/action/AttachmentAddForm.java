package daris.client.ui.object.action;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskCreateHandler;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.model.asset.task.AssetImportTask;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObjectRef;
import daris.client.model.task.AttachmentAddTask;
import daris.client.model.task.ImportTask;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.dti.file.LocalFileSelectTarget;
import daris.client.ui.form.LocalFileForm;
import daris.client.ui.widget.MessageBox;

public class AttachmentAddForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private DObjectRef _o;
    private ImportTask _task;

    private VerticalPanel _vp;
    private SimplePanel _interfaceSP;
    private Form _interfaceForm;
    private Field<String> _attachmentNameField;
    private LocalFileForm _fileForm;
    private Label _statusLabel;

    public AttachmentAddForm(DObjectRef o, List<LocalFile> files) {
        _o = o;
        _task = new AttachmentAddTask(o, files);

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _interfaceSP = new SimplePanel();
        _interfaceSP.fitToParent();
        _vp.add(_interfaceSP);

        _interfaceForm = createInterfaceForm();
        _interfaceForm.render();
        addMustBeValid(_interfaceForm);
        _interfaceSP.setContent(new ScrollPanel(_interfaceForm, ScrollPolicy.AUTO));

        _fileForm = new LocalFileForm(LocalFileSelectTarget.ANY, false, _task.files());
        _fileForm.setHeight(100);
        _fileForm.setWidth100();
        _fileForm.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                List<LocalFile> files = _fileForm.files();
                _task.setFiles(files);
                if (files != null && !files.isEmpty()) {
                    _attachmentNameField.setValue(files.get(0).name());
                }
            }
        });
        addMustBeValid(_fileForm);
        _vp.add(_fileForm);

        _statusLabel = new Label();
        _statusLabel.setHeight(20);
        _statusLabel.setWidth100();
        _statusLabel.setPaddingLeft(20);
        _statusLabel.setFontSize(12);
        _statusLabel.setFontWeight(FontWeight.BOLD);
        _statusLabel.setColour(RGB.RED);
        _vp.add(_statusLabel);

    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (!v.valid()) {
            _statusLabel.setText(v.reasonForIssue());
        } else {
            _statusLabel.setText(null);
        }
        return v;
    }

    private Form createInterfaceForm() {
        Form form = new Form(FormEditMode.CREATE);
        Field<String> objectIdField = new Field<String>(new FieldDefinition("id", ConstantType.DEFAULT,
                "The id of the " + _o.referentTypeName() + " to attach to.", null, 1, 1));
        objectIdField.setValue(_o.id());
        form.add(objectIdField);

        FieldGroup attachmentFieldGroup = new FieldGroup(new FieldDefinition("attachment", DocType.DEFAULT,
                "The attachment.", null, 1, 1));

        _attachmentNameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "Name of the attachment.", null, 1, 1));
        List<LocalFile> files = _task.files();
        if (files != null && !files.isEmpty()) {
            _attachmentNameField.setValue(files.get(0).name());
        }
        attachmentFieldGroup.add(_attachmentNameField);

        Field<String> attachmentDescriptionField = new Field<String>(new FieldDefinition("description",
                TextType.DEFAULT, "Description about the attachment.", null, 0, 1));
        attachmentFieldGroup.add(attachmentDescriptionField);

        form.add(attachmentFieldGroup);

        return form;
    }

    public void save(XmlWriter w) {
        w.push("args");
        _interfaceForm.save(w);
        w.pop();
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener l) {

        XmlStringWriter w = new XmlStringWriter();
        save(w);
        _task.setArgs(w.document());
        _task.execute(new DTITaskCreateHandler<AssetImportTask>() {

            @Override
            public void created(AssetImportTask task) {
                l.executed(true);
                new DTITaskDialog(task, _vp.window());
                task.monitor(1000, false, new DTITaskStatusHandler<DTITask>() {

                    @Override
                    public void status(Timer t, DTITask task) {
                        if (task != null) {
                            if (task.finished()) {
                                MessageBox.display(MessageBox.Type.info, "DTI Task " + task.id(), "Task status: "
                                        + task.status().toString().toLowerCase(), 1);
                            }
                            if (task.status() == DTITask.State.COMPLETED) {
                                MessageBox.display(MessageBox.Type.info, "Attach to " + _o.id(),
                                        "Attachement has been added.", 3);
                            }
                        }
                    }
                });
            }

            @Override
            public void completed(AssetImportTask task) {
                l.executed(true);
                MessageBox.display(MessageBox.Type.info, "DTI Task " + task.id(), "Task completed.", 3);

            }

            @Override
            public void failed() {
                l.executed(false);
                Dialog.inform("Error", "Failed to create DTI task for DICOM ingest");

            }
        });
    }
}
