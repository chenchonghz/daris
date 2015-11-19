package daris.client.model.dataset;

import arc.xml.XmlDoc.Element;
import daris.client.model.method.MethodRef;
import daris.client.model.method.MethodStep;

public class PrimaryDataSet extends DataSet {

    private MethodRef _method;
    private MethodStep _step;
    private String _subjectId;
    private String _subjectState;

    public PrimaryDataSet(Element oe) throws Throwable {
        super(oe);
        if (oe.elementExists("acquisition/method")) {
            _method = new MethodRef(oe.value("acquisition/method/id"));
            _step = new MethodStep(_method,
                    oe.value("acquisition/method/step"));
        }
        _subjectId = oe.value("acquisition/subject/id");
        _subjectState = oe.value("acquisition/subject/state");
    }

    @Override
    public MethodRef method() {
        return _method;
    }

    @Override
    public MethodStep step() {
        return _step;
    }

    public String subjectId() {
        return _subjectId;
    }

    public String subjectState() {
        return _subjectState;
    }

}
