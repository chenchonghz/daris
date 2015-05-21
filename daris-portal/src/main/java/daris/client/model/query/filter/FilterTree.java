package daris.client.model.query.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.Tree;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;
import daris.client.model.query.filter.dicom.PatientBirthDateFilter;
import daris.client.model.query.filter.dicom.PatientIdFilter;
import daris.client.model.query.filter.dicom.PatientNameFilter;
import daris.client.model.query.filter.dicom.PatientSexFilter;
import daris.client.model.query.filter.dicom.SeriesModalityFilter;
import daris.client.model.query.filter.dicom.SeriesScanDateFilter;
import daris.client.model.query.filter.dicom.SeriesSizeFilter;
import daris.client.model.query.filter.dicom.StudyIngestDateFilter;
import daris.client.model.query.filter.dicom.StudyScanDateFilter;
import daris.client.model.query.filter.mf.ACLFilter;
import daris.client.model.query.filter.mf.ACLFilter.ACLOperator;
import daris.client.model.query.filter.mf.AssetFilter;
import daris.client.model.query.filter.mf.AssetModifiedFilter;
import daris.client.model.query.filter.mf.CIDFilter;
import daris.client.model.query.filter.mf.CTimeFilter;
import daris.client.model.query.filter.mf.CTimeRangeFilter;
import daris.client.model.query.filter.mf.ClassFilter;
import daris.client.model.query.filter.mf.ContentStatusFilter;
import daris.client.model.query.filter.mf.ContentStoreFilter;
import daris.client.model.query.filter.mf.CreatedByFilter;
import daris.client.model.query.filter.mf.MTimeFilter;
import daris.client.model.query.filter.mf.MTimeRangeFilter;
import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.model.query.filter.mf.ModifiedByFilter;
import daris.client.model.query.filter.mf.NamespaceFilter;
import daris.client.model.query.filter.mf.TextFilter;

public class FilterTree implements Tree {

    public static FilterTree DEFAULT = new FilterTree(
            new FilterEntry[] {
                    new FilterEntry("composite", new CompositeFilter(), "Composite filter"),
                    new FilterEntry("asset/metadata", new MetadataFilter(null, null, null, false),
                            "Metadata filter"),
                    new FilterEntry("asset/acl", new ACLFilter(ACLOperator.FOR_ROLE, null),
                            "ACL filter"),
                    new FilterEntry("asset/cid", new CIDFilter(), "CID filter"),
                    new FilterEntry("asset/class", new ClassFilter(null, null),
                            "Asset class filter"),
                    new FilterEntry("asset/content-status", new ContentStatusFilter(),
                            "Asset content status filter"),
                    new FilterEntry("asset/content-store", new ContentStoreFilter(),
                            "Asset content store filter"),
                    new FilterEntry("asset/created-by", new CreatedByFilter(),
                            "Asset created-by filter"),
                    new FilterEntry("asset/ctime", new CTimeFilter(), "Asset ctime filter"),
                    new FilterEntry("asset/ctime.range", new CTimeRangeFilter(),
                            "Asset ctime range filter"),
                    new FilterEntry("asset/mtime", new MTimeFilter(), "Asset mtime filter"),
                    new FilterEntry("asset/mtime.range", new MTimeRangeFilter(),
                            "Asset mtime range filter"),
                    new FilterEntry("asset/modified", new AssetModifiedFilter(),
                            "Asset modified filter"),
                    new FilterEntry("asset/modified-by", new ModifiedByFilter(),
                            "Asset modified-by filter"),
                    new FilterEntry("asset/namespace", new NamespaceFilter(),
                            "Asset namespace filter"),
                    new FilterEntry("asset/property", new AssetFilter(null, null), "Asset filter"),
                    new FilterEntry("asset/text", new TextFilter(null, null, null), "Text filter"),
                    new FilterEntry("dicom/patient/id", new PatientIdFilter(),
                            "Dicom patient id filter"),
                    new FilterEntry("dicom/patient/name", new PatientNameFilter(),
                            "Dicom patient name filter"),
                    new FilterEntry("dicom/patient/sex", new PatientSexFilter(),
                            "Dicom patient sex filter"),
                    new FilterEntry("dicom/patient/birth date", new PatientBirthDateFilter(),
                            "Dicom patient birth date filter"),
                    new FilterEntry("dicom/study/scan date", new StudyScanDateFilter(),
                            "Dicom study scan date filter"),
                    new FilterEntry("dicom/study/ingest date", new StudyIngestDateFilter(),
                            "Dicom study ingest date filter"),
                    new FilterEntry("dicom/series/protocol", new SeriesModalityFilter(),
                            "Dicom series protocol filter"),
                    new FilterEntry("dicom/series/modality", new SeriesModalityFilter(),
                            "Dicom series modality filter"),
                    new FilterEntry("dicom/series/size", new SeriesSizeFilter(),
                            "Dicom series size filter"),
                    new FilterEntry("dicom/series/scan date", new SeriesScanDateFilter(),
                            "Dicom series scan date filter") });

    public static class FilterEntry {
        public final Filter filter;
        public final String path;
        public final String description;

        public FilterEntry(String path, Filter filter, String description) {
            this.filter = filter;
            this.path = path;
            this.description = description;
        }
    }

    public static class FilterTreeNode implements Container {

        public static final Image FOLDER_ICON = new Image(Resource.INSTANCE.folderBlue16()
                .getSafeUri().asString(), 16, 16);
        public static final Image FOLDER_OPEN_ICON = new Image(Resource.INSTANCE.folderBlueOpen16()
                .getSafeUri().asString(), 16, 16);

        public static final Image FILTER_ICON = new Image(Resource.INSTANCE.search32().getSafeUri()
                .asString(), 16, 16);

        private List<Node> _children;
        private String _path;
        private Filter _f;
        private String _description;

        public FilterTreeNode(FilterTreeNode parent, String path, Filter f, String description) {

            if (parent != null) {
                parent.add(this, null);
            }
            _children = new ArrayList<Node>();
            _path = path;
            _f = f;
            _description = description;
        }

        @Override
        public String type() {

            return object() == null ? "directory" : "filter";
        }

        @Override
        public Image icon() {
            return object() == null ? FOLDER_ICON : FILTER_ICON;
        }

        @Override
        public String name() {
            return _path == null ? null : _path.substring(_path.lastIndexOf('/') + 1);
        }

        @Override
        public String path() {
            return _path;
        }

        @Override
        public void description(TreeNodeDescriptionHandler dh) {
            if (_description != null) {
                dh.description(_description);
            }
        }

        @Override
        public List<BaseWidget> adornments() {
            return null;
        }

        @Override
        public Object object() {
            return _f;
        }

        @Override
        public boolean readOnly() {
            return true;
        }

        @Override
        public Object subscribe(DynamicBoolean descend, NodeListener l) {
            return null;
        }

        @Override
        public void unsubscribe(Object key) {

        }

        @Override
        public void discard() {

        }

        @Override
        public boolean sorted() {
            return false;
        }

        @Override
        public Image openIcon() {
            return object() == null ? FOLDER_OPEN_ICON : FILTER_ICON;
        }

        @Override
        public Fuzzy hasChildren() {
            return _children.isEmpty() ? Fuzzy.NO : Fuzzy.YES;
        }

        @Override
        public void contents(long start, long end, final TreeNodeContentsHandler ch) {
            int start0 = (int) start;
            int nbChildren = _children.size();
            int end0 = end > nbChildren ? nbChildren : (int) end;
            if (start0 > end0 || start0 >= end) {
                ch.loaded(0, 0, 0, null);
                return;
            }
            ch.loaded(start0, end0, end0 - start0, _children.subList(start0, end0));
        }

        @Override
        public void add(Node n, TreeNodeAddHandler ah) {
            _children.add((FilterTreeNode) n);
            if (ah != null) {
                ah.added(n);
            }
        }

        @Override
        public void remove(Node n, TreeNodeRemoveHandler rh) {
            _children.remove((FilterTreeNode) n);
            if (rh != null) {
                rh.removed(n);
            }
        }

    }

    private FilterTreeNode _root;
    private HashMap<String, FilterTreeNode> _nodes;

    protected FilterTree(FilterEntry... entries) {
        _nodes = new HashMap<String, FilterTreeNode>();
        _root = new FilterTreeNode(null, null, null, null);
        if (entries != null) {
            for (FilterEntry e : entries) {
                addFilter(e.path, e.filter, e.description);
            }
        }
    }

    public void addFilter(String path, Filter f, String description) {
        addNodes(path, 0, _root, f, description);
    }

    private void addNodes(String path, int fromIndex, FilterTreeNode pn, Filter f,
            String description) {
        int i = path.indexOf('/', fromIndex);
        String npath = i == -1 ? path : path.substring(0, i);
        FilterTreeNode n = _nodes.get(npath);
        if (n == null) {
            if (i == -1) {
                n = new FilterTreeNode(pn, npath, f, description);
            } else {
                n = new FilterTreeNode(pn, npath, null, null);
            }
            _nodes.put(n.path(), n);
        }
        if (i != -1) {
            addNodes(path, i + 1, n, f, description);
        }
    }

    @Override
    public Image icon() {
        return FilterTreeNode.FILTER_ICON;
    }

    @Override
    public Container root() {
        return _root;
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public void discard() {
        _nodes.clear();
    }

}
