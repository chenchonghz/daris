package daris.client.task;

import java.util.List;

import arc.mf.client.agent.modules.asset.AssetImportTask;
import arc.mf.client.file.LocalFile;
import arc.mf.model.asset.AssetRef;
import arc.mf.model.asset.task.AssetImportControls;

public class UploadTask extends AssetImportTask {

    public UploadTask(AssetRef a, List<LocalFile> files,
            AssetImportControls ic, long estSize) {
        super(a, files, ic, estSize);
        // TODO Auto-generated constructor stub
    }

}
