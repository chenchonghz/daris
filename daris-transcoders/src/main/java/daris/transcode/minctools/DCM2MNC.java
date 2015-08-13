package daris.transcode.minctools;

import java.io.File;

import arc.mf.plugin.Exec;

public class DCM2MNC {

    public static final String CMD = "dcm2mnc";

    public static String execute(File dir) throws Throwable {
        StringBuilder args = new StringBuilder();
        args.append("-usecoordinates -dname . ");
        File[] inputFiles = dir.listFiles();
        for (File inputFile : inputFiles) {
            args.append(" ");
            args.append(inputFile.getAbsolutePath());
        }
        args.append(" ");
        args.append(dir.getAbsolutePath());
        return Exec.exec(CMD, args.toString());
    }

    private DCM2MNC() {
    }

}
