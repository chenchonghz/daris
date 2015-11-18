package daris.client.task;

import java.util.List;

import arc.mf.client.agent.modules.asset.AssetContentKey;
import arc.mf.client.agent.modules.asset.AssetDownloadTask;
import arc.mf.client.file.LocalOSFile;
import arc.mf.model.asset.task.AssetDownloadControls;

public class DownloadTask extends AssetDownloadTask {

    public DownloadTask(List<AssetContentKey> ids,
            final AssetDownloadControls dc, final LocalOSFile to) {
        super(ids, dc, to);
        // TODO
    }

}