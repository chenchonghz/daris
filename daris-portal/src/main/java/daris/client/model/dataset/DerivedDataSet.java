package daris.client.model.dataset;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import daris.client.model.dataset.messages.DerivationDataSetCreate;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class DerivedDataSet extends DataSet {

    public static class Input {
        private String _id;
        private String _vid;

        public Input(String id, String vid) {

            _id = id;
            _vid = vid;
        }

        public String id() {

            return _id;
        }

        public String vid() {

            return _vid;
        }
    }

    private List<Input> _inputs;

    private String _methodId;

    private String _methodStep;

    private boolean _processed;
    
    private boolean _anonymized;

    public DerivedDataSet(XmlElement dde) throws Throwable {

        super(dde);
        List<XmlElement> ies = dde.elements("derivation/input");
        if (ies != null && !ies.isEmpty()) {
            _inputs = new ArrayList<Input>(ies.size());
            for (XmlElement ie : ies) {
                Input input = new Input(ie.value(), ie.value("@vid"));
                _inputs.add(input);
            }
        }
        _processed = dde.booleanValue("derivation/processed", false);
        _anonymized = dde.booleanValue("derivation/anonymized", false);
        _methodId = dde.value("derivation/method/id");
        _methodStep = dde.value("derivation/method/step");
    }

    public DerivedDataSet(String id, String proute, String name, String description) {
        super(id, proute, name, description, false, 0, false);
    }

    public List<Input> inputs() {

        return _inputs;
    }

    public String methodId() {

        return _methodId;
    }

    public String methodStep() {

        return _methodStep;
    }

    public boolean processed() {
        return _processed;
    }

    public boolean anonymized () {
    	return _anonymized;
    }
    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        return new DerivationDataSetCreate(po, this);
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        // TODO Auto-generated method stub
        return null;
    }

    public void setProcessed(boolean processed) {
        _processed = processed;
    }
    
    public void setAnonymized (boolean anonymized) {
    	_anonymized = anonymized;
    }
}
