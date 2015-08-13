package daris.transcode.dcmtools;

import java.io.File;
import java.util.Map;

import nig.dicom.siemens.CSAFileUtils;
import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class Dicom2SiemensRDA extends DarisTranscodeImpl {

    Dicom2SiemensRDA() {
    }

    @Override
    protected void transcode(File dir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        File[] inputFiles = dir.listFiles();
        // Siemens DICOM -> Siemens RDA
        for (File inputFile : inputFiles) {
            String rdaFileName = inputFile.getName();
            if (rdaFileName.indexOf(".") != -1) {
                rdaFileName = rdaFileName.substring(0,
                        rdaFileName.lastIndexOf("."));
            }
            rdaFileName = rdaFileName + ".rda";
            File rdaFile = new File(dir, rdaFileName);
            if (CSAFileUtils.isCSADicomFile(inputFile)) {
                CSAFileUtils.convertToSiemensRDA(inputFile, rdaFile);
            }
        }
    }

    @Override
    protected boolean resourceManaged() {
        // it is fairly light weight and don't need any careful resource
        // management
        return false;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.DICOM_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.SIEMENS_RDA;
    }

    @Override
    public DarisTranscodeProvider provider() {
        return DCMToolsTranscodeProvider.INSTANCE;
    }

}
