package daris.client.model.object.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.object.DObjectRef;

public class CollectionTranscodeList
        extends ObjectMessage<Map<String, List<String>>> {

    private String _cid;

    public CollectionTranscodeList(DObjectRef o) {
        this(o.citeableId());
    }

    public CollectionTranscodeList(String cid) {
        _cid = cid;
    }

    @Override
    protected String idToString() {
        return _cid;
    }

    @Override
    protected Map<String, List<String>> instantiate(Element xe)
            throws Throwable {
        if (xe != null && xe.elementExists("transcode")) {
            List<XmlDoc.Element> tes = xe.elements("transcode");
            Map<String, List<String>> map = new TreeMap<String, List<String>>();
            for (XmlDoc.Element te : tes) {
                String from = te.value("@from");
                List<String> tos = new ArrayList<String>(te.values("to"));
                map.put(from, tos);
            }
            return map;
        }
        return null;
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        w.add("cid", _cid);
    }

    @Override
    protected String messageServiceName() {
        return "daris.collection.transcode.list";
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

}
