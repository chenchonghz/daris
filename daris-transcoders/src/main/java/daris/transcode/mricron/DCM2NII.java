package daris.transcode.mricron;

import java.io.File;

import arc.mf.plugin.Exec;

public class DCM2NII {

    public static final String CMD = "dcm2nii";

    public static String execute(File dir) throws Throwable {
        String inputDir = dir.getAbsolutePath();
        String outputDir = inputDir;
        return Exec.exec(CMD, "-v y -o " + outputDir + " " + inputDir);
    }

    private DCM2NII() {

    }

}
