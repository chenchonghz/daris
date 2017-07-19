package daris.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ThrowableUtils {

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            e.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

}
