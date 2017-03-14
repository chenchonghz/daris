package daris.plugin.asset.path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginLog;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class XpathToken extends Token {

    public static final String SAFE_XPATH_CHARS = "^\\{\\}\\\"\\\\";

    public static final String PATTERN_START = "^\\ *\\{\\ *xpath\\ *\\(";

    public static final String PATTERN_END = "\\)\\ *\\}\\ *$";

    public static final String PATTERN = PATTERN_START + "[" + SAFE_XPATH_CHARS + "]+" + PATTERN_END;

    XpathToken(String s) {
        super(s);
    }

    @Override
    public String compile(ServiceExecutor executor, XmlDoc.Element assetMeta) {
        String xpath = null;
        try {
            xpath = extractXpath(source());
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, e.getMessage(), e);
        }
        if (xpath == null) {
            return null;
        }
        String value = null;
        try {
            value = assetMeta.value(xpath);
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, "Asset meta value for xpath \"" + xpath + "\" is null.", e);
        }
        if (value != null) {
            value = value.trim();
            value = FileName.replaceUnsafeCharacters(value, "_");
            return value;
        } else {
            return null;
        }
    }

    private static String extractXpath(String token) throws Throwable {
        Pattern pStart = Pattern.compile(PATTERN_START);
        Matcher mStart = pStart.matcher(token);
        if (!mStart.find()) {
            throw new Exception("Failed to parse xpath token: " + token);
        }
        int beginIdx = mStart.end();
        Pattern pEnd = Pattern.compile(PATTERN_END);
        Matcher mEnd = pEnd.matcher(token);
        if (!mEnd.find()) {
            throw new Exception("Failed to parse xpath token: " + token);
        }
        int endIdx = mEnd.start();
        return token.substring(beginIdx, endIdx);
    }

    public static boolean isXpathToken(String s) {
        if (s == null) {
            return false;
        }
        return Pattern.matches(XpathToken.PATTERN, s);
    }

    public static void main(String[] args) throws Throwable {
        String valid = " { xpath (meta/mf-note/note) }";
        String invalid = " { path(meta/mf-note/note) }";
        System.out.println(String.format("%20s\t\t%s", valid, isXpathToken(valid) ? "Y" : "N"));
        System.out.println(String.format("%20s\t\t%s", invalid, isXpathToken(invalid) ? "Y" : "N"));
        System.out.println(extractXpath(valid));
    }
}
