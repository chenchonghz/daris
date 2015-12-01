package daris.client.model.task;

import java.io.File;
import java.util.List;

import arc.file.matching.Profile;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;

public abstract class DataImportTask extends UploadTask {

    protected DataImportTask(String type, List<File> files) {
        super(type, files);
    }

    @Override
    protected final Profile profile() throws Throwable {
        return UploadFCP.getDataImportFCP();
    }

    protected abstract String serviceName();

    protected abstract void setServiceArgs(XmlWriter w) throws Throwable;

    @Override
    protected void doExecute() throws Throwable {
        setVariable("service", serviceName());
        XmlStringWriter w = new XmlStringWriter();
        w.push("args");
        setServiceArgs(w);
        w.pop();
        setVariable("args", w.document());
        super.doExecute();
    }

}
