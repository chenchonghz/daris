package daris.plugin.asset.path;

import java.util.List;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class CompositeToken extends Token {

    private List<Token> _tokens;

    CompositeToken(String source, List<Token> tokens) {
        super(source);
        _tokens = tokens;
    }

    @Override
    public String compile(ServiceExecutor executor, XmlDoc.Element assetMeta) {
        StringBuilder sb = new StringBuilder();
        for (Token token : _tokens) {
            String v = token.compile(executor, assetMeta);
            if (v != null) {
                sb.append(v);
            }
        }
        String r = sb.toString();
        if (r != null && !r.isEmpty()) {
            return r;
        }
        return null;
    }

}
