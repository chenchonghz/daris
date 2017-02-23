package daris.plugin.asset.path;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class AssetPathCompiler implements Compilable {

    public static final String PREFIX = "$$$:";

    private String _source;
    private boolean _leadingSlash = false;
    private List<Component> _cs;

    private AssetPathCompiler(String s) throws Throwable {
        _source = s;
        _cs = new ArrayList<Component>();
        s = s.trim();
        if (s.startsWith("/")) {
            _leadingSlash = true;
            s = s.substring(1);
        }
        parseComponents(s, _cs);
    }

    @Override
    public String source() {
        return _source;
    }

    @Override
    public String compile(ServiceExecutor executor, XmlDoc.Element assetMeta) {
        StringBuilder sb = new StringBuilder();
        if (_leadingSlash) {
            sb.append('/');
        }

        for (int i = 0; i < _cs.size(); i++) {
            Component c = _cs.get(i);
            if (i > 0) {
                sb.append('/');
            }
            sb.append(c.compile(executor, assetMeta));
        }
        return sb.toString();
    }

    private static void parseComponents(String s, List<Component> components)
            throws Throwable {
        String[] cs = parseComponents(s);
        if (cs == null) {
            return;
        }
        for (String c : cs) {
            components.add(new Component(c));
        }
    }

    private static String[] parseComponents(String s) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty()) {
            return new String[] { s };
        }
        String[] cs = s.split("[\\ ]*\\}\\/\\{[\\ ]*");
        for (int i = 0; i < cs.length; i++) {
            while (cs[i].endsWith(" ")) {
                cs[i] = cs[i].substring(0, cs[i].length() - 1);
            }
            while (cs[i].endsWith("/")) {
                cs[i] = cs[i].substring(0, cs[i].length() - 1);
            }
            while (cs[i].startsWith(" ")) {
                cs[i] = cs[i].substring(1);
            }
            while (cs[i].startsWith("/")) {
                cs[i] = cs[i].substring(1);
            }
            if (!cs[i].startsWith("{")) {
                cs[i] = "{" + cs[i];
            }
            if (!cs[i].endsWith("}")) {
                cs[i] = cs[i] + "}";
            }
            assert cs[i].startsWith("{");
            assert cs[i].endsWith("}");
        }
        return cs;
    }

    public static AssetPathCompiler parse(String s) throws Throwable {

        if (s == null || !s.startsWith(PREFIX)) {
            throw new Exception("Invalid pattern: "
                    + ". Pattern must start with " + PREFIX);
        }
        return new AssetPathCompiler(s.substring(PREFIX.length()).trim());
    }

}
