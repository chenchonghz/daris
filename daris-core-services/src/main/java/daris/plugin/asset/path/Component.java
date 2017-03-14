package daris.plugin.asset.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class Component implements Compilable {

    private String _source;

    private List<Token> _options;

    Component(String s) throws Throwable {
        _source = s;
        _options = new ArrayList<Token>();
        parseOptions(s, _options);
    }

    @Override
    public String source() {
        return _source;
    }

    private static void parseOptions(String s, List<Token> options)
            throws Throwable {
        String[] os = parseOptions(s);
        for (String o : os) {
            options.add(Token.parse(o));
        }
    }

    private static String[] parseOptions(String s) {
        String[] os = s.split("\\ *\\}\\ *\\|\\ *\\{\\ *");
        for (int i = 0; i < os.length; i++) {
            os[i] = os[i].trim();
            if (!os[i].startsWith("{")) {
                os[i] = "{" + os[i];
            }
            if (!os[i].endsWith("}")) {
                os[i] = os[i] + "}";
            }
        }
        return os;
    }

    @Override
    public String compile(ServiceExecutor executor, XmlDoc.Element assetMeta) {
        for (Iterator<Token> it = _options.iterator(); it.hasNext();) {
            Token option = it.next();
            String r = option.compile(executor, assetMeta);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

}
