package daris.client.model.dictionary.messages;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc.Element;

public class DictionaryEntryExists extends ObjectMessage<Boolean> {

    public static final String SERVICE_NAME = "dictionary.entry.exists";

    private String _dict;
    private String _term;

    public DictionaryEntryExists(String dict, String term) {
        _dict = dict;
        _term = term;
    }

    @Override
    protected String idToString() {
        return null;
    }

    @Override
    protected Boolean instantiate(Element xe) throws Throwable {
        return xe != null && xe.booleanValue("exists", false);
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        w.add("dictionary", _dict);
        w.add("term", _term);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

}
