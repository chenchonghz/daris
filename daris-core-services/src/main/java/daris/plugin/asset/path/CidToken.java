package daris.plugin.asset.path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginLog;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc.Element;

public class CidToken extends Token {

    public static final String PATTERN_START = "^\\ *\\{\\ *cid\\ *\\(\\ *";

    public static final String PATTERN_END = "\\ *\\)\\ *\\}\\ *$";

    public static final String PATTERN = PATTERN_START + "\\-?\\d+\\ *,\\ *\\-?\\d+" + PATTERN_END;

    CidToken(String s) {
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
            int idx[] = extractCidIndexes(source());
            return cid(cid, idx[0], idx[1]);
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.WARNING, e.getMessage(), e);
            return null;
        }
    }

    static int[] extractCidIndexes(String token) throws Throwable {
        Pattern pStart = Pattern.compile(PATTERN_START);
        Matcher mStart = pStart.matcher(token);
        if (!mStart.find()) {
            throw new Exception("Failed to parse cid token: " + token);
        }
        int beginIdx = mStart.end();
        Pattern pEnd = Pattern.compile(PATTERN_END);
        Matcher mEnd = pEnd.matcher(token);
        if (!mEnd.find()) {
            throw new Exception("Failed to parse cid token: " + token);
        }
        int endIdx = mEnd.start();
        String s = token.substring(beginIdx, endIdx);
        String[] parts = s.split(",");
        assert parts.length == 2;
        int[] idx = new int[2];
        idx[0] = Integer.parseInt(parts[0].trim());
        idx[1] = Integer.parseInt(parts[1].trim());
        return idx;
    }

    static String cid(String cid, int beginIndex, int endIndex) {
        String[] parts = cid.split("\\.");
        if (parts == null || parts.length == 1) {
            return cid;
        }
        int len = parts.length;
        int from = beginIndex < 0 ? (len + beginIndex) : beginIndex;
        int to = endIndex <= 0 ? (len + endIndex) : endIndex;
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) {
                sb.append('.');
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    static boolean isCidToken(String token) {
        return Pattern.matches(CidToken.PATTERN, token);
    }

    public static void main(String[] args) throws Throwable {
        String valid = " { cid ( -2, -1 ) }";
        String invalid = " { cid( 0, -1) }";
        System.out.println(String.format("%20s\t\t%s", valid, isCidToken(valid) ? "Y" : "N"));
        System.out.println(String.format("%20s\t\t%s", invalid, isCidToken(invalid) ? "Y" : "N"));
        int[] idx = extractCidIndexes(invalid);
        System.out.println(cid("1.2.3.4.5.6", idx[0], idx[1]));
        System.out.println(cid("1.2.3", 0, 0));
    }

}
