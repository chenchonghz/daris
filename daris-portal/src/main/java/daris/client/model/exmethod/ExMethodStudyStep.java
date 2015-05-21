package daris.client.model.exmethod;

import java.util.List;

import arc.mf.client.xml.XmlElement;

public class ExMethodStudyStep extends ExMethodStep {

    private String _type;
    private List<XmlElement> _meta;

    public ExMethodStudyStep(String exmId, String exmProute, String step, String name, State state, String notes,
            String type, List<XmlElement> meta, boolean editable) {

        super(exmId, exmProute, step, name, state, notes, editable);

        _type = type;
        _meta = meta;
    }

    public String studyType() {

        return _type;
    }

    public List<XmlElement> metadata() {

        return _meta;
    }

}