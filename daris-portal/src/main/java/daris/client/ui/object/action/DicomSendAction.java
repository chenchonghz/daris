package daris.client.ui.object.action;

import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.Query;
import daris.client.ui.dicom.DicomSendForm;

public class DicomSendAction extends ActionInterface<DObject> {

    private DObjectRef _root;
    private String _where;

    public DicomSendAction(DObjectRef root, Window owner) {

        this(root, owner, ActionIntefaceUtil.windowWidth(owner, 0.7), ActionIntefaceUtil.windowHeight(owner, 0.7));
    }

    public DicomSendAction(DObjectRef root, Window owner, int width, int height) {

        super(root.referentTypeName(), root, new Vector<ActionPrecondition>(), owner, width, height);
        _root = root;
        _where = null;
        preconditions().add(new CanSendDicomPrecondition(_root));
        preconditions().add(new HasDicomDataPrecondition(_root));
    }

    public DicomSendAction(String where, Window owner) {

        this(where, owner, ActionIntefaceUtil.windowWidth(owner, 0.7), ActionIntefaceUtil.windowHeight(owner, 0.7));
    }

    public DicomSendAction(Query query, Window owner) {
        this(query.filter().toString(), owner);
    }

    public DicomSendAction(String where, Window owner, int width, int height) {

        super(null, (DObjectRef) null, new Vector<ActionPrecondition>(), owner, width, height);
        _root = null;
        _where = where;
        preconditions().add(new HasDicomDataPrecondition(_where));
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {

        ch.created(_root != null ? new DicomSendForm(_root) : new DicomSendForm(_where));
    }

    @Override
    public String actionName() {

        return "DICOM Send";
    }

    @Override
    public String title() {
        if (_root != null) {
            return "DICOM Send " + _root.id();
        } else {
            return "DICOM Send";
        }
    }

}
