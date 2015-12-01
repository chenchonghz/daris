package daris.client.model.task;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.file.matching.Construct;
import arc.file.matching.FileSystemCompiler;
import arc.file.matching.MultiFileIterator;
import arc.file.matching.Profile;
import arc.mf.client.ServerClient;
import arc.mf.client.agent.modules.asset.AssetImportTask.FileState;
import arc.mf.client.agent.task.Monitor;
import arc.mf.client.file.os.FileStatisticsTask;
import arc.mf.client.util.FileImport;
import arc.mf.desktop.server.Session;
import arc.utils.FileUtil;
import daris.client.settings.UploadSettings;

public abstract class UploadTask extends ObservableTask
        implements FileSystemCompiler.Listener, Monitor {

    public static final String TYPE_NAME = "daris.upload";

    private List<File> _files;
    private FileImport.Options _importOptions;
    private List<FileStatisticsTask> _stats;
    private UploadTaskProgress _progress;

    protected UploadTask(String type, List<File> files) {
        super(null, type == null ? TYPE_NAME : type);
        _files = new ArrayList<File>();
        if (files != null) {
            _files.addAll(files);
        }
        _importOptions = new FileImport.Options();
        _progress = new UploadTaskProgress();

    }

    protected boolean managed() {
        return true;
    }

    public UploadTask setName(String name) {
        _importOptions.setName(name);
        return this;
    }

    public UploadTask setDescription(String description) {
        _importOptions.setDescription(description);
        return this;
    }

    public UploadTask setComment(String comment) {
        _importOptions.setComment(comment);
        return this;
    }

    public UploadTask setAllowIncompleteMeta(boolean allowIncompleteMeta) {
        _importOptions.setAllowIncompleteMeta(allowIncompleteMeta);
        return this;
    }

    public UploadTask setAllowInvalidMeta(boolean allowInvalidMeta) {
        _importOptions.setAllowInvalidMeta(allowInvalidMeta);
        return this;
    }

    public UploadTask setVariables(Map<String, String> variables) {
        _importOptions.setVariables(variables);
        return this;
    }

    public UploadTask setVariables(String[] variables) {
        if (variables == null || variables.length == 0) {
            return this;
        }
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < variables.length; i += 2) {
            map.put(variables[i], variables[i + 1]);
        }
        _importOptions.setVariables(map);
        return this;
    }

    public UploadTask setVariable(String name, String value) {
        _importOptions.setVariableValue(name, value);
        return this;
    }

    protected abstract Profile profile() throws Throwable;

    @Override
    protected void doExecute() throws Throwable {
        if (_files == null || _files.isEmpty()) {
            return;
        }
        if (aborted()) {
            return;
        }
        /*
         * start file statistics tasks
         */
        _stats = new ArrayList<FileStatisticsTask>(_files.size());
        for (File f : _files) {
            FileStatisticsTask st = new FileStatisticsTask(this, sessionId(),
                    f);
            st.submit(0L);
            _stats.add(st);
        }
        /*
         * retrieve the file compilation profile from the server.
         */
        Profile fcp = profile();
        /*
         * create file import task
         */
        FileImport fi;
        if (_files.size() > 1) {
            FileSystemCompiler fsc = new FileSystemCompiler(
                    FileUtil.commonRoot(_files), fcp,
                    UploadSettings.DEFAULT_MAX_NUM_OF_CONCURRENT_REQUESTS);
            fsc.addListener(this);
            fi = new FileImport(fsc, _importOptions,
                    new MultiFileIterator(_files));
        } else {
            FileSystemCompiler fsc = new FileSystemCompiler(_files.get(0), fcp,
                    UploadSettings.DEFAULT_MAX_NUM_OF_CONCURRENT_REQUESTS);
            fsc.addListener(this);
            fi = new FileImport(fsc, _importOptions);
        }
        /*
         * execute file import task
         */
        monitor(500L, this); // starts monitor
        ServerClient.Connection cxn = Session.connection();
        try {
            fi.execute(cxn);
        } finally {
            cxn.close();
        }
    }

    @Override
    protected void updateProgress() {
        if (_stats == null) {
            return;
        }
        for (final FileStatisticsTask stat : this._stats) {
            _progress.incTotalFiles(stat.nbCanRead());
            _progress.incTotalSize(stat.totalSize());
        }
    }

    @Override
    public void analyzing(Construct c, String path, long nbBytes) {
        _progress.setCurrentFile(path, nbBytes, FileState.ANALYZING);
    }

    @Override
    public void consumed(Construct c, String path, long nbBytes) {
        _progress.incProcessedFiles();
    }

    @Override
    public void consuming(Construct c, String path, long nbBytes) {
        _progress.setCurrentFile(path, nbBytes, FileState.CONSUMING);
        // _progress.setCurrentFileProcessedSize(0L);
    }

    @Override
    public void ignored(Construct c, String path) {
        _progress.incIgnoredFiles();
    }

    @Override
    public void retry(Construct c, String path) {
        setState(State.RUNNING);
    }

    @Override
    public void retryWait(Construct c, String path, Throwable t) {
        failedWillRetry(t);
    }

    @Override
    public void skipped(Construct c, String path, long nbBytes) {
        _progress.setCurrentFile(path, nbBytes, FileState.SKIPPED);
        _progress.incProcessedFiles();
        _progress.incProcessedSize(nbBytes);
        _progress.incSkippedFiles();
        // _progress.incCurrentFileProcessedSize(nbBytes);
    }

    @Override
    public void transmitting(long nbBytes) {
        _progress.incProcessedSize(nbBytes);
        // _progress.incCurrentFileProcessedSize(nbBytes);
    }

    @Override
    public void waiting(Construct c, String path, long nbBytes) {
        _progress.setCurrentFile(path, nbBytes, FileState.WAITING);
    }

    @Override
    public void monitor() {
        // DO NOTHING
        // but it is required to call updateProgress() method in the super
        // class.
    }

    @Override
    public void finished(State state) {
        // DO NOTHING
        // but it is required to call updateProgress() method in the super
        // class.
    }

    public void start() {
        if (managed()) {
            UploadTaskManager.get().addTask(this);
        } else {
            submit();
        }
    }

    @Override
    public boolean discard() {
        if (managed()) {
            UploadTaskManager.get().removeTask(this);
        }
        return super.discard();
    }

}
