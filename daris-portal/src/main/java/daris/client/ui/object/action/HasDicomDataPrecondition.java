package daris.client.ui.object.action;

import java.util.List;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.dicom.messages.DicomDataSetCount;
import daris.client.model.object.DObjectRef;

public class HasDicomDataPrecondition implements ActionPrecondition {

    private DObjectRef _root;
    private String _where;

    public HasDicomDataPrecondition(DObjectRef root) {

        _root = root;
        _where = null;
    }

    public HasDicomDataPrecondition(String where) {

        _root = null;
        _where = where;
    }

    @Override
    public EvaluatePrecondition evaluate() {

        return EvaluatePrecondition.BEFORE_INTERACTION;
    }

    @Override
    public String description() {

        return "Checking if object " + _root.id() == null ? "" : _root.id() + " contains DICOM data sets.";
    }

    @Override
    public void execute(final ActionPreconditionListener l) {

        if (_root != null) {
            new DicomDataSetCount(_root).send(new ObjectMessageResponse<Integer>() {

                @Override
                public void responded(Integer count) {

                    if (count > 0) {
                        l.executed(ActionPreconditionOutcome.PASS,
                                "The object " + (_root.id() == null ? "" : _root.id()) + " contains " + count
                                        + " DICOM datasets.");
                    } else {
                        l.executed(ActionPreconditionOutcome.FAIL,
                                "The object " + (_root.id() == null ? "" : _root.id()) + " contains no DICOM datasets.");
                    }
                }
            });
        } else {
            XmlStringWriter w = new XmlStringWriter();
            w.add("where", "cid contained by (" + _where
                    + ") and type=dicom/series and mf-dicom-series has value and asset has content");
            w.add("action", "count");
            Session.execute("asset.query", new ServiceResponseHandler() {

                @Override
                public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                    int count = xe.intValue("value", 0);
                    if (count > 0) {
                        l.executed(ActionPreconditionOutcome.PASS, "Found " + count + " DICOM datasets.");
                    } else {
                        l.executed(ActionPreconditionOutcome.FAIL, "No DICOM datasets found.");
                    }
                }
            });
        }
    }

}
