package daris.client.gui.dicom.action;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class DicomSendDialog {
    private Dialog<Void> _dlg;

    public DicomSendDialog() {
        _dlg = new Dialog<Void>();
        DialogPane dp = _dlg.getDialogPane();
        dp.setContent(new StackPane(new Text("test content")));
        dp.setMinSize(600, 380);
        dp.getButtonTypes().add(ButtonType.CANCEL);
        dp.getButtonTypes().add(ButtonType.OK);
        
        _dlg.setWidth(600);
        _dlg.setHeight(400);
        _dlg.show();
    }

}
