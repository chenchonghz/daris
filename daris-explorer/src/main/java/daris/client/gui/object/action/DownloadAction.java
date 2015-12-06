package daris.client.gui.object.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CollectionTranscodeList;
import javafx.stage.Window;

public class DownloadAction extends ActionInterface<DObject> {

    public static final String ACTION_NAME = "Download";
    private DObjectRef _o;

    public DownloadAction(Window w, DObjectRef o) {
        super(o.referentTypeName(), null, w, 600, 320);
        _o = o;
    }

    public DownloadAction(Window w, DObject o) {
        this(w, new DObjectRef(o, true));
    }

    @Override
    public void createInterface(final InterfaceCreateHandler ch) {
        new CollectionTranscodeList(_o).send(transcodes->{
            ch.created(new DownloadForm(_o, transcodes));    
        });
    }

    @Override
    public String actionName() {
        return "Download";
    }

}
