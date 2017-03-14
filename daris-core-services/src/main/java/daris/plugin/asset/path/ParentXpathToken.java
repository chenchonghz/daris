package daris.plugin.asset.path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginLog;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;

public class ParentXpathToken extends Token {

    public static final String PATTERN_PARENT_START = "^\\ *\\{\\ *parent\\ *\\(\\ *";

    public static final String PATTERN_PARENT_LEVEL = "[1-9]+";

    public static final String PATTERN_XPATH_START = "\\ *\\)\\ *\\.\\ *xpath\\ *\\(";

    public static final String PATTERN_END = "\\)\\ *\\}\\ *$";

    public static final String PATTERN = PATTERN_PARENT_START
            + PATTERN_PARENT_LEVEL + PATTERN_XPATH_START + "["
            + XpathToken.SAFE_XPATH_CHARS + "]+" + PATTERN_END;

    ParentXpathToken(String s) {
        super(s);
    }

    @Override
    public String compile(ServiceExecutor executor, Element assetMeta) {

        String cid = null;
        try {
            cid = assetMeta.value("cid");
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, "No cid is found.", e);
        }
        if (cid == null) {
            return null;
        }
        try {
            int level = ParentXpathToken.extractParentLevel(source());
            String parentCid = parentCid(cid, level);
            String parentXpath = ParentXpathToken.extractParentXpath(source());
            XmlDoc.Element parentAssetMeta = executor.execute("asset.get",
                    "<args><cid>" + parentCid + "</cid></args>", null, null)
                    .element("asset");
            String value = parentAssetMeta.value(parentXpath);
            if (value != null) {
                value = value.trim();
                value = FileName.replaceUnsafeCharacters(value, "_");
            }
            return value;
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, e.getMessage(), e);
            return null;
        }
    }

    static int extractParentLevel(String token) throws Throwable {
        Pattern p = Pattern.compile(PATTERN_PARENT_START);
        Matcher m = p.matcher(token);
        if (!m.find()) {
            throw new Exception(
                    "Failed to parse parent level from parent xpath token: "
                            + token);
        }
        int beginIdx = m.end();
        p = Pattern.compile(PATTERN_XPATH_START);
        m = p.matcher(token);
        if (!m.find(beginIdx)) {
            throw new Exception(
                    "Failed to parse parent level from parent xpath token: "
                            + token);
        }
        int endIdx = m.start();
        int parentLevel = Integer.parseInt(token.substring(beginIdx, endIdx));
        return parentLevel;
    }

    static String extractParentXpath(String token) throws Throwable {
        Pattern p = Pattern.compile(PATTERN_PARENT_START + PATTERN_PARENT_LEVEL
                + PATTERN_XPATH_START);
        Matcher m = p.matcher(token);
        if (!m.find()) {
            throw new Exception(
                    "Failed to parse xpath from parent xpath token: " + token);
        }
        int beginIdx = m.end();
        p = Pattern.compile(PATTERN_END);
        m = p.matcher(token);
        if (!m.find(beginIdx)) {
            throw new Exception(
                    "Failed to parse xpath from parent xpath token: " + token);
        }
        int endIdx = m.start();
        return token.substring(beginIdx, endIdx);
    }

    static String parentCid(String cid, int level) {

        assert level >= 1;

        return CidToken.cid(cid, 0, -1 * level);
    }

    public static boolean isParentXpathToken(String s) {
        if (s == null) {
            return false;
        }
        return Pattern.matches(ParentXpathToken.PATTERN, s);
    }

    public static void main(String[] args) throws Throwable {
        String valid = " { parent(1).xpath(meta/mf-note/note) }";
        String invalid = " { path(meta/mf-note/note) }";
        System.out.println(String.format("%20s\t\t%s", valid,
                isParentXpathToken(valid) ? "Y" : "N"));
        System.out.println(String.format("%20s\t\t%s", invalid,
                isParentXpathToken(invalid) ? "Y" : "N"));
        System.out.println(extractParentLevel(valid));
        System.out.println(extractParentXpath(valid));
    }

}
