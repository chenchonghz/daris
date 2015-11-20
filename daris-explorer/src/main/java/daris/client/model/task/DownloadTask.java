package daris.client.model.task;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.ServerClient;
import arc.mf.desktop.server.Session;
import daris.client.model.object.DObjectRef;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DownloadTask extends ObservableTask {

    public static final String TYPE_NAME = "daris.download";

    private DObjectRef _object;
    private DownloadOptions _options;

    private IntegerProperty _totalObjectsProperty;
    private IntegerProperty _processedObjectsProperty;
    private LongProperty _totalSizeProperty;
    private LongProperty _processedSizeProperty;
    private DoubleProperty _progressProperty;
    private StringProperty _messageProperty;
    private StringProperty _currentObject;
    private StringProperty _currentFile;

    public DownloadTask(DObjectRef object, DownloadOptions options) {
        super(null, TYPE_NAME);
        _object = object;
        _options = options == null ? new DownloadOptions() : options;
        _totalObjectsProperty = new SimpleIntegerProperty(this, "totalObjects");
        _processedObjectsProperty = new SimpleIntegerProperty(this,
                "processedObjects");
        _totalSizeProperty = new SimpleLongProperty(this, "totalSize");
        _processedSizeProperty = new SimpleLongProperty(this, "processedSize");
        _messageProperty = new SimpleStringProperty(this, "message");
        _currentObject = new SimpleStringProperty(this, "currentObject");
        _currentFile = new SimpleStringProperty(this, "currentFile");
    }

    private void setTotalObjects(int totalObjects) {
        Platform.runLater(() -> {
            _totalObjectsProperty.set(totalObjects);
        });
    }

    private void setProcessedObjects(int processedObjects) {
        Platform.runLater(() -> {
            _processedObjectsProperty.set(processedObjects);
        });
    }

    private void setTotalSize(long totalSize) {
        Platform.runLater(() -> {
            _totalSizeProperty.set(totalSize);
        });
    }

    private void setMessage(String message) {
        Platform.runLater(() -> {
            _messageProperty.set(message);
        });
    }

    private void setProgress(double progress) {
        Platform.runLater(() -> {
            _progressProperty.set(progress);
        });
    }

    @Override
    protected void doExecute() throws Throwable {
        ServerClient.Connection cxn = Session.connection();
        try {

            long totalSize = calcTotalSize(cxn);
            setTotalSize(totalSize);

            checkIfAborted();

            List<String> cids = new ArrayList<String>();
            if (_options.recursive()) {
                addObjects(cxn, cids);
                checkIfAborted();
            } else {
                cids.add(_object.citeableId());
            }
            setTotalObjects(cids.size());

            setProgress(0.01);

            for (String cid : cids) {
                downloadObject(cxn, cid);
            }

        } finally {
            cxn.close();
        }
    }

    private void downloadObject(ServerClient.Connection cxn, String cid)
            throws Throwable {
        // transcode?

        // asset.get?

        // decompress?
    }

    private void addObjects(ServerClient.Connection cxn, List<String> cids) {
        setMessage("Adding objects...");
        // TODO
    }

    private long calcTotalSize(ServerClient.Connection cxn) throws Throwable {
        setMessage("Calculating total size of object contents...");
        // TODO
        return 0L;
    }

}
