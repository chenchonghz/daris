package daris.client.model.task;

public class UploadTaskManager extends ObservableTaskManager<UploadTask> {

    UploadTaskManager() {
        super();
    }

    private static UploadTaskManager _instance;

    public static UploadTaskManager get() {
        if (_instance == null) {
            _instance = new UploadTaskManager();
        }
        return _instance;
    }

}
