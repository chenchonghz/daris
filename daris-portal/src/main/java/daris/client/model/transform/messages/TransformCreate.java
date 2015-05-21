package daris.client.model.transform.messages;

import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class TransformCreate extends ObjectMessage<String> {
    
    public static final String SERVICE_NAME = "transform.create";

    private long _duid;
    private int _dver;
    private String _name;
    private String _description;
    private Map<String, String> _params;
    private boolean _execute = true;

    public TransformCreate(long definitionUid, int definitionVersion, String name, String description,
            Map<String, String> params, boolean execute) {
        _duid = definitionUid;
        _dver = definitionVersion;
        _name = name;
        _description = description;
        _params = params;
        _execute = execute;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("definition", new String[] { "version", Integer.toString(_dver) }, _duid);
        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        if (_params != null && !_params.isEmpty()) {
            Set<String> params = _params.keySet();
            for (String param : params) {
                w.add("parameter", new String[] { "name", param }, _params.get(param));
            }
        }
        w.add("execute", _execute);
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return xe.value("uid");
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "transform";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
