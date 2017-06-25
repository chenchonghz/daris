package daris.client.ui.collection.download;

import arc.gui.dialog.DialogProperties;
import arc.gui.dialog.DialogProperties.Type;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.collection.download.DownloaderSettings;
import daris.client.model.collection.messages.DownloaderGet;
import daris.client.model.object.DObjectRef;

public class DownloaderGetDialog {

    private DObjectRef _obj;
    private DownloaderSettings _settings;

    public DownloaderGetDialog(DObjectRef obj) {
        _obj = obj;
        _settings = new DownloaderSettings();
        _settings.addObject(_obj);
    }

    public void show(arc.gui.window.Window owner) {
        DialogProperties dp = new DialogProperties(Type.ACTION, "Get downloader app",
                new DownloaderSettingsForm(_obj, _settings));
        dp.setModal(true);
        dp.setSize(600, 500);
        dp.setButtonAction(new AsynchronousAction() {

            @Override
            public void execute(final ActionListener l) {
                new DownloaderGet(_settings).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        l.executed(true);
                    }
                });
            }
        });
        dp.setButtonLabel("Get");
        Dialog.postDialog(dp).show();
    }
}
