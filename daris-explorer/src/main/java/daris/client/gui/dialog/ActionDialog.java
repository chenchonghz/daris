package daris.client.gui.dialog;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.StateChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;

public class ActionDialog {

    private ValidatedInterfaceComponent _vic;
    private AsynchronousAction _action;
    private Dialog<Void> _dlg;
    private Button _okButton;

    public ActionDialog(ValidatedInterfaceComponent vic,
            AsynchronousAction action, double width, double height) {
        _vic = vic;
        _action = action;
        _dlg = new Dialog<Void>();
        _dlg.setWidth(width);
        _dlg.setHeight(height);

        DialogPane dp = _dlg.getDialogPane();
        dp.setContent(new StackPane(_vic.gui()));
        dp.getButtonTypes().add(ButtonType.CANCEL);
        dp.getButtonTypes().add(ButtonType.OK);
        _okButton = (Button) _dlg.getDialogPane().lookupButton(ButtonType.OK);
        _okButton.setDisable(!(_vic.valid().valid()));
        _okButton.setOnAction(event -> {
            _okButton.setDisable(true);
            _action.execute(new ActionListener() {

                @Override
                public void executed(boolean executed) {
                    _okButton.setDisable(false);
                    if (executed) {
                        _dlg.close();
                    }
                }
            });
        });
        _vic.addChangeListener(new StateChangeListener() {
            @Override
            public void notifyOfChangeInState() {
                _okButton.setDisable(!(_vic.valid().valid()));
            }
        });
    }

    public void show() {
        _dlg.show();
    }

}
