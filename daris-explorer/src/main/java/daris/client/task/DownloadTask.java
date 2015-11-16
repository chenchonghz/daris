package daris.client.task;

import arc.mf.client.agent.modules.asset.AssetContentKey;
import arc.mf.client.agent.modules.asset.AssetDownloadTask;
import arc.mf.client.file.LocalOSFile;
import arc.mf.model.asset.task.AssetDownloadControls;

public class DownloadTask extends AssetDownloadTask {

    public DownloadTask(AssetContentKey id, AssetDownloadControls dc,
            LocalOSFile to) {
        super(id, dc, to);
        // TODO Auto-generated constructor stub
    }

}
