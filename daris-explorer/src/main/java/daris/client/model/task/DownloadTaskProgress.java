package daris.client.model.task;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DownloadTaskProgress {

    SimpleObjectProperty<Double> progressProperty;

    IntegerProperty totalObjectsProperty;
    private int _totalObjects;

    IntegerProperty processedObjectsProperty;
    private int _processedObjects;

    StringProperty objectsProgressMessageProperty;

    LongProperty totalSizeProperty;
    private long _totalSize;

    LongProperty processedSizeProperty;
    private long _processedSize;

    StringProperty sizeProgressMessageProperty;

    LongProperty receivedSizeProperty;
    private long _receivedSize;

    StringProperty messageProperty;

    StringProperty currentObjectProperty;

    StringProperty currentOutputFileProperty;

    DownloadTaskProgress() {
        this.progressProperty = new SimpleObjectProperty<Double>(this,
                "progress");
        this.progressProperty.set(0.0);
        this.totalObjectsProperty = new SimpleIntegerProperty(this,
                "totalObjects");
        _totalObjects = 0;
        this.totalObjectsProperty.set(_totalObjects);
        this.processedObjectsProperty = new SimpleIntegerProperty(this,
                "processedObjects");
        _processedObjects = 0;
        this.processedObjectsProperty.set(_processedObjects);
        this.objectsProgressMessageProperty = new SimpleStringProperty(this,
                "objectsProgressProperty");
        this.totalSizeProperty = new SimpleLongProperty(this, "totalSize");
        _totalSize = 0L;
        this.totalSizeProperty.set(_totalSize);
        this.processedSizeProperty = new SimpleLongProperty(this,
                "processedSize");
        _processedSize = 0L;
        this.processedSizeProperty.set(_processedSize);
        this.sizeProgressMessageProperty = new SimpleStringProperty(this,
                "sizeProgressMessage");
        this.receivedSizeProperty = new SimpleLongProperty(this,
                "receivedSize");
        _receivedSize = 0L;
        this.receivedSizeProperty.set(_receivedSize);
        this.messageProperty = new SimpleStringProperty(this, "message");
        this.currentObjectProperty = new SimpleStringProperty(this,
                "currentObject");
        this.currentOutputFileProperty = new SimpleStringProperty(this,
                "currentOutputFile");
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> {
            this.progressProperty.set(progress);
        });
    }

    public void setTotalObjects(int totalObjects) {
        _totalObjects = totalObjects;
        Platform.runLater(() -> {
            this.totalObjectsProperty.set(_totalObjects);
            this.objectsProgressMessageProperty
                    .set(_processedObjects + "/" + _totalObjects);
        });
    }

    public void setProcessedObjects(int processedObjects) {
        _processedObjects = processedObjects;
        Platform.runLater(() -> {
            this.processedObjectsProperty.set(_processedObjects);
            this.objectsProgressMessageProperty
                    .set(_processedObjects + "/" + _totalObjects);
        });
    }

    public void incProcessedObjects() {
        _processedObjects++;
        setProcessedObjects(_processedObjects);
    }

    public void setTotalSize(long totalSize) {
        _totalSize = totalSize;
        Platform.runLater(() -> {
            this.totalSizeProperty.set(_totalSize);
            this.sizeProgressMessageProperty
            .set(_processedSize + "/" + _totalSize);
        });
    }

    public void setProcessedSize(long processedSize) {
        _processedSize = processedSize;
        Platform.runLater(() -> {
            this.processedSizeProperty.set(_processedSize);
            this.sizeProgressMessageProperty
                    .set(_processedSize + "/" + _totalSize);
        });
    }

    public void incProcessedSize(long increment) {
        if (increment > 0) {
            long processedSize = _processedSize + increment;
            setProcessedSize(processedSize);
            if (_totalSize > 0) {
                setProgress((double) processedSize / (double) _totalSize);
            }
        }
    }

    public void setReceivedSize(long receivedSize) {
        _receivedSize = receivedSize;
        Platform.runLater(() -> {
            this.receivedSizeProperty.set(_receivedSize);
        });
    }

    public void incReceivedSize(long increment) {
        setReceivedSize(_receivedSize + increment);
    }

    public void setMessage(String message) {
        Platform.runLater(() -> {
            this.messageProperty.set(message);
        });
    }

    public void setCurrentObject(String id) {
        Platform.runLater(() -> {
            this.currentObjectProperty.set(id);
        });
    }

    public void setCurrentOutputFile(String file) {
        Platform.runLater(() -> {
            this.currentOutputFileProperty.set(file);
        });
    }

}
