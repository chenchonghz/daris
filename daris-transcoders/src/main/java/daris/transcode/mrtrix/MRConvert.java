package daris.transcode.mrtrix;

import java.io.File;

import arc.mf.plugin.Exec;

public class MRConvert {

    public static final String CMD = "mrconvert";

    public static String convertToNifti(File dir) throws Throwable {
        String inputDir = dir.getAbsolutePath();
        String outputFile = dir.getAbsolutePath() + "/mrconvert.nii";
        return Exec.exec(CMD, "-datatype int16 " + inputDir + " " + outputFile);
    }

    public static String convertToAnalyzeNL(File dir) throws Throwable {
        String inputDir = dir.getAbsolutePath();
        String outputFile = dir.getAbsolutePath() + "/mrconvert.img";
        return Exec.exec(CMD, inputDir + " " + outputFile);
    }

    private MRConvert() {
    }

}
