package daris.client.gui.object.action;

import java.io.File;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.DownloadCollisionPolicy;
import daris.client.model.task.DownloadOptions;
import daris.client.model.task.DownloadTask;
import daris.client.settings.DownloadSettings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

public class DownloadForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    private DObjectRef _obj;
    private GridPane _grid;
    private CheckBox _recursiveCheckBox;
    private CheckBox _decompressCheckBox;
    private ComboBox<DownloadCollisionPolicy> _collisionPolicyComboBox;
    private TextField _directoryTextField;
    private Button _directoryChooserButton;

    public DownloadForm(DObjectRef obj) {
        _obj = obj;
        _grid = new GridPane();
        _grid.setAlignment(Pos.CENTER);
        _grid.setHgap(5);
        _grid.setVgap(5);
        _grid.setPadding(new Insets(5, 5, 5, 5));
        _grid.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT) }));
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        _grid.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        _grid.getColumnConstraints().add(cc);

        int rowIndex = 0;
        Label recursiveLabel = new Label("Recursive:");
        _recursiveCheckBox = new CheckBox();
        _recursiveCheckBox.setAllowIndeterminate(false);
        _recursiveCheckBox.setSelected(_obj.isDataSet() ? false : true);
        _recursiveCheckBox.setDisable(_obj.isDataSet());
        _grid.addRow(rowIndex++, recursiveLabel, _recursiveCheckBox);

        Label decompressLabel = new Label("Decompress:");
        _decompressCheckBox = new CheckBox();
        _decompressCheckBox.setAllowIndeterminate(false);
        _decompressCheckBox.setSelected(true);
        _grid.addRow(rowIndex++, decompressLabel, _decompressCheckBox);

        Label collisionPolicyLabel = new Label("If file exists?");
        _collisionPolicyComboBox = new ComboBox<DownloadCollisionPolicy>();
        _collisionPolicyComboBox.getItems()
                .addAll(DownloadCollisionPolicy.values());
        _collisionPolicyComboBox.setValue(DownloadCollisionPolicy.OVERWRITE);
        _grid.addRow(rowIndex++, collisionPolicyLabel,
                _collisionPolicyComboBox);

        Label directoryLabel = new Label("Download to directory:");
        HBox directoryHBox = new HBox();
        _directoryTextField = new TextField();
        _directoryTextField.setMinWidth(200.0);
        _directoryTextField.setText(DownloadSettings.getDefaultDirectory());
        _directoryTextField.setDisable(true);
        directoryHBox.getChildren().add(_directoryTextField);
        _directoryChooserButton = new Button("Change...");
        _directoryChooserButton.setOnAction(event -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setInitialDirectory(
                    new File(DownloadSettings.getDefaultDirectory()));
            dirChooser.setTitle("Select download directory");
            File dir = dirChooser.showDialog(gui().getScene().getWindow());
            if (dir != null) {
                _directoryTextField.setText(dir.getAbsolutePath());
            }
        });
        directoryHBox.getChildren().add(_directoryChooserButton);
        _grid.addRow(rowIndex++, directoryLabel, directoryHBox);

    }

    @Override
    public Node gui() {
        return _grid;
    }

    @Override
    public void execute(ActionListener al) {
        DownloadOptions options = new DownloadOptions();
        options.setRecursive(_recursiveCheckBox.isSelected());
        options.setDecompress(_decompressCheckBox.isSelected());
        options.setCollisionPolicy(_collisionPolicyComboBox.getValue());
        options.setDirectory(_directoryTextField.getText());
        new DownloadTask(_obj, options).start();
        al.executed(true);
    }

}
