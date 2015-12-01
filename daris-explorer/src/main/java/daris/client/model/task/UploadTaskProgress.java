package daris.client.model.task;

import arc.mf.client.agent.modules.asset.AssetImportTask.FileState;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UploadTaskProgress {
    LongProperty totalFilesProperty;
    long _totalFiles = -1L;
    LongProperty totalSizeProperty;
    long _totalSize = -1L;
    LongProperty processedFilesProperty;
    long _processedFiles = 0L;
    LongProperty processedSizeProperty;
    long _processedSize = 0L;
    LongProperty skippedFilesProperty;
    long _skippedFiles = 0L;
    LongProperty ignoredFilesProperty;
    long _ignoredFiles = 0L;
    StringProperty processedFilesMessageProperty;
    StringProperty processedSizeMessageProperty;
    ObjectProperty<Double> progressProperty;
    StringProperty messageProperty;
    StringProperty currentFilePathProperty;
    LongProperty currentFileSizeProperty;
    ObjectProperty<FileState> currentFileStateProperty;

    UploadTaskProgress() {
        _totalFiles = -1L;
        this.totalFilesProperty = new SimpleLongProperty(this, "totalFiles");
        this.totalFilesProperty.set(_totalFiles);

        _totalSize = -1L;
        this.totalSizeProperty = new SimpleLongProperty(this, "totalSize");
        this.totalSizeProperty.set(_totalSize);

        _processedFiles = 0L;
        this.processedFilesProperty = new SimpleLongProperty(this,
                "processedFiles");
        this.processedFilesProperty.set(_processedFiles);

        _processedSize = 0L;
        this.processedSizeProperty = new SimpleLongProperty(this,
                "processedSize");
        this.processedSizeProperty.set(_processedSize);

        _skippedFiles = 0L;
        this.skippedFilesProperty = new SimpleLongProperty(this,
                "skippedFiles");
        this.skippedFilesProperty.set(_skippedFiles);

        _ignoredFiles = 0L;
        this.ignoredFilesProperty = new SimpleLongProperty(this,
                "ignoredFiles");
        this.ignoredFilesProperty.set(_ignoredFiles);

        this.processedFilesMessageProperty = new SimpleStringProperty(this,
                "processedFilesMessage");
        this.processedSizeMessageProperty = new SimpleStringProperty(this,
                "processedSizeMessage");

        this.progressProperty = new SimpleObjectProperty<Double>(this,
                "progress");
        this.progressProperty.set(0.0);

        this.messageProperty = new SimpleStringProperty(this,
                "messageProperty");

        this.currentFilePathProperty = new SimpleStringProperty(this,
                "currentFilePathProperty");

        this.currentFileSizeProperty = new SimpleLongProperty(this,
                "currentFileSizeProperty");

        this.currentFileStateProperty = new SimpleObjectProperty<FileState>(
                this, "currentFileStateProperty");
    }

    public synchronized void setTotalFiles(long totalFiles) {
        _totalFiles = totalFiles;
        Platform.runLater(() -> {
            this.totalFilesProperty.set(_totalFiles);
            this.processedFilesMessageProperty
                    .set(_processedFiles + "/" + _totalFiles);
        });
    }

    public synchronized void setTotalSize(long totalSize) {
        _totalSize = totalSize;
        Platform.runLater(() -> {
            this.totalSizeProperty.set(_totalSize);
            this.processedSizeMessageProperty
                    .set(_processedSize + "/" + _totalSize);
        });
    }

    public synchronized void setProcessedFiles(long processedFiles) {
        _processedFiles = processedFiles;
        Platform.runLater(() -> {
            this.processedFilesProperty.set(_processedFiles);
            this.processedFilesMessageProperty
                    .set(_processedFiles + "/" + _totalFiles);
        });
    }

    public void incProcessedFiles() {
        _processedFiles++;
        setProcessedFiles(_processedFiles);
    }

    public synchronized void setProcessedSize(long processedSize) {
        _processedSize = processedSize;
        Platform.runLater(() -> {
            this.processedSizeProperty.set(_processedSize);
            this.processedSizeMessageProperty
                    .set(_processedSize + "/" + _totalSize);
            this.setProgress(_processedSize / _totalSize);
        });
    }

    public void incProcessedSize(long inc) {
        setProcessedSize(_processedSize + inc);
    }

    public synchronized void incSkippedFiles() {
        _skippedFiles++;
        Platform.runLater(() -> {
            this.skippedFilesProperty.set(_skippedFiles);
        });
    }

    public synchronized void setProgress(double progress) {
        Platform.runLater(() -> {
            this.progressProperty.set(progress);
        });
    }

    public synchronized void setMessage(String msg) {
        Platform.runLater(() -> {
            this.messageProperty.set(msg);
        });
    }

    public synchronized void setCurrentFile(String path, long size,
            FileState state) {
        Platform.runLater(() -> {
            this.currentFilePathProperty.set(path);
            this.currentFileSizeProperty.set(size);
            this.currentFileStateProperty.set(state);
        });
    }

    public synchronized void incIgnoredFiles() {
        _ignoredFiles++;
        Platform.runLater(() -> {
            this.ignoredFilesProperty.set(_ignoredFiles);
        });
    }

}
