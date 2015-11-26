package daris.client.model.task;

public class DownloadTaskManager extends ObservableTaskManager<DownloadTask> {

    DownloadTaskManager() {
        super();
    }

    private static DownloadTaskManager _instance;

    public static DownloadTaskManager get() {
        if (_instance == null) {
            _instance = new DownloadTaskManager();
        }
        return _instance;
    }

}
