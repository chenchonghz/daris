package daris.client.model.query.options;

import java.util.List;

import arc.mf.client.util.ListUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.model.asset.document.tree.MetadataTree;
import arc.mf.model.asset.document.tree.MetadataTree.DisplayTo;
import arc.mf.object.tree.Tree;

public class AssetQueryOptions extends QueryOptions {

    public static final XPath XPATH_CID = new XPath("cid", "cid");
    public static final XPath XPATH_MIME_TYPE = new XPath("mime_type", "type");

    public AssetQueryOptions() {
        super(Entity.asset, Action.get_value);
    }

    protected AssetQueryOptions(XmlElement xe) throws Throwable {
        super(xe);
    }

    @Override
    public Tree metadataTree() {
        return new MetadataTree(DisplayTo.DOCUMENT_NODES, true);
    }

    @Override
    public List<XPath> defaultXPaths(Purpose purpose) {
        switch (purpose) {
        case QUERY:
        case EXPORT:
            return ListUtil.list(XPATH_CID, XPATH_MIME_TYPE);
        default:
            return null;
        }
    }

}
