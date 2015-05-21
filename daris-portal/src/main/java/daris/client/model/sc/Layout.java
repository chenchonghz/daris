package daris.client.model.sc;

import java.util.List;

import daris.client.model.sc.messages.ShoppingCartLayoutPatternList;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

public class Layout {

    public static enum Type {
        custom, flat, preserved;
        public static Type fromString(String type) {
            Type[] ts = values();
            for (Type t : ts) {
                if (t.name().equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return null;
        }

    }

    public static class Pattern {
        private String _name;
        private String _description;
        private String _pattern;

        public Pattern(String pattern, String name, String description) {
            _pattern = pattern;
            _name = name;
            _description = description;
        }

        public Pattern(XmlElement pe) {
            _name = pe.value("@name");
            _description = pe.value("@description");
            _pattern = pe.value();
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

        public final String toString() {
            return name();
        }

        public boolean equals(Object o) {
            if (o != null) {
                if (o instanceof Pattern) {
                    if (ObjectUtil.equals(((Pattern) o).pattern(), pattern())) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static void resolve(final String pattern, final ObjectResolveHandler<Pattern> rh) {
            if(pattern==null){
                rh.resolved(null);
                return;
            }
            new ShoppingCartLayoutPatternList().send(new ObjectMessageResponse<List<Layout.Pattern>>(){

                @Override
                public void responded(List<Pattern> patterns) {
                    if(patterns!=null){
                        for(Pattern p : patterns){
                            if(pattern.equals(p.pattern())){
                                rh.resolved(p);
                                return;
                            }
                        }
                    }
                    rh.resolved(null);
                }});
        }
    }

    private Type _type;
    private Pattern _pattern;

    public Layout(Type type, Pattern pattern) {
        _type = type;
        if (type == Type.custom) {
            _pattern = pattern;
        }
    }

    public Layout(XmlElement le) {
        _type = Layout.Type.fromString(le.value());
        XmlElement lpe = le.element("layout-pattern");
        if (lpe != null) {
            _pattern = new Layout.Pattern(lpe);
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Layout)) {
            return false;
        }
        Layout lo = (Layout) o;
        return _type == lo.type() && ObjectUtil.equals(_pattern, lo.pattern());
    }

    public Type type() {
        return _type;
    }

    public Pattern pattern() {
        return _pattern;
    }

    public static Layout instantiate(XmlElement le) throws Throwable {
        if (le != null) {
            return new Layout(le);
        }
        return null;
    }

    public void saveUpdateArgs(XmlWriter w) {
        w.add("layout", _type.name());
        if (_type == Type.custom) {
            w.add("layout-pattern", _pattern.pattern());
        }
    }

}
