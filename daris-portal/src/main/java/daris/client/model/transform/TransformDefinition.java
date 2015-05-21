package daris.client.model.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

public class TransformDefinition extends TObject {

    public static final String TYPE_NAME = "transform-definition";

    public static final int VERSION_LATEST = 0;

    public static class ParameterDefinition {

        public static enum DataType {

            STRING, BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE;

            @Override
            public String toString() {

                return super.toString().toLowerCase();
            }

            public static String[] stringValues() {

                DataType[] vs = values();
                String[] svs = new String[vs.length];
                for (int i = 0; i < vs.length; i++) {
                    svs[i] = vs[i].toString();
                }
                return svs;
            }

            public static DataType fromString(String type) throws Throwable {

                if (type != null) {
                    DataType[] vs = values();
                    for (int i = 0; i < vs.length; i++) {
                        if (vs[i].toString().equalsIgnoreCase(type)) {
                            return vs[i];
                        }
                    }
                }
                throw new Exception("Invalid data type: " + type);
            }

        }

        private String _name;
        private DataType _type;
        private String _description;
        private int _minOccurs;
        private int _maxOccurs;
        private String _value;

        public ParameterDefinition(String name, DataType type, String description, int minOccurs, int maxOccurs,
                String value) {
            _name = name;
            _type = type;
            _description = description;
            _minOccurs = minOccurs;
            _maxOccurs = maxOccurs;
            _value = value;
        }

        public ParameterDefinition(XmlElement pe) throws Throwable {

            _name = pe.value("@name");
            _type = DataType.fromString(pe.value("@type"));
            if (_type == null) {
                throw new Exception("Failed to parse parameter type. Found " + pe.value("@type") + ".");
            }
            _description = pe.value("description");
            _minOccurs = pe.intValue("@min-occurs", 1);
            _maxOccurs = pe.intValue("@max-occurs", 1);
            _value = pe.value("value");
        }

        /**
         * The minimum number of occurrences required.
         * 
         * @return
         */
        public int minOccurs() {
            return _minOccurs;
        }

        /**
         * The maximum number of occurrences required.
         * 
         * @return
         */
        public int maxOccurs() {
            return _maxOccurs;
        }

        /**
         * The name of the parameter
         * 
         * @return
         */
        public String name() {
            return _name;
        }

        /**
         * The description about the parameter.
         * 
         * @return
         */
        public String description() {
            return _description;
        }

        /**
         * The value pre-specified.
         * 
         * @return
         */
        public String value() {
            return _value;
        }

        public DataType type() {
            return _type;
        }

        public void save(XmlWriter w) throws Throwable {
            w.push("parameter",
                    new String[] { "type", type().toString(), "name", name(), "min-occurs",
                            Integer.toString(minOccurs()), "max-occurs", Integer.toString(maxOccurs()) });
            if (description() != null) {
                w.add("description", description());
            }
            if (value() != null) {
                w.add("value", value());
            }
            w.pop();
        }

        @Override
        public boolean equals(Object o) {
            if (o != null) {
                if (o instanceof ParameterDefinition) {
                    return ObjectUtil.equals(((ParameterDefinition) o).name(), name());
                }
            }
            return false;
        }

        public static Map<String, ParameterDefinition> parse(List<XmlElement> pdes) throws Throwable {
            if (pdes == null || pdes.isEmpty()) {
                return null;
            }
            Map<String, ParameterDefinition> params = new HashMap<String, ParameterDefinition>();
            for (XmlElement pde : pdes) {
                ParameterDefinition pd = new ParameterDefinition(pde);
                params.put(pd.name(), pd);
            }
            return params;
        }

    }

    private int _version;

    private Map<String, ParameterDefinition> _params;

    public TransformDefinition(XmlElement te) throws Throwable {
        super(te);
        _version = te.intValue("@version", VERSION_LATEST);
        _params = ParameterDefinition.parse(te.elements("parameter"));
    }

    public int version() {
        return _version;
    }

    public Map<String, ParameterDefinition> paramDefinitions() {
        return _params;
    }

    @Override
    public void save(XmlWriter w) throws Throwable {
        super.save(w);
        if (_params != null && !_params.isEmpty()) {
            for (ParameterDefinition p : _params.values()) {
                p.save(w);
            }
        }
    }

}
