package daris.client.model.task;

import java.io.File;
import java.util.List;

import arc.file.matching.Profile;

public class DataImportTask extends UploadTask {

    protected DataImportTask(String type, List<File> files) {
        super(type, files);
    }

    @Override
    protected final Profile profile() throws Throwable {
        return UploadFCP.getDataImportFCP();
    }

}
