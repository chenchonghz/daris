package daris.transcode.debabeler;

import arc.mf.plugin.Exec;

public class Debabeler {

    public static final String JAR = "loni-debabeler.jar";
    public static final String MAIN_CLASS = "edu.ucla.loni.debabel.events.engine.DebabelerEngine";
    private static long JAVA_XMX = 2000000000L;

    public static long javaXmx() {
        return JAVA_XMX;
    }

    public static String javaXmx(boolean mb) {
        StringBuilder sb = new StringBuilder();
        if (mb) {
            sb.append(JAVA_XMX / 1000000L);
            sb.append("m");
        } else {
            sb.append(JAVA_XMX);
        }
        return sb.toString();
    }

    public static void setJavaXmx(long sizeBytes) {
        if (sizeBytes < 512000000L) {
            JAVA_XMX = 512000000L;
        } else {
            JAVA_XMX = sizeBytes;
        }
    }

    public static void setJavaXmx(String value) {
        if (value.endsWith("M") || value.endsWith("m")) {
            long size = Long.parseLong(value.substring(0, value.length() - 1));
            setJavaXmx(size * 1000000L);
        } else if (value.endsWith("K") || value.endsWith("k")) {
            long size = Long.parseLong(value.substring(0, value.length() - 1));
            setJavaXmx(size * 1000L);
        } else {
            long size = Long.parseLong(value);
            setJavaXmx(size);
        }
    }

    public static String execute(String inputDir, String target,
            String mappingFile) throws Throwable {
        String[] jars = new String[] { Debabeler.JAR };
        String[] javaOptions = { "-Xmx" + javaXmx(true),
                "-Djava.awt.headless=true" };
        String[] args = { Debabeler.MAIN_CLASS, "-input", inputDir,
                "-suppress", "-target", target, "-mapping", mappingFile };
        return Exec.execJava(jars, javaOptions, args, null);
    }
}
