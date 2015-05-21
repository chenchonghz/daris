package daris.client.model.query.messages;

import daris.client.model.query.options.QueryOptions;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class QueryCount extends ObjectMessage<Long> {

    private String _where;

    public QueryCount(String where) {
        _where = where;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("where", _where);
        w.add("action", QueryOptions.Action.count);
    }

    @Override
    protected String messageServiceName() {
        return "asset.query";
    }

    @Override
    protected Long instantiate(XmlElement xe) throws Throwable {
        return xe.longValue("value");
    }

    @Override
    protected String objectTypeName() {
        return "query";
    }

    @Override
    protected String idToString() {
        return _where;
    }

}
