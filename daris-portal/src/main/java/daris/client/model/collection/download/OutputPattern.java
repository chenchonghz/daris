package daris.client.model.collection.download;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;

public class OutputPattern {

    private String _name;
    private String _description;
    private String _pattern;

    public OutputPattern(String pattern, String name, String description) {
        _pattern = pattern;
        _name = name;
        _description = description;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public String pattern() {
        return _pattern;
    }

    public static Map<String, OutputPattern> instantiateShoppingCartLayoutPatterns(List<XmlElement> lpes) {
        if (lpes != null && !lpes.isEmpty()) {
            Map<String, OutputPattern> ps = new LinkedHashMap<String, OutputPattern>(lpes.size());
            for (XmlElement lpe : lpes) {
                String pattern = lpe.value();
                String name = lpe.value("@name");
                String description = lpe.value("@description");
                ps.put(name, new OutputPattern(pattern, name, description));
            }
            if (!ps.isEmpty()) {
                return ps;
            }
        }
        return null;
    }

}
