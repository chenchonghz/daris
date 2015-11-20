package daris.client.model.task;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import daris.client.model.transcode.Transcode;

public class DownloadOptions {

    public static enum CollisionPolicy {
        SKIP, OVERWRITE, RENAME
    }

    private boolean _recursive;
    private boolean _decompress;
    private Set<Transcode> _transcodes;
    private File _directory;

    public boolean recursive() {
        return _recursive;
    }

    public DownloadOptions setRecursive(boolean recursive) {
        _recursive = recursive;
        return this;
    }

    public boolean decompress() {
        return _decompress;
    }

    public DownloadOptions setDecompress(boolean decompress) {
        _decompress = decompress;
        return this;
    }

    public Set<Transcode> transcodes() {
        if (_transcodes == null) {
            return null;
        } else {
            return Collections.unmodifiableSet(_transcodes);
        }
    }

    public DownloadOptions addTranscode(Transcode transcode) {
        if (_transcodes == null) {
            _transcodes = new HashSet<Transcode>();
        }
        _transcodes.add(transcode);
        return this;
    }

    public DownloadOptions removeTranscode(Transcode transcode) {
        if (_transcodes != null) {
            _transcodes.remove(transcode);
        }
        return this;
    }

    public DownloadOptions setTranscodes(Collection<Transcode> transcodes) {
        if (_transcodes != null) {
            _transcodes.clear();
        }
        if (transcodes != null) {
            for (Transcode transcode : transcodes) {
                addTranscode(transcode);
            }
        }
        return this;
    }

    public File directory() {
        return _directory;
    }

    public DownloadOptions setDirectory(File dir) {
        _directory = dir;
        return this;
    }
}
