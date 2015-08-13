package daris.transcode.pvconv;

import java.io.File;

import arc.mf.plugin.Exec;

public class PVCONV {

    public static final String SCRIPT = "pvconv.pl";

    public enum Target {
        ANALYZE_NL, ANALYZE_RL, MINC
    }

    private PVCONV() {

    }

    public static String execute(String inDir, Target target, String outDir)
            throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(inDir);
        switch (target) {
        case ANALYZE_NL:
            sb.append(" -noradio");
            break;
        case ANALYZE_RL:
            sb.append(" -radio");
            break;
        case MINC:
            sb.append(" -outtype minc");
            break;
        default:
            break;
        }
        sb.append(" -outdir ");
        sb.append(outDir);
        return Exec.exec(SCRIPT, sb.toString());
    }

    public static String execute(File dir, Target target) throws Throwable {
        return execute(dir.getAbsolutePath(), target, dir.getAbsolutePath());
    }

}
