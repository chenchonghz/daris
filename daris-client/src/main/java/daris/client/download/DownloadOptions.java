package daris.client.download;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DownloadOptions {

    public static enum Parts {
        META, CONTENT, ALL;

        public static Parts fromString(String s, Parts def)
                throws IllegalArgumentException {
            if (s == null || s.trim().isEmpty()) {
                return def;
            }
            if (s.equalsIgnoreCase(META.toString())) {
                return META;
            }
            if (s.equalsIgnoreCase(CONTENT.toString())) {
                return CONTENT;
            }
            if (s.equalsIgnoreCase(ALL.toString())) {
                return ALL;
            }
            throw new IllegalArgumentException("Failed parse parts: " + s);
        }
    }

    private boolean _recursive;
    private File _outputDir;
    private boolean _includeAttachments;
    private boolean _decompress;
    private boolean _datasetOnly;
    private Parts _parts;
    private Map<String, String> _transcodes;
    private boolean _overwrite;
    private String _filter;

    public DownloadOptions(boolean recursive, File outputDir,
            boolean includeAttachments, boolean decompress, boolean datasetOnly,
            Parts parts, Map<String, String> transcodes, boolean overwrite,
            String filter) {
        _recursive = recursive;
        _outputDir = outputDir;
        _includeAttachments = includeAttachments;
        _decompress = decompress;
        _datasetOnly = datasetOnly;
        _parts = parts;
        _transcodes = transcodes;
        _overwrite = overwrite;
    }

    public DownloadOptions() {
        this(true, new File(System.getProperty("user.dir")), false, true, false,
                Parts.CONTENT, null, true, null);
    }

    public File outputDir() {
        return _outputDir;
    }

    public void setOutputDir(File outputDir) {
        _outputDir = outputDir;
    }

    public String filter() {
        return _filter;
    }

    public void setFilter(String filter) {
        _filter = filter;
    }

    public String transcode(String from) {
        if (_transcodes != null) {
            return _transcodes.get(from);
        }
        return null;
    }

    public boolean hasTranscode(String from) {
        return _transcodes != null && _transcodes.containsKey(from);
    }

    public void addTranscode(String from, String to) {
        if (_transcodes == null) {
            _transcodes = new HashMap<String, String>();
        }
        _transcodes.put(from, to);
    }

    public void removeTranscode(String from) {
        if (_transcodes != null) {
            _transcodes.remove(from);
        }
    }

    public Map<String, String> transcodes() {
        if (_transcodes == null) {
            return null;
        }
        return Collections.unmodifiableMap(_transcodes);
    }

    public boolean hasTranscodes() {
        return _transcodes != null && !_transcodes.isEmpty();
    }

    public void setTranscodes(Map<String, String> transcodes) {
        _transcodes = transcodes;
    }

    public boolean recursive() {
        return _recursive;
    }

    public void setRecursive(boolean recursive) {
        _recursive = recursive;
    }

    public boolean decompress() {
        return _decompress;
    }

    public void setDecompress(boolean decompress) {
        _decompress = decompress;
    }

    public boolean datasetOnly() {
        return _datasetOnly;
    }

    public void setDatasetOnly(boolean datasetOnly) {
        _datasetOnly = datasetOnly;
    }

    public boolean overwrite() {
        return _overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        _overwrite = overwrite;
    }

    public Parts parts() {
        return _parts;
    }

    public void setParts(Parts parts) {
        _parts = parts;
    }

    public boolean includeAttachments() {
        return _includeAttachments;
    }

    public void setIncludeAttachments(boolean includeAttachments) {
        _includeAttachments = includeAttachments;
    }
}
