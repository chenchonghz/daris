package daris.plugin.asset.path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginLog;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class StringToken extends Token {

    public static final String PATTERN_START = "^\\ *\\{\\ *\\'";

    public static final String PATTERN_END = "\\'\\ *\\}\\ *$";

    public static final String PATTERN = PATTERN_START + "["
            + FileName.SAFE_CHARS + "]+" + PATTERN_END;

    StringToken(String s) {
        super(s);
    }

    @Override
    public String compile(ServiceExecutor executor, XmlDoc.Element assetMeta) {
        String value = null;
        try {
            value = extractStringValue(source());
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, e.getMessage(), e);
        }
        if (value == null) {
            return null;
        } else {
            return FileName.replaceUnsafeCharacters(value, "_");
        }

    }

    private static String extractStringValue(String token) throws Throwable {
        Pattern pStart = Pattern.compile(PATTERN_START);
        Matcher mStart = pStart.matcher(token);
        if (!mStart.find()) {
            throw new Exception("Failed to parse string token: " + token);
        }
        int beginIdx = mStart.end();
        Pattern pEnd = Pattern.compile(PATTERN_END);
        Matcher mEnd = pEnd.matcher(token);
        if (!mEnd.find()) {
            throw new Exception("Failed to parse string token: " + token);
        }
        int endIdx = mEnd.start();
        return token.substring(beginIdx, endIdx);
    }

    public static final boolean isStringToken(String s) {
        return Pattern.matches(PATTERN, s);
    }

    public static void main(String[] args) throws Throwable {
        String valid = " {'abc.txt' }";
        String invalid = " { '****' }";
        System.out.println(String.format("%10s\t\t%s", valid,
                isStringToken(valid) ? "Y" : "N"));
        System.out.println(String.format("%10s\t\t%s", invalid,
                isStringToken(invalid) ? "Y" : "N"));
        System.out.println(extractStringValue(valid));
    }

}
