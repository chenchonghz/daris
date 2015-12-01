package daris.client.model.dataset.task;

import java.io.File;
import java.util.List;

import arc.xml.XmlWriter;
import daris.client.model.task.DataImportTask;

public class PrimaryDataSetCreateTask extends DataImportTask {

    public static final String TYPE_NAME = "dataset.primary.create";

    protected PrimaryDataSetCreateTask(List<File> files) {
        super(TYPE_NAME, files);
    }

    @Override
    protected String serviceName() {
        return "om.pssd.dataset.primary.create";
    }

    @Override
    protected void setServiceArgs(XmlWriter w) throws Throwable {
        // TODO Auto-generated method stub

    }

}
