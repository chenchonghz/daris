package daris.client.model.transform;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.exmethod.ExMethodTransformStep;
import daris.client.model.transform.Transform.Parameter;
import daris.client.model.transform.TransformDefinition.ParameterDefinition;
import daris.client.model.transform.messages.TransformCreate;

public class TransformBuilder {

    private TransformDefinition _defn;
    private String _name;
    private String _description;
    private Map<String, String> _params;

    private Map<String, Set<String>> _itValues;

    public TransformBuilder(TransformDefinition defn, String name, String description,
            Map<String, Set<String>> iteratorValues) {
        _defn = defn;
        _name = name;
        _description = description;
        _params = new HashMap<String, String>();
        Map<String, ParameterDefinition> paramDefns = _defn.paramDefinitions();
        if (paramDefns != null) {
            Collection<ParameterDefinition> pds = paramDefns.values();
            for (ParameterDefinition pd : pds) {
                String value = pd.value();
                if (value != null) {
                    _params.put(pd.name(), value);
                }
            }
        }
        _itValues = iteratorValues;
    }

    public void loadParameterValues(ExMethodTransformStep step, Transform transform) {
        Map<String, Transform.Parameter> params = transform.parameters();
        if (params == null || params.isEmpty()) {
            return;
        }
        ExMethodTransformStep.Iterator it = step.iterator();
        Set<String> names = params.keySet();
        for (String name : names) {
            if (it == null || !name.equals(it.parameter())) {
                Transform.Parameter param = params.get(name);
                setParameter(name, param.value());
            }
        }
    }

    public TransformDefinition definition() {
        return _defn;
    }

    public String name() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String description() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void save(XmlWriter w) {
        w.add("definition", new String[] { "version", Integer.toString(_defn.version()) }, _defn.uid());
        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        if (_params != null && _params.isEmpty()) {
            Set<String> params = _params.keySet();
            for (String param : params) {
                if (hasIterator() && !isIterator(param)) {
                    w.add("parameter", new String[] { "name", param }, _params.get(param));
                }
            }
        }
    }

    public String parameterValue(String name) {
        if (_params != null) {
            return _params.get(name);
        }
        return null;
    }

    public void setParameter(String name, Object value) {
        _params.put(name, value.toString());
    }

    public void removeParameter(String name) {
        _params.remove(name);
    }

    public String iteratorParameter() {
        if (hasIterator()) {
            return _itValues.keySet().iterator().next();
        } else {
            return null;
        }
    }

    public Set<String> iteratorValues() {
        String itParam = iteratorParameter();
        if (itParam != null) {
            return _itValues.get(itParam);
        } else {
            return null;
        }
    }

    public void setIteratorValues(String itParam, Collection<String> values) {
        _itValues.put(itParam, new HashSet<String>(values));
    }

    public boolean hasIterator() {
        return _itValues != null && !_itValues.isEmpty();
    }

    public boolean isIterator(String paramName) {
        if (_itValues == null || _itValues.isEmpty()) {
            return false;
        }
        return _itValues.keySet().contains(paramName);
    }

    public boolean isIterator(Parameter param) {
        return isIterator(param.name());
    }

    public void executeTransform(ObjectMessageResponse<String> rh) {
        if (hasIterator()) {
            String itParam = iteratorParameter();
            Set<String> itValues = iteratorValues();
            if (itValues != null) {
                Map<String, String> params = new HashMap<String, String>(_params);
                for (String itValue : itValues) {
                    params.put(itParam, itValue);
                    new TransformCreate(_defn.uid(), _defn.version(), _name, _description, params, true).send(rh);
                }
            }
        } else {
            new TransformCreate(_defn.uid(), _defn.version(), _name, _description, _params, true).send(rh);
        }
    }

}
