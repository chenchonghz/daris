package daris.client.model.object.metadata.tree;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class ObjectTypeMetadataNode implements Container {

    private DObject.Type _type;
    private List<Node> _nodes;

    public ObjectTypeMetadataNode(DObjectRef project) {
        this(project, null);
    }

    public ObjectTypeMetadataNode(DObjectRef project, DObject.Type type) {
        _type = type;
        _nodes = new ArrayList<Node>();
        if (_type != null) {
            _nodes.add(new ObjectInterfaceMetadataNode(_type));
            if (_type == DObject.Type.project || _type == DObject.Type.subject || _type == DObject.Type.study) {
                _nodes.add(new ObjectDomainMetadataNode(_type, project));
            }
            if (_type==DObject.Type.subject || _type == DObject.Type.study || _type == DObject.Type.dataset) {
                _nodes.add(new ObjectDicomMetadataNode(_type));
            }
        } else {
            _nodes.add(new ObjectTypeMetadataNode(project, DObject.Type.project));
            _nodes.add(new ObjectTypeMetadataNode(project, DObject.Type.subject));
            _nodes.add(new ObjectTypeMetadataNode(project, DObject.Type.ex_method));
            _nodes.add(new ObjectTypeMetadataNode(project, DObject.Type.study));
            _nodes.add(new ObjectTypeMetadataNode(project, DObject.Type.dataset));
        }
    }

    public DObject.Type objectType() {
        return _type;
    }

    @Override
    public String type() {
        return name();
    }

    @Override
    public Image icon() {
        return null;
    }

    @Override
    public String name() {
        if (_type == null) {
            return "metadata";
        } else {
            return _type.toString() + " metadata";
        }
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
        return null;
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
        for (Node n : _nodes) {
            n.discard();
        }
    }

    @Override
    public boolean sorted() {
        return true;
    }

    @Override
    public Image openIcon() {
        return null;
    }

    @Override
    public Fuzzy hasChildren() {
        return Fuzzy.YES;
    }

    @Override
    public void contents(long start, long end, TreeNodeContentsHandler ch) {
        ch.loaded(0, _nodes.size(), _nodes.size(), _nodes);
    }

    @Override
    public void add(Node n, TreeNodeAddHandler ah) {

    }

    @Override
    public void remove(Node n, TreeNodeRemoveHandler rh) {

    }

}
