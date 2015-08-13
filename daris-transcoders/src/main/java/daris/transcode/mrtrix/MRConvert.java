package daris.transcode.mrtrix;

import java.io.File;

import arc.mf.plugin.Exec;

public class MRConvert {

    public static final String CMD = "mrconvert";

    public static String execute(File dir) throws Throwable {
        String inputDir = dir.getAbsolutePath();
        String outputFile = dir.getAbsolutePath() + "/mrconvert.nii";
        return Exec.exec(CMD, "-datatype int16 " + inputDir + " "
                + outputFile);
    }
    
    private MRConvert(){}

}
