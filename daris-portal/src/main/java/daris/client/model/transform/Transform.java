package daris.client.model.transform;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.client.util.DateTime;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

public class Transform extends TObject {

    public static final String TYPE_NAME = "transform";

    public static enum Type {
        kepler, exec;
        public static Type fromString(String s) {
            if (s != null) {
                Type[] vs = values();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i].toString().equalsIgnoreCase(s)) {
                        return vs[i];
                    }
                }
            }
            return null;
        }

        public static String[] stringValues() {
            Type[] vs = values();
            String[] ss = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                ss[i] = vs[i].toString();
            }
            return ss;
        }

    }

    public static class Parameter {
        private String _name;
        private String _value;

        public Parameter(String name, String value) {
            _name = name;
            _value = value;
        }

        public Parameter(XmlElement pe) throws Throwable {
            this(pe.value("@name"), pe.value());
        }

        public String name() {
            return _name;
        }

        public String value() {
            return _value;
        }

        public void save(XmlWriter w) throws Throwable {
            w.add("parameter", new String[] { "name", name() }, value());
        }

        public static Parameter parse(XmlElement pe) throws Throwable {
            if (pe != null) {
                String name = pe.value("@name");
                String value = pe.value();
                if (name != null && value != null) {
                    return new Parameter(pe);
                }
            }
            return null;
        }

        public static Map<String, Parameter> parse(List<XmlElement> pes) throws Throwable {
            if (pes != null) {
                Map<String, Parameter> params = new HashMap<String, Parameter>();
                for (XmlElement pe : pes) {
                    Parameter p = new Parameter(pe);
                    params.put(p.name(), p);
                }
                if (!params.isEmpty()) {
                    return params;
                }
            }
            return null;
        }

    }

    public static class Status {

        public static enum State {

            pending, running, suspended, terminated, failed, unknown;

            public static State fromString(String s) {
                if (s != null) {
                    State[] vs = values();
                    for (int i = 0; i < vs.length; i++) {
                        if (vs[i].toString().equalsIgnoreCase(s)) {
                            return vs[i];
                        }
                    }
                }
                return null;
            }

            public static String[] stringValues() {
                State[] vs = values();
                String[] ss = new String[vs.length];
                for (int i = 0; i < vs.length; i++) {
                    ss[i] = vs[i].toString();
                }
                return ss;
            }

        }

        private Date _time;
        private State _state;

        public Status(State state, Date time) {
            _state = state;
            _time = time;
        }

        public Status(XmlElement se) throws Throwable {
            _state = State.fromString(se.value());
            if (_state == null) {
                throw new Exception("Invalid state " + se.value());
            }

            _time = DateTime.SERVER_DATE_TIME_FORMAT.parse(se.value("@time"));
        }

        public State state() {
            return _state;
        }

        public Date time() {
            return _time;
        }

        @Override
        public String toString() {
            return _state.toString();
        }

        public void save(XmlWriter w) throws Throwable {
            w.add("status", new String[] { "time", DateTime.SERVER_DATE_TIME_FORMAT.format(_time) }, _state);
        }

        public static Status parse(XmlElement se) throws Throwable {
            if (se == null) {
                return null;
            }
            return new Status(se);
        }

    }

    public static class Log {
        public static enum Type {
            error, warning, info;
            public static Type fromString(String s) {
                if (s != null) {
                    Type[] vs = values();
                    for (int i = 0; i < vs.length; i++) {
                        if (vs[i].toString().equalsIgnoreCase(s)) {
                            return vs[i];
                        }
                    }
                }
                return null;
            }

            public static String[] stringValues() {
                Type[] vs = values();
                String[] ss = new String[vs.length];
                for (int i = 0; i < vs.length; i++) {
                    ss[i] = vs[i].toString();
                }
                return ss;
            }
        }

        private Type _type;
        private Date _time;
        private String _msg;

        public Log(Type type, Date time, String msg) {
            _type = type;
            _time = time;
            _msg = msg;
        }

        public Log(XmlElement le) throws Throwable {
            this(Type.fromString(le.value("@type")), DateTime.SERVER_DATE_TIME_FORMAT.parse(le.value("@time")), le
                    .value());
        }

        public Type type() {
            return _type;
        }

        public Date time() {
            return _time;
        }

        public String message() {
            return _msg;
        }

        @Override
        public String toString() {
            return DateTime.SERVER_DATE_TIME_FORMAT.format(_time) + ": " + _type + ": " + _msg;
        }

        public void save(XmlWriter w) throws Throwable {
            w.add("log",
                    new String[] { "type", type().toString(), "time", DateTime.SERVER_DATE_TIME_FORMAT.format(time()) },
                    message());
        }

        public static Log parse(XmlElement le) throws Throwable {
            if (le != null) {
                return new Log(le);
            }
            return null;
        }

        public static List<Log> parse(List<XmlElement> les) throws Throwable {
            if (les != null && !les.isEmpty()) {
                List<Log> logs = new Vector<Log>(les.size());
                for (XmlElement le : les) {
                    logs.add(new Log(le));
                }
                return logs;
            }
            return null;
        }
    }

    public static class Progress {
        private int _progress;
        private int _total;
        private Date _time;

        public Progress(XmlElement pe) throws Throwable {
            _progress = pe.intValue();
            _total = pe.intValue("@total");
            _time = pe.dateValue("@time");
        }

        public Progress(int progress, int total, Date time, String message) {
            _progress = progress;
            _total = total;
            _time = time;
        }

        public int total() {
            return _total;
        }

        public int progress() {
            return _progress;
        }

        @Override
        public final String toString() {
            return Integer.toString(_progress) + "/" + Integer.toString(_total);
        }

        public Date time() {
            return _time;
        }

        public void save(XmlWriter w) throws Throwable {
            w.add("progress", new String[] { "total", Integer.toString(_total), "time",
                    DateTime.SERVER_DATE_TIME_FORMAT.format(time()) }, Integer.toString(_progress));
        }

        public static Progress parse(XmlElement pe) throws Throwable {
            if (pe != null) {
                return new Progress(pe);
            }
            return null;
        }

    }

    private TransformDefinitionRef _defn;
    private Status _status;
    private Progress _progress;
    private List<Log> _logs;
    private Map<String, String> _props;
    private Map<String, Parameter> _params;

    public Transform(XmlElement te) throws Throwable {
        super(te);
        _defn = new TransformDefinitionRef(te.longValue("definition"), te.intValue("definition/version",
                TransformDefinition.VERSION_LATEST));
        _status = Status.parse(te.element("status"));
        List<XmlElement> rpes = te.elements("runtime/property");
        if (rpes != null && !rpes.isEmpty()) {
            _props = new HashMap<String, String>();
            for (XmlElement rpe : rpes) {
                _props.put(rpe.value("@name"), rpe.value());
            }
        }
        _params = Parameter.parse(te.elements("parameter"));
        _progress = Progress.parse(te.element("progress"));
        _logs = Log.parse(te.elements("log"));
    }

    public TransformDefinitionRef definition() {
        return _defn;
    }

    public Map<String, Parameter> parameters() {
        return _params;
    }

    public boolean hasParameters() {
        return _params != null && !_params.isEmpty();
    }

    public Map<String, String> runtimeProperties() {
        return _props;
    }

    public boolean hasRuntimeProperties() {
        return _props != null && !_props.isEmpty();
    }

    public List<Log> logs() {
        return _logs;
    }

    public boolean hasLogs() {
        return _logs != null && !_logs.isEmpty();
    }

    public Progress progress() {
        return _progress;
    }

    public Status status() {
        return _status;
    }

    @Override
    public void save(XmlWriter w) throws Throwable {
        super.save(w);
        w.add("definition", new String[] { "version", Integer.toString(_defn.version()) }, _defn.uid());
        if (_status != null) {
            _status.save(w);
        }
        if (_logs != null && !_logs.isEmpty()) {
            for (Log l : _logs) {
                l.save(w);
            }
        }
        if (_progress != null) {
            _progress.save(w);
        }
        if (_params != null && !_params.isEmpty()) {
            for (Parameter p : _params.values()) {
                if (p != null) {
                    p.save(w);
                }
            }
        }
        if (_props != null && !_props.isEmpty()) {
            w.push("runtime");
            for (String name : _props.keySet()) {
                w.add("property", new String[] { "name", name }, _props.get(name));
            }
            w.pop();
        }
    }

    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>transform</h3>");
        sb.append("<ul>");
        sb.append("<li style=\"margin-top:5px;\"><b>uid: </b>" + uid() + "</li>");
        if (name() != null) {
            sb.append("<li style=\"margin-top:5px;\"><b>name: </b>" + name() + "</li>");
        }
        if (description() != null) {
            sb.append("<li style=\"margin-top:5px;\"><b>description: </b>" + description() + "</li>");
        }
        sb.append("<li style=\"margin-top:5px;\"><b>type: </b>" + type() + "</li>");
        sb.append("<li style=\"margin-top:5px;\"><b>definition: </b><ul><li><b>uid: </b>" + definition().uid()
                + "</li><li><b>version: </b>" + definition().version() + "</li></ul></li>");
        Map<String, Parameter> params = parameters();
        if (params != null) {
            for (Parameter p : params.values()) {
                sb.append("<li style=\"margin-top:5px;\"><b>parameter[" + p.name() + "]: </b>" + p.value() + "</li>");
            }
        }

        sb.append("<li style=\"margin-top:5px;\"><b>runtime:</b><ul>");
        if (_props != null) {
            for (String pn : _props.keySet()) {
                sb.append("<li><b>property[" + pn + "]: </b>" + _props.get(pn) + "</li>");
            }
        }
        sb.append("</ul></li>");

        sb.append("<li style=\"margin-top:5px;\"> <b>status["
                + DateTime.SERVER_DATE_TIME_FORMAT.format(status().time()) + "]: </b>" + status().state() + "</li>");

        if (progress() != null) {
            sb.append("<li style=\"margin-top:5px;\"> <b>progress["
                    + DateTime.SERVER_DATE_TIME_FORMAT.format(progress().time()) + "]:</b>" + progress().progress()
                    + "/" + progress().total() + "</li>");
        }

        if (_logs != null) {
            for (Log l : _logs) {
                sb.append("<li style=\"margin-top:5px;\"><b>log[" + DateTime.SERVER_DATE_TIME_FORMAT.format(l.time())
                        + ": " + l.type() + "]: </b>" + l.message() + "</li>");
            }
        }

        sb.append("</ul>");
        return sb.toString();
    }

}
