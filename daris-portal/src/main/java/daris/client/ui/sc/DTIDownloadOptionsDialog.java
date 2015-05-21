package daris.client.ui.sc;

import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;

public abstract class DTIDownloadOptionsDialog {

    private DTIDownloadOptionsDialog() {
    }

    public static void show(Window owner, ActionListener al) {
        DTIDownloadOptionsForm form = new DTIDownloadOptionsForm();
        DialogProperties dp = new DialogProperties(DialogProperties.Type.INFORM, "DTI download options", form);
        dp.setActionEnabled(true);
        dp.setButtonLabel("Ok");
        dp.setModal(true);
        dp.setOwner(owner);
        dp.setSize(480, 200);
        Dialog dialog = Dialog.postDialog(dp, al);
        dialog.show();
    }

}
