package daris.client.ui.object.action;

import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem.XmlType;
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
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.model.asset.task.AssetImportTask;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.DicomIngestTask;
import daris.client.model.task.ImportTask;
import daris.client.model.type.TypeStringEnum;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.form.LocalFileForm;
import daris.client.ui.widget.MessageBox;

public class DicomIngestForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    private DObjectRef _po;
    private ImportTask _task;

    private VerticalPanel _vp;
    private SimplePanel _interfaceSP;
    private Form _interfaceForm;
    private LocalFileForm _fileForm;
    private Label _statusLabel;

    public DicomIngestForm(DObjectRef po, List<LocalFile> files) {
        _po = po;
        _task = new DicomIngestTask(po, files);

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _interfaceSP = new SimplePanel();
        _interfaceSP.fitToParent();
        _vp.add(_interfaceSP);

        _interfaceForm = createInterfaceForm();
        _interfaceForm.render();
        addMustBeValid(_interfaceForm);
        _interfaceSP
                .setContent(new ScrollPanel(_interfaceForm, ScrollPolicy.AUTO));

        _fileForm = new LocalFileForm(LocalFile.Filter.ANY, true, files);
        _fileForm.setHeight(100);
        _fileForm.setWidth100();
        _fileForm.addChangeListener(new StateChangeListener() {
            @Override
            public void notifyOfChangeInState() {
                List<LocalFile> files = _fileForm.files();
                _task.setFiles(files);
            }
        });
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

    private Form createInterfaceForm() {

        Form form = new Form(FormEditMode.CREATE);

        Field<Boolean> anonymizeField = new Field<Boolean>(
                new FieldDefinition("anonymize", BooleanType.DEFAULT_TRUE_FALSE,
                        "Anonymize the patient name.", null, 1, 1));
        anonymizeField.setValue(true);
        form.add(anonymizeField);

        Field<String> anonymizeElementsField = new Field<String>(
                new FieldDefinition("anonymize-elements", StringType.DEFAULT,
                        "Indicates the DICOM elements to anonymize.", null, 0,
                        1));
        form.add(anonymizeElementsField);

        Field<String> engineField = new Field<String>(
                new FieldDefinition("engine", ConstantType.DEFAULT,
                        "The type of storage engine to use to process the data.",
                        null, 1, 1));
        engineField.setValue(DicomIngestTask.ENGINE_NIG_DICOM);
        form.add(engineField);

        FieldGroup argFieldGroup = new FieldGroup(
                new FieldDefinition("arg", DocType.DEFAULT,
                        "An argument to pass through to the selected engine.",
                        null, 0, Integer.MAX_VALUE));
        Field<String> argNameField = new Field<String>(
                new FieldDefinition("name", StringType.DEFAULT,
                        "The name of the argument.", null, 1, 1));
        argNameField.setXmlType(XmlType.ATTRIBUTE);
        argNameField.setValue(DicomIngestTask.NIG_DICOM_ID_CITABLE);
        argFieldGroup.add(argNameField);
        Field<String> argValueField = new Field<String>(
                new FieldDefinition(null, StringType.DEFAULT,
                        "The value of the argument.", null, 1, 1));
        argValueField.setValue(_po.id());
        argFieldGroup.add(argValueField);
        form.add(argFieldGroup);

        Field<String> typeField = new Field<String>(new FieldDefinition("type",
                new EnumerationType<String>(new TypeStringEnum()),
                "The MIME type of the input. If not specified, defined from the input stream itself.",
                null, 0, 1));
        form.add(typeField);

        Field<String> serviceField = new Field<String>(
                new FieldDefinition("service", StringType.DEFAULT,
                        "The name of a service to be called for each study that is created/updated.",
                        null, 0, 1));
        form.add(serviceField);

        Field<Boolean> waitField = new Field<Boolean>(
                new FieldDefinition("wait", BooleanType.DEFAULT_TRUE_FALSE,
                        "Indicates whether the caller of this service should block until the data is fully ingested. Defaults to true.",
                        null, 0, 1));
        waitField.setValue(true);
        form.add(waitField);

        return form;
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
                                MessageBox.display(MessageBox.Type.info,
                                        "DTI Task " + task.id(),
                                        "Task status: " + task.status()
                                                .toString().toLowerCase() + ".",
                                        1);
                            }
                            if (task.status() == DTITask.State.COMPLETED) {
                                MessageBox.display(MessageBox.Type.info,
                                        "DICOM ingest task",
                                        "DICOM data has been ingested successfully.",
                                        3);
                            } else if (task.status() == DTITask.State.FAILED) {
                                Dialog.warn("Error",
                                        "DICOM ingest task " + task.id()
                                                + " failed with following error: <br/>"
                                                + task.errorAndStack());
                            } else if (task
                                    .status() == DTITask.State.FAILED_RETRY) {
                                MessageBox.display(MessageBox.Type.warning,
                                        "Warning",
                                        "DICOM ingest task " + task.id()
                                                + " failed with following error:<br/>"
                                                + task.errorAndStack()
                                                + "<br/>Retrying...",
                                        3);
                            }
                        }
                    }
                });
            }

            @Override
            public void completed(AssetImportTask task) {
                l.executed(true);
                MessageBox.display(MessageBox.Type.info,
                        "DTI Task " + task.id(), "Task completed.", 3);

            }

            @Override
            public void failed() {
                l.executed(false);
                Dialog.inform("Error",
                        "Failed to create DTI task for DICOM ingest.");

            }
        });
    }
}
