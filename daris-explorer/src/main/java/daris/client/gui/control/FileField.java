package daris.client.gui.control;

import java.io.File;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class FileField extends HBox {

    private TextField _textField;
    private Button _selectButton;
    private ObjectProperty<File> _fileProperty;
    private boolean _selectFile;
    private File _initialDirectory;
    private Window _ownerWindow;

    public FileField(File file) {
        _fileProperty = new SimpleObjectProperty<File>(file);
        _textField.setMinWidth(250.0);
        _textField.setText(file.getAbsolutePath());
        _textField.setDisable(true);
        Bindings.bindBidirectional(_textField.textProperty(), _fileProperty,
                new StringConverter<File>() {

                    @Override
                    public String toString(File object) {
                        return object.getAbsolutePath();
                    }

                    @Override
                    public File fromString(String string) {
                        return new File(string);
                    }
                });
        getChildren().add(_textField);
        _selectButton = new Button("Select...");
        _selectButton.setOnAction(event -> {
            if (_selectFile) {
                FileChooser chooser = new FileChooser();
                if (_initialDirectory != null) {
                    chooser.setInitialDirectory(_initialDirectory);
                }
                chooser.setTitle("Select File");
                _fileProperty.set(chooser.showOpenDialog(_ownerWindow));
            } else {
                DirectoryChooser chooser = new DirectoryChooser();
                if (_initialDirectory != null) {
                    chooser.setInitialDirectory(_initialDirectory);
                }
                chooser.setTitle("Select directory");
                _fileProperty.set(chooser.showDialog(_ownerWindow));
            }
        });
        getChildren().add(_selectButton);
    }

    public FileField setSelectFile(boolean selectFile) {
        _selectFile = selectFile;
        return this;
    }

    public FileField setSelectDirectory(boolean selectDirectory) {
        _selectFile = !selectDirectory;
        return this;
    }

    public FileField setInitialDirectory(File initialDirectory) {
        _initialDirectory = initialDirectory;
        return this;
    }

    public FileField setOwnerWindow(Window ownerWindow) {
        _ownerWindow = ownerWindow;
        return this;
    }

    public FileField setFile(File file) {
        _fileProperty.set(file);
        return this;
    }

    public FileField setFile(String path) {
        return setFile(new File(path));
    }

    public ObjectProperty<File> fileProperty() {
        return _fileProperty;
    }
    
    public StringProperty textProperty(){
        return _textField.textProperty();
    }
}
