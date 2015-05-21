package daris.client.model.study.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class StudyTypeList extends ObjectMessage<List<String>> {

    /*
     * ex-method id
     */
    private String _exmid;

    public StudyTypeList(String exMethodId) {
        _exmid = exMethodId;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_exmid != null) {
            w.add("id", _exmid);
        }
    }

    @Override
    protected String messageServiceName() {
        if (_exmid != null) {
            return "om.pssd.ex-method.study.type.list";
        } else {
            return "om.pssd.study.type.describe";
        }
    }

    @Override
    protected List<String> instantiate(XmlElement xe) throws Throwable {
        return xe.values("type/name");
    }

    @Override
    protected String objectTypeName() {
        return "study types";
    }

    @Override
    protected String idToString() {
        return _exmid;
    }

}
