package daris.client.gui.dicom.action;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.DocType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.StringType;
import daris.client.gui.form.Form;
import daris.client.gui.form.Form.FormTreeItem;
import daris.client.gui.form.FormItem;
import daris.client.model.dicom.messages.DicomSend;
import daris.client.model.object.DObjectRef;
import javafx.scene.Node;

@SuppressWarnings("rawtypes")
public class DicomSendForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    private DicomSend _msg;
    // private GridPane _grid;
    private Form _form;
    private DObjectRef _o;

    @SuppressWarnings("unchecked")
    public DicomSendForm(DObjectRef o) {
        _o = o;
        _form = new Form();
        FormItem callingAE = new FormItem(DocType.DEFAULT, "calling-ae",
                "Calling Application Entity", "Calling application entity", 1,
                1);

        FormItem<String> callingAETitle = new FormItem<String>(
                StringType.DEFAULT, "title", "Title", "Calling AE title", 1, 1);
        callingAETitle.setValue("TEST123");
        callingAE.add(callingAETitle);
        
        _form.add(callingAE);

        FormItem calledAE = new FormItem(DocType.DEFAULT, "called-ae",
                "Called Application Entity", "Called application entity", 1, 1);
        
        FormItem<String> calledAETitle = new FormItem<String>(
                StringType.DEFAULT, "title", "Title", "Called AE title", 1, 1);
        calledAETitle.setValue("WL");
        calledAE.add(calledAETitle);
        
        FormItem<String> calledAEHost= new FormItem<String>(
                StringType.DEFAULT, "host", "Host", "Called AE host", 1, 1);
        calledAE.add(calledAEHost);
        FormItem<Integer> calledAEPort = new FormItem<Integer>(
                IntegerType.POSITIVE_ONE, "port", "Port", "Called AE port", 1,
                1);
        calledAE.add(calledAEPort);
        
        
        _form.add(calledAE);
        
        _form.render();

        // _msg = new DicomSend(cid);
        //
        // _grid = new GridPane();
        // _grid.setAlignment(Pos.CENTER);
        // _grid.setHgap(5);
        // _grid.setVgap(5);
        // _grid.setPadding(new Insets(5, 5, 5, 5));
        // _grid.setBorder(new Border(new BorderStroke[] {
        // new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID,
        // new CornerRadii(5), BorderWidths.DEFAULT) }));
        // ColumnConstraints cc = new ColumnConstraints();
        // cc.setHalignment(HPos.RIGHT);
        // _grid.getColumnConstraints().add(cc);
        // cc = new ColumnConstraints();
        // cc.setHalignment(HPos.LEFT);
        // _grid.getColumnConstraints().add(cc);
        //
        // int rowIndex = 0;
        //
        // /*
        // * Calling AE Title
        // */
        // Label calledAETLabel = new Label("Calling AE Title");
        // ComboBox<String> calledAETCombo = new ComboBox<String>();
        // calledAETCombo.setEditable(true);
        // // TODO:
        // // calledAETCombo.getItems().setAll(xxx);
        // // calledAETCombo.getSelectionModel().select(xxx);
        // calledAETCombo.getSelectionModel().selectedItemProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // // TODO
        // });
        // calledAETCombo.getEditor().textProperty().addListener((obs, ov,
        // nv)->{
        // // TODO:
        // });
        // _grid.addRow(rowIndex++, calledAETLabel, calledAETCombo);
        //
        // /*
        // * Called AE Title
        // */
        // Label includeAttachmentsLabel = new Label("Include attachments");
        // CheckBox includeAttachmentsCheckBox = new CheckBox();
        // includeAttachmentsCheckBox.setAllowIndeterminate(false);
        // includeAttachmentsCheckBox.setSelected(_options.includeAttachments());
        // includeAttachmentsCheckBox.selectedProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // _options.setIncludeAttachments(newValue);
        // });
        // _grid.addRow(rowIndex++, includeAttachmentsLabel,
        // includeAttachmentsCheckBox);
        //
        // /*
        // * recursive?
        // */
        // if (_obj.isDataSet()) {
        // _options.setRecursive(false);
        // } else {
        // Label recursiveLabel = new Label("Recursive:");
        // CheckBox recursiveCheckBox = new CheckBox();
        // recursiveCheckBox.setAllowIndeterminate(false);
        // recursiveCheckBox.setSelected(_options.recursive());
        // recursiveCheckBox.selectedProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // _options.setRecursive(newValue);
        // });
        // _grid.addRow(rowIndex++, recursiveLabel, recursiveCheckBox);
        // }
        //
        // /*
        // * transcodes?
        // */
        // if (_availableTranscodes != null && !_availableTranscodes.isEmpty())
        // {
        // for (String from : _availableTranscodes.keySet()) {
        // Label transcodeLabel = new Label(
        // "Transcode from: " + from + " to:");
        // ComboBox<String> transcodeCombo = new ComboBox<String>();
        // transcodeCombo.getItems().add("none");
        // transcodeCombo.getItems()
        // .addAll(_availableTranscodes.get(from));
        // transcodeCombo.getSelectionModel().select(0);
        // transcodeCombo.getSelectionModel().selectedItemProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // if ("none".equals(newValue)) {
        // _options.removeTranscode(from);
        // } else {
        // _options.addTranscode(from, newValue);
        // }
        // });
        // _grid.addRow(rowIndex++, transcodeLabel, transcodeCombo);
        // }
        // }
        //
        // /*
        // * decompress?
        // */
        // Label decompressLabel = new Label("Decompress:");
        // CheckBox decompressCheckBox = new CheckBox();
        // decompressCheckBox.setAllowIndeterminate(false);
        // decompressCheckBox.setSelected(_options.decompress());
        // decompressCheckBox.selectedProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // _options.setDecompress(newValue);
        // });
        // _grid.addRow(rowIndex++, decompressLabel, decompressCheckBox);
        //
        // /*
        // * overwrite
        // */
        // Label overwriteLabel = new Label("Overwrite");
        // CheckBox overwriteCheckBox = new CheckBox();
        // overwriteCheckBox.setAllowIndeterminate(false);
        // overwriteCheckBox.setSelected(_options.overwrite());
        // overwriteCheckBox.selectedProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // _options.setOverwrite(newValue);
        // });
        // _grid.addRow(rowIndex++, overwriteLabel, overwriteCheckBox);
        //
        // Label directoryLabel = new Label("Download to directory:");
        // HBox directoryHBox = new HBox();
        // final TextField directoryTextField = new TextField();
        // directoryTextField.setMinWidth(250.0);
        // directoryTextField.setText(DownloadSettings.getDefaultDirectory());
        // directoryTextField.setDisable(true);
        // directoryTextField.textProperty()
        // .addListener((observable, oldValue, newValue) -> {
        // _options.setDirectory(newValue);
        // });
        // directoryHBox.getChildren().add(directoryTextField);
        // Button directoryChooserButton = new Button("Change...");
        // directoryChooserButton.setOnAction(event -> {
        // DirectoryChooser dirChooser = new DirectoryChooser();
        // dirChooser.setInitialDirectory(
        // new File(DownloadSettings.getDefaultDirectory()));
        // dirChooser.setTitle("Select download directory");
        // File dir = dirChooser.showDialog(gui().getScene().getWindow());
        // if (dir != null) {
        // directoryTextField.setText(dir.getAbsolutePath());
        // }
        // });
        // directoryHBox.getChildren().add(directoryChooserButton);
        // _grid.addRow(rowIndex++, directoryLabel, directoryHBox);

    }

    @Override
    public Node gui() {
        return _form.gui();
    }

    @Override
    public void execute(ActionListener al) {
        // new DownloadTask(_obj, _options).start();
        // al.executed(true);
    }
}