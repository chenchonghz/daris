package daris.client.model.collection.download;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Parts;

public class DownloaderSettings {

    private String _manifestName;
    private String _manifestDesc;
    private List<String> _queries;
    private Set<String> _cids;

    private Parts _parts;
    private String _outputPattern;
    private Map<String, String> _transcodes;
    private boolean _unarchive;

    private boolean _generateToken;
    private String _tokenPassword;
    private Date _tokenExpiry;
    private int _tokenUseCount = 0;

    private TargetPlatform _platform;

    public DownloaderSettings() {

        _parts = Parts.content;
        _unarchive = false;
        _generateToken = true;
        _platform = TargetPlatform.JAVA;
    }

    public void setManifestName(String manifestName) {
        _manifestName = manifestName;
    }

    public void setManifestDescription(String manifestDescription) {
        _manifestDesc = manifestDescription;
    }

    public void setQueries(String... where) {
        if (_queries != null) {
            _queries.clear();
        } else {
            _queries = new ArrayList<String>();
        }
        if (where != null) {
            for (String q : where) {
                _queries.add(q);
            }
        }
    }

    public List<String> queries() {
        return _queries;
    }

    public String manifestName() {
        return _manifestName;
    }

    public String manifestDescription() {
        return _manifestDesc;
    }

    public void addObject(DObjectRef o) {
        if (_cids == null) {
            _cids = new LinkedHashSet<String>();
        }
        _cids.add(o.id());
    }

    public Collection<String> cids() {
        return _cids;
    }

    public Parts parts() {
        return _parts;
    }

    public void setParts(Parts parts) {
        _parts = parts;
    }

    public String outputPattern() {
        return _outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        _outputPattern = outputPattern;
    }

    public void addTranscode(String from, String to) {
        if (_transcodes == null) {
            _transcodes = new LinkedHashMap<String, String>();
        }
        _transcodes.put(from, to);
    }

    public void removeTranscode(String from) {
        if (_transcodes != null) {
            _transcodes.remove(from);
        }
    }

    public Map<String, String> transcodes() {
        return _transcodes;
    }

    public String transcodeFor(String from) {
        if (_transcodes != null) {
            return _transcodes.get(from);
        }
        return null;
    }

    public boolean unarchive() {
        return _unarchive;
    }

    public void setUnarchive(boolean unarchive) {
        _unarchive = unarchive;
    }

    public boolean generateToken() {
        return _generateToken;
    }

    public void setGenerateToken(boolean generateToken) {
        _generateToken = generateToken;
    }

    public Date tokenExpiry() {
        return _tokenExpiry;
    }

    public void setTokenExpiry(Date expiry) {
        _tokenExpiry = expiry;
    }

    public String tokenPassword() {
        return _tokenPassword;
    }

    public void setTokenPassword(String tokenPassword) {
        _tokenPassword = tokenPassword;
    }

    public int tokenUseCount() {
        return _tokenUseCount;
    }

    public void setTokenUseCount(int tokenUseCount) {
        _tokenUseCount = tokenUseCount;
    }

    public TargetPlatform targetPlatform() {
        return _platform;
    }

    public void setTargetPlatform(TargetPlatform platform) {
        _platform = platform;
    }

    public void save(XmlWriter w) {
        if (_manifestName != null) {
            w.add("name", _manifestName);
        }
        if (_manifestDesc != null) {
            w.add("description", _manifestDesc);
        }
        if (_queries != null) {
            for (String where : _queries) {
                w.add("where", where);
            }
        }
        if (_cids != null) {
            for (String cid : _cids) {
                w.add("cid", cid);
            }
        }
        if (_parts != null) {
            w.add("parts", _parts);
        }
        if (_outputPattern != null) {
            w.add("output-pattern", _outputPattern);
        }
        if (_transcodes != null) {
            Set<String> froms = _transcodes.keySet();
            for (String from : froms) {
                String to = _transcodes.get(from);
                w.push("transcode");
                w.add("from", from);
                w.add("to", to);
                w.pop();
            }
        }
        w.add("unarchive", _unarchive);
        if (_generateToken) {
            w.push("token");
            if (_tokenPassword != null) {
                w.add("password", _tokenPassword);
            }
            if (_tokenExpiry != null) {
                w.addDateOnly("to", _tokenExpiry);
            }
            if (_tokenUseCount > 0) {
                w.add("use-count", _tokenUseCount);
            }
            w.pop();
        }
        w.add("platform", _platform.name().toLowerCase());
    }
}
