package daris.client.model.dataset.task;

import java.io.File;
import java.util.List;

import arc.xml.XmlWriter;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.DataImportTask;

public class PrimaryDataSetCreateTask extends DataImportTask {

    public static final String TYPE_NAME = "dataset.primary.create";

    private String _pid;

    protected PrimaryDataSetCreateTask(String pid, List<File> files) {
        super(TYPE_NAME, files);
        _pid = pid;
    }

    public PrimaryDataSetCreateTask(DObjectRef o, List<File> files) {
        this(o.citeableId(), files);
    }

    @Override
    protected String serviceName() {
        return "om.pssd.dataset.primary.create";
    }

    @Override
    protected void setServiceArgs(XmlWriter w) throws Throwable {
        w.add("pid", _pid);
        // TODO Auto-generated method stub

    }

}
