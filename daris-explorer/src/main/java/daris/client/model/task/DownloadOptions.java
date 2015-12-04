package daris.client.model.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import arc.mf.client.util.UnhandledException;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.transcode.Transcode;
import daris.client.settings.DownloadSettings;
import daris.client.settings.UserSettingsRef;

public class DownloadOptions {

    public static enum Parts {
        meta, content, all;
        public static Parts fromString(String s, Parts defaultValue) {
            if (s != null) {
                if (s.equalsIgnoreCase(meta.name())) {
                    return meta;
                }
                if (s.equalsIgnoreCase(content.name())) {
                    return content;
                }
                if (s.equalsIgnoreCase(all.name())) {
                    return all;
                }
            }
            return defaultValue;
        }
    }

    private boolean _recursive;
    private boolean _decompress;
    private Parts _parts;
    private Map<String, Transcode> _transcodes;
    private DownloadCollisionPolicy _collisionPolicy;
    private String _directory;

    public DownloadOptions() {
        _recursive = false;
        _decompress = false;
        _parts = Parts.all;
        _transcodes = new HashMap<String, Transcode>();
        _collisionPolicy = DownloadCollisionPolicy.OVERWRITE;
        _directory = DownloadSettings.getDefaultDirectory();
    }

    public DownloadOptions(DownloadSettings settings) {
        _recursive = settings.recursive();
        _decompress = settings.decompress();
        _parts = settings.parts();
        if (settings.hasTranscodes()) {
            Set<Transcode> transcodes = settings.transcodes();
            for (Transcode transcode : transcodes) {
                addTranscode(transcode);
            }
        }
        _collisionPolicy = settings.collisionPolicy();
        _directory = settings.directory();
    }

    public Parts parts() {
        return _parts;
    }

    public void setParts(Parts parts) {
        _parts = parts;
    }

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
        if (_transcodes == null || _transcodes.isEmpty()) {
            return null;
        } else {
            return new HashSet<Transcode>(_transcodes.values());
        }
    }

    public DownloadOptions addTranscode(Transcode transcode) {
        if (_transcodes == null) {
            _transcodes = new HashMap<String, Transcode>();
        }
        _transcodes.put(transcode.fromMimeType, transcode);
        return this;
    }

    public DownloadOptions removeTranscode(String fromMimeType) {
        if (_transcodes != null) {
            _transcodes.remove(fromMimeType);
        }
        return this;
    }

    public DownloadOptions removeTranscode(Transcode transcode) {
        return removeTranscode(transcode.fromMimeType);
    }

    public boolean hasTranscodes() {
        return _transcodes != null && !_transcodes.isEmpty();
    }

    public Transcode transcodeFor(String fromMimeType) {
        if (hasTranscodes()) {
            return _transcodes.get(fromMimeType);
        } else {
            return null;
        }
    }

    public boolean hasTranscodeFor(String fromMimeType) {
        return hasTranscodes() && _transcodes.containsKey(fromMimeType);
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

    public DownloadCollisionPolicy collisionPolicy() {
        return _collisionPolicy;
    }

    public DownloadOptions setCollisionPolicy(
            DownloadCollisionPolicy collisionPolicy) {
        _collisionPolicy = collisionPolicy;
        return this;
    }

    public String directory() {
        return _directory;
    }

    public DownloadOptions setDirectory(String dir) {
        _directory = dir;
        return this;
    }

    public void saveToUserSettings() {
        UserSettingsRef.get().resolve(settings -> {
            settings.setDownloadSettings(DownloadOptions.this);
            try {
                settings.save();
            } catch (Throwable e) {
                UnhandledException.report("Saving user download settings", e);
            }
        });
    }

    public static void loadFromUserSettings(
            ObjectResolveHandler<DownloadOptions> rh) {
        UserSettingsRef.get().resolve(settings -> {
            if (rh != null) {
                rh.resolved(new DownloadOptions(settings.downloadSettings()));
            }
        });
    }
}
