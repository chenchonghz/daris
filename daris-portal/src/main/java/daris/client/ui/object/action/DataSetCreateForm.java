package daris.client.ui.object.action;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem.XmlType;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
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
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.LongType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.model.asset.task.AssetImportTask;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.IDUtil;
import daris.client.model.file.FileUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.Study;
import daris.client.model.task.ImportTask;
import daris.client.model.task.PrimaryDataSetCreateTask;
import daris.client.model.type.TypeStringEnum;
import daris.client.model.type.messages.TypesFromExt;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.dti.file.LocalFileSelectTarget;
import daris.client.ui.form.LocalFileForm;
import daris.client.ui.form.MetadataSetForm;
import daris.client.ui.widget.MessageBox;

public abstract class DataSetCreateForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private ImportTask _task;

    private VerticalPanel _vp;
    private Label _statusLabel;

    private TabPanel _tp;

    private int _interfaceTabId = 0;
    private SimplePanel _interfaceSP;
    private Form _interfaceForm;
    private Field<String> _ctypeField;
    private LocalFileForm _fileForm;

    private int _metadataTabId = 0;
    private MetadataSetForm _metadataForm;

    private DObjectRef _po;

    public DataSetCreateForm(ImportTask task, DObjectRef po) {
        _task = task;
        _po = po;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _tp = new TabPanel();
        _tp.fitToParent();
        _vp.add(_tp);

        _statusLabel = new Label();
        _statusLabel.setHeight(20);
        _statusLabel.setWidth100();
        _statusLabel.setPaddingLeft(20);
        _statusLabel.setFontSize(12);
        _statusLabel.setFontWeight(FontWeight.BOLD);
        _statusLabel.setColour(RGB.RED);
        _vp.add(_statusLabel);

        /*
         * Interface tab
         */
        VerticalSplitPanel interfaceVSP = new VerticalSplitPanel();
        interfaceVSP.fitToParent();

        _interfaceSP = new SimplePanel();
        _interfaceSP.fitToParent();
        interfaceVSP.add(_interfaceSP);

        _interfaceForm = createInterfaceForm();
        addInterfaceFormItems(_interfaceForm);
        _interfaceForm.render();
        addMustBeValid(_interfaceForm);
        _interfaceSP.setContent(new ScrollPanel(_interfaceForm, ScrollPolicy.AUTO));

        _fileForm = new LocalFileForm(LocalFileSelectTarget.ANY, false, _task.files());
        _fileForm.setWidth100();
        _fileForm.setPreferredHeight(0.35);
        _fileForm.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                _task.setFiles(_fileForm.files());
                updateCTypeByFileExtension();
            }
        });
        addMustBeValid(_fileForm);
        interfaceVSP.add(_fileForm);

        _interfaceTabId = _tp.addTab("Interface", null, interfaceVSP);
        _tp.setActiveTabById(_interfaceTabId);

        /*
         * Metadata tab
         */
        _metadataForm = new MetadataSetForm(null);
        _metadataTabId = _tp.addTab("Metadata", null, (BaseWidget) _metadataForm.gui());
    }

    protected DObjectRef parentObject() {
        return _po;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public ImportTask task() {
        return _task;
    }

    private void updateCTypeByFileExtension() {
        List<LocalFile> files = _task.files();
        if (files != null && files.size() == 1) {
            LocalFile f = files.get(0);
            if (f.isFile()) {
                String ext = FileUtil.getExtension(f);
                if (ext != null) {
                    new TypesFromExt(ext).send(new ObjectMessageResponse<List<String>>() {

                        @Override
                        public void responded(List<String> ctypes) {
                            if (ctypes != null && !ctypes.isEmpty() && _ctypeField != null) {
                                _ctypeField.setValue(ctypes.get(0));
                            }
                        }
                    });
                }
            }
        }
    }

    private Form createInterfaceForm() {
        Form form = new Form(FormEditMode.CREATE);

        FieldGroup pidFieldGroup = new FieldGroup(new FieldDefinition("pid", DocType.DEFAULT,
                "Identifier of the parent (study).", "Identifier of the parent (study).", 1, 1));
        Field<String> prouteField = new Field<String>(new FieldDefinition("proute", ConstantType.DEFAULT,
                "PRoute of the parent (study).", "PRoute of the parent (study).", 0, 1));
        prouteField.setXmlType(XmlType.ATTRIBUTE);
        prouteField.setValue(_po.proute());
        pidFieldGroup.add(prouteField);

        Field<String> pidField = new Field<String>(new FieldDefinition(null, ConstantType.DEFAULT,
                "Citeable id of the parent (study).", "Citeable id of the parent (study).", 1, 1));
        pidField.setValue(_po.id());
        pidFieldGroup.add(pidField);
        form.add(pidFieldGroup);

        /*
         * Field<String> nameField = new Field<String>(new
         * FieldDefinition("name", StringType.DEFAULT, "Name of the dataset.",
         * "Name of the dataset.", (_task instanceof PrimaryDataSetCreateTask) ?
         * 1 : 0, 1));
         */
        // Does not need to be mandatory for primary data sets
        Field<String> nameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "Name of the dataset.", "Name of the dataset.", 0, 1));

        form.add(nameField);

        Field<String> descriptionField = new Field<String>(new FieldDefinition("description", TextType.DEFAULT,
                "Description about the dataset", "Description about the dataset.", 0, 1));
        form.add(descriptionField);

        FieldGroup methodFieldGroup = new FieldGroup(new FieldDefinition("method", DocType.DEFAULT,
                "Details about the ex-method for which this acquisition was made.", null, 0, 1));
        final Field<String> methodIdField = new Field<String>(new FieldDefinition("id", ConstantType.DEFAULT,
                "The citeable id of the ex-method.", null, 1, 1));
        methodFieldGroup.add(methodIdField);
        final Field<String> methodStepField = new Field<String>(new FieldDefinition("step", ConstantType.DEFAULT,
                "The execution step within the ex-method.", null, 1, 1));
        methodFieldGroup.add(methodStepField);
        if (_po != null && _po.isStudy()) {
            if (_po.referent() != null) {
                if (((Study) _po.referent()).exMethodId() == null) {
                    _po.reset();
                }
            }
            _po.resolve(new ObjectResolveHandler<DObject>() {

                @Override
                public void resolved(DObject o) {
                    if (o != null) {
                        Study so = (Study) o;
                        methodIdField.setValue(so.exMethodId());
                        methodStepField.setValue(so.stepPath());
                    }
                }
            });
        }
        form.add(methodFieldGroup);

        Field<String> typeField = new Field<String>(new FieldDefinition("type", new EnumerationType<String>(
                new TypeStringEnum()), "MIME type of the dataset if different from the content.", null, 0, 1));
        form.add(typeField);

        _ctypeField = new Field<String>(new FieldDefinition("ctype", new EnumerationType<String>(new TypeStringEnum()),
                "Encapsulation MIME type of the content, if there is content.", null, 0, 1));
        form.add(_ctypeField);
        updateCTypeByFileExtension();

        Field<String> lctypeField = new Field<String>(new FieldDefinition("lctype", new EnumerationType<String>(
                new TypeStringEnum()), "Logical MIME type of the content, if there is content. ", null, 0, 1));
        form.add(lctypeField);

        /*
         * transform
         */
        FieldGroup transformFieldGroup = new FieldGroup(new FieldDefinition("transform", DocType.DEFAULT, null, null,
                0, 1));
        Field<String> midField = new Field<String>(new FieldDefinition("mid", ConstantType.DEFAULT,
                "The id of the method.", "The id of the method.", 0, 1));
        midField.setValue(IDUtil.getParentId(_po.id()));
        transformFieldGroup.add(midField);

        Field<Long> tuidField = new Field<Long>(new FieldDefinition("tuid", LongType.POSITIVE,
                "The unique id of the transform.", "The unique id of the transform.", 0, 1));
        transformFieldGroup.add(tuidField);

        FieldGroup softwareFieldGroup = new FieldGroup(new FieldDefinition("software", DocType.DEFAULT, null, null, 0,
                1));

        // software name
        Field<String> softwareNameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "The name of the software.", "The name of the software.", 1, 1));
        softwareFieldGroup.add(softwareNameField);

        // software version
        Field<String> softwareVersionField = new Field<String>(new FieldDefinition("version", StringType.DEFAULT,
                "The version of the software.", "The version of the software.", 0, 1));
        softwareFieldGroup.add(softwareVersionField);

        // software commands
        FieldGroup commandField = new FieldGroup(new FieldDefinition("command", DocType.DEFAULT,
                "The command used to perform the transform.", "The command used to perform the transform.", 0, 1));

        // command name
        Field<String> commandNameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "The name of the command.", "The name of the command.", 1, 1));
        commandField.add(commandNameField);

        // command arguments
        FieldGroup argumentFieldGroup = new FieldGroup(new FieldDefinition("argument", DocType.DEFAULT,
                "The argument for the command.", "The argument for the command.", 0, Integer.MAX_VALUE));

        // argument name
        Field<String> argumentNameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "The name of argument.", "The name of the argument.", 1, 1));
        argumentFieldGroup.add(argumentNameField);

        // argument value
        Field<String> argumentValueField = new Field<String>(new FieldDefinition("value", StringType.DEFAULT,
                "The value of argument.", "The value of the argument.", 1, 1));
        argumentFieldGroup.add(argumentValueField);
        commandField.add(argumentFieldGroup);
        softwareFieldGroup.add(commandField);
        transformFieldGroup.add(softwareFieldGroup);
        form.add(transformFieldGroup);

        return form;
    }

    protected abstract void addInterfaceFormItems(Form interfaceForm);

    public void save(XmlWriter w) {
        w.push("args");
        _interfaceForm.save(w);
        if (_fileForm.files() != null && _fileForm.files().size() == 1) {
            w.add("filename", _fileForm.file().name());
        }
        w.push("meta");
        _metadataForm.save(w);
        w.pop();
        w.pop();
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
                                MessageBox.display(MessageBox.Type.info, "Import",
                                        "Dataset has been imported successfully.", 3);
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
                Dialog.inform("Error", "Failed to create DTI task for imporing dataset.");

            }
        });
    }
}
