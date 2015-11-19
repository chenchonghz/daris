package daris.client.model.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.method.MethodRef;
import daris.client.model.method.MethodStep;

public class DerivedDataSet extends DataSet {

    public static class Input {
        public final String cid;
        public final long vid;

        public Input(String cid, long vid) {
            this.cid = cid;
            this.vid = vid;
        }
    }

    private MethodRef _method;
    private MethodStep _step;
    private boolean _processed;
    private List<Input> _inputs;

    public DerivedDataSet(Element oe) throws Throwable {
        super(oe);
        if (oe.elementExists("derivation/method")) {
            _method = new MethodRef(oe.value("derivation/method/id"));
            _step = new MethodStep(_method, oe.value("derivation/method/step"));
        }
        _processed = oe.booleanValue("derivation/processed", false);
        if (oe.elementExists("derivation/input")) {
            List<XmlDoc.Element> ies = oe.elements("derivation/input");
            _inputs = new ArrayList<Input>(ies.size());
            for (XmlDoc.Element ie : ies) {
                Input input = new Input(ie.value(), ie.longValue("@vid"));
                _inputs.add(input);
            }
        }
    }

    @Override
    public MethodRef method() {
        return _method;
    }

    @Override
    public MethodStep step() {
        return _step;
    }

    public boolean processed() {
        return _processed;
    }

    public List<Input> inputs() {
        if (_inputs == null || _inputs.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableList(_inputs);
    }

}
