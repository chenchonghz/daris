package daris.client.gui.object.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import javafx.stage.Window;

public class DownloadAction extends ActionInterface<DObject> {

    public static final String ACTION_NAME = "Download";
    private DObjectRef _o;

    public DownloadAction(Window w, DObjectRef o) {
        super(o.referentTypeName(), null, w, 0, 0);
        _o = o;
    }

    public DownloadAction(Window w, DObject o) {
        this(w, new DObjectRef(o));
    }

    @Override
    public void createInterface(final InterfaceCreateHandler ch) {
        ch.created(new DownloadForm(_o));
    }

    @Override
    public String actionName() {
        return "Download";
    }

}
