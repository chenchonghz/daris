package daris.client.model.object.metadata.tree;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.asset.document.tree.MetadataDocumentContainerNode;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.metadata.ObjectDomainMetadataRef;

public class ObjectDomainMetadataNode implements Container {

    private ObjectDomainMetadataRef _domainMeta;

    public ObjectDomainMetadataNode(DObject.Type type, DObjectRef project) {
        _domainMeta = new ObjectDomainMetadataRef(type, project);
    }

    @Override
    public String type() {
        return _domainMeta.type().toString() + " " + name();
    }

    @Override
    public Image icon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String name() {
        return "domain metadata";
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public void description(TreeNodeDescriptionHandler dh) {

    }

    @Override
    public List<BaseWidget> adornments() {
        return null;
    }

    @Override
    public Object object() {
        return _domainMeta;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Fuzzy hasChildren() {
        return Fuzzy.MAYBE;
    }

    @Override
    public void contents(long start, long end, final TreeNodeContentsHandler ch) {
        _domainMeta.resolve(new ObjectResolveHandler<List<MetadataDocumentRef>>() {

            @Override
            public void resolved(List<MetadataDocumentRef> mds) {
                if (mds != null && !mds.isEmpty()) {
                    int size = mds.size();
                    List<Node> nodes = new ArrayList<Node>(size);
                    for (MetadataDocumentRef md : mds) {
                        nodes.add(new MetadataDocumentContainerNode(md, true){
                            @Override
                            public String name(){
                                return path();
                            }
                        });
                    }
                    ch.loaded(0, size, size, nodes);
                    return;
                }
                ch.loaded(0, 0, 0, null);
            }
        });

    }

    @Override
    public void add(Node n, TreeNodeAddHandler ah) {

    }

    @Override
    public void remove(Node n, TreeNodeRemoveHandler rh) {

    }

}
