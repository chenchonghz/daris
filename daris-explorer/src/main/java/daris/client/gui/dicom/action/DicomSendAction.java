package daris.client.gui.dicom.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import javafx.stage.Window;

public class DicomSendAction extends ActionInterface<DObject> {

    public static final String ACTION_NAME = "Download";
    private DObjectRef _o;

    public DicomSendAction(Window w, DObjectRef o) {
        super(o.referentTypeName() + " " + o.citeableId(), null, w, 580, 480);
        _o = o;
    }

    public DicomSendAction(Window w, DObject o) {
        this(w, new DObjectRef(o, true));
    }

    @Override
    public void createInterface(final InterfaceCreateHandler ch) {
        ch.created(new DicomSendForm(_o));
    }

    @Override
    public String actionName() {
        return "Send";
    }

}