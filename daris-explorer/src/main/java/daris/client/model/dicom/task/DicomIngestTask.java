package daris.client.model.dicom.task;

import java.io.File;
import java.util.List;

import arc.file.matching.Profile;
import daris.client.model.task.UploadFCP;
import daris.client.model.task.UploadTask;

public class DicomIngestTask extends UploadTask {

    protected DicomIngestTask(List<File> files) {
        super("dicom.ingest", files);
    }

    @Override
    protected final Profile profile() throws Throwable {
        return UploadFCP.getDicomIngestFCP();
    }

}
