package nig.mf.plugin.pssd.util;

import java.util.Collection;
import java.util.Iterator;

public class StringUtil {

    public static String join(Collection<String> ss, char c) {
        if (ss == null || ss.isEmpty()) {
            return "";
        }
        if (ss.size() == 1) {
            return ss.iterator().next();
        }
        Iterator<String> it = ss.iterator();
        StringBuilder sb = new StringBuilder(it.next());
        while (it.hasNext()) {
            sb.append(c).append(it.next());
        }
        return sb.toString();
    }

}
