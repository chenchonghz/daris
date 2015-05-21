package daris.client.model.query.filter.mf;

import arc.mf.client.util.ObjectUtil;
import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.object.ObjectResolveHandler;

public class MetadataPath {

    private String _path;
    private MetadataDocumentRef _doc;
    private arc.mf.xml.defn.Node _node;

    public MetadataPath(String path) {
        _path = path;
        _doc = new MetadataDocumentRef(documentPath(path));
        _node = null;
    }

    public MetadataPath(arc.mf.xml.defn.Node n) {
        _path = n.path();
        _doc = new MetadataDocumentRef(documentPath(n.path()));
        _node = n;
    }

    public MetadataPath(MetadataDocumentRef doc) {
        _path = doc.path();
        _doc = doc;
        _node = null;
    }

    public String path() {
        return _path;
    }

    public MetadataDocumentRef document() {
        return _doc;
    }

    public boolean resolved() {
        if (documentOnly()) {
            return _doc != null;
        } else {
            return _node != null;
        }
    }

    public arc.mf.xml.defn.Node node() {
        return _node;
    }

    public void resolveNode(final ObjectResolveHandler<arc.mf.xml.defn.Node> rh) {
        if (documentOnly()) {
            _node = null;
            rh.resolved(_node);
            return;
        }
        if (_node != null) {
            rh.resolved(_node);
            return;
        }
        _doc.resolve(new ObjectResolveHandler<MetadataDocument>() {
            @Override
            public void resolved(MetadataDocument md) {
                if (rh != null) {
                    rh.resolved(md.definition().findByPath(_path, true));
                }
            }
        });
    }

    public static String documentPath(String path) {
        if (path != null) {
            int idx = path.indexOf('/');
            if (idx == -1) {
                return path;
            } else {
                return path.substring(0, idx);
            }
        }
        return null;
    }

    public static String nodePath(String path) {
        if (path != null) {
            int idx = path.indexOf('/');
            if (idx != -1) {
                return path.substring(idx + 1);
            }
        }
        return null;
    }

    public boolean documentOnly() {
        return _path.indexOf('/') == -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (o instanceof MetadataPath) {
                return ObjectUtil.equals(((MetadataPath) o).path(), path());
            }
        }
        return false;
    }

    public String name() {
        if (path() == null) {
            return null;
        }
        int pos1 = path().lastIndexOf('/');
        int pos2 = path().lastIndexOf('@');
        if (pos1 == pos2) {
            return path();
        }
        if (pos1 > pos2) {
            return path().substring(pos1 + 1);
        } else {
            return path().substring(pos2 + 1);
        }
    }

    public static void main(String[] args) {
        System.out.println(new MetadataPath("/aaa/bbb/@ccc").name());
    }
}
