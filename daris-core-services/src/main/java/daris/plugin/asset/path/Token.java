package daris.plugin.asset.path;

import java.util.ArrayList;
import java.util.List;

public abstract class Token implements Compilable {

    private String _source;

    Token(String s) {
        _source = s;
    }

    @Override
    public String source() {
        return _source;
    }

    private static String[] parseTokens(String s) {
        String[] sts = s.split("\\ *\\}\\ *\\{\\ *");
        for (int i = 0; i < sts.length; i++) {
            sts[i] = sts[i].trim();
            if (!sts[i].startsWith("{")) {
                sts[i] = "{" + sts[i];
            }
            if (!sts[i].endsWith("}")) {
                sts[i] = sts[i] + "}";
            }
        }
        return sts;
    }

    private static Token parseSingleToken(String s) throws Throwable {
        if (StringToken.isStringToken(s)) {
            return new StringToken(s);
        } else if (XpathToken.isXpathToken(s)) {
            return new XpathToken(s);
        } else if (CidToken.isCidToken(s)) {
            return new CidToken(s);
        } else if (ParentXpathToken.isParentXpathToken(s)) {
            return new ParentXpathToken(s);
        } else {
            throw new Exception("Failed to parse pattern: " + s);
        }
    }

    public static Token parse(String s) throws Throwable {
        String[] sts = parseTokens(s);
        assert sts.length >= 1;
        if (sts.length == 1) {
            return parseSingleToken(sts[0]);
        } else {
            List<Token> tokens = new ArrayList<Token>();
            for (String st : sts) {
                tokens.add(parseSingleToken(st));
            }
            return new CompositeToken(s, tokens);
        }
    }

}
