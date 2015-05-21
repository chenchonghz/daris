package daris.client.model.transcode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;

public class Transcode {

    public static final String NONE = "none";

    private String _from;
    private String _to;
    private String _description;
    private String _toDescription;

    public Transcode(String from, String to, String description, String toDescription) {

        _from = from;
        _to = to;
        _description = description;
        _toDescription = toDescription;
    }

    public Transcode(XmlElement te) {

        _from = te.value("from");
        _to = te.value("to");
        assert _from != null;
        assert _to != null;
        _description = te.value("description");
        _toDescription = te.value("to/@description");
    }

    public Transcode(String from, String to) {
        this(from, to, null, null);
    }

    public Transcode(String from) {
        this(from, NONE, null, null);
    }

    public String from() {
        return _from;
    }

    public String to() {
        return _to;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Transcode)) {
            return false;
        }
        Transcode t = (Transcode) o;
        return ObjectUtil.equals(_from, t.from()) && ObjectUtil.equals(_to, t.to());
    }

    @Override
    public final String toString() {
        return _to;
    }

    public String description() {
        return _description;
    }

    public String toDescription() {
        return _toDescription;
    }

    public static Map<String, Transcode> instantiateMap(List<XmlElement> tes) throws Throwable {

        if (tes != null && !tes.isEmpty()) {
            Map<String, Transcode> transcodes = new LinkedHashMap<String, Transcode>();
            for (XmlElement te : tes) {
                Transcode transcode = new Transcode(te);
                transcodes.put(transcode.from(), transcode);
            }
            if (!transcodes.isEmpty()) {
                return transcodes;
            }
        }
        return null;
    }

    public static List<Transcode> instantiateList(List<XmlElement> tes) throws Throwable {

        if (tes != null && !tes.isEmpty()) {
            List<Transcode> transcodes = new ArrayList<Transcode>();
            for (XmlElement te : tes) {
                transcodes.add(new Transcode(te));
            }
            if (!transcodes.isEmpty()) {
                return transcodes;
            }
        }
        return null;
    }
}
