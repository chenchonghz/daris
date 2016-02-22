package daris.client.ui.object.action;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.Validity;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.collection.messages.CollectionArchiveCreate;
import daris.client.model.object.DObjectRef;
import daris.client.ui.collection.CollectionArchiveOptionsForm;

public class DObjectDownloadForm extends CollectionArchiveOptionsForm
        implements AsynchronousAction {

    private VerticalPanel _vp;
    private HTML _status;

    public DObjectDownloadForm(DObjectRef obj) {
        super(obj);

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _vp.add(super.gui());

        _status = new HTML();
        _status.setWidth100();
        _status.setHeight(22);
        _status.setPaddingLeft(15);
        _status.setColour(RGB.RED);
        _vp.add(_status);
    }

    @Override
    public Validity valid() {
        Validity validity = super.valid();
        if (!validity.valid()) {
            _status.setHTML(validity.reasonForIssue());
            return validity;
        }
        if (validity.valid()) {
            _status.clear();
        } else {
            _status.setHTML(validity.reasonForIssue());
        }
        return validity;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener al) {
        CollectionArchiveCreate msg = new CollectionArchiveCreate(object(),
                archiveOptions());
        msg.send(new ObjectMessageResponse<Null>() {

            @Override
            public void responded(Null r) {
                al.executed(true);
            }
        });
    }

    public void showDialog(Window owner) {
        DialogProperties dp = new DialogProperties(DialogProperties.Type.ACTION,
                "Download " + object().referentTypeName() + " " + object().id(),
                this);
        dp.setButtonAction(this);
        dp.setButtonLabel("Download");
        dp.setActionEnabled(true);
        dp.setModal(true);
        dp.setOwner(owner);
        dp.setCancelLabel("Cancel");
        dp.setSize(640, 280);
        Dialog.postDialog(dp).show();
    }

}