package daris.client.ui.query.action;

import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.util.Action;
import daris.client.model.query.messages.QueryResultExport;

public class ResultExportAction implements Action {
    private QueryResultExport _qre;
    private arc.gui.window.Window _owner;

    public ResultExportAction(QueryResultExport qre, arc.gui.window.Window ownerWindow) {
        _qre = qre;
        _owner = ownerWindow;
    }

    @Override
    public void execute() {
        ResultExportForm form = new ResultExportForm(_qre);
        DialogProperties dp = new DialogProperties(DialogProperties.Type.ACTION, "Export query results as "
                + _qre.outputFormat() + "...", form);
        dp.setButtonAction(form);
        dp.setButtonLabel("Export");
        dp.setOwner(_owner);
        dp.setSize(320, 180);
        dp.setActionEnabled(form.valid().valid());
        Dialog dialog = Dialog.postDialog(dp);
        dialog.show();
    }

}
