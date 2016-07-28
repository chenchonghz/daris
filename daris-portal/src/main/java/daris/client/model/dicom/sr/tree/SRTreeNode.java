package daris.client.model.dicom.sr.tree;

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
import daris.client.Resource;
import daris.client.model.dicom.sr.ContentItem;

public class SRTreeNode implements Container {

	public final static arc.gui.image.Image ICON_CONTAINER = new arc.gui.image.Image(
			Resource.INSTANCE.folderBlue16().getSafeUri().asString(), 16, 16);
	public final static arc.gui.image.Image ICON_CONTAINER_OPEN = new arc.gui.image.Image(
			Resource.INSTANCE.folderBlueOpen16().getSafeUri().asString(), 16, 16);
	public final static arc.gui.image.Image ICON_LEAF = new arc.gui.image.Image(
			Resource.INSTANCE.document16().getSafeUri().asString(), 16, 16);

	private ContentItem _item;
	private List<Node> _nodes;

	public SRTreeNode(ContentItem item) {
		_item = item;
	}

	@Override
	public String type() {
		return _item.type();
	}

	@Override
	public Image icon() {
		if (_item.hasItems()) {
			return ICON_CONTAINER;
		} else {
			return ICON_LEAF;
		}
	}

	@Override
	public String name() {
		return _item.name();
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
		return _item;
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
		if (_item.hasItems()) {
			return ICON_CONTAINER_OPEN;
		}
		return null;
	}

	@Override
	public Fuzzy hasChildren() {
		if (_item.hasItems()) {
			return Fuzzy.YES;
		}
		return Fuzzy.NO;
	}

	@Override
	public void contents(long start, long end, TreeNodeContentsHandler ch) {
		List<ContentItem> items = _item.items();
		if (items != null && !items.isEmpty()) {
			if (_nodes == null) {
				_nodes = new ArrayList<Node>(items.size());
				for (ContentItem ci : items) {
					_nodes.add(new SRTreeNode(ci));
				}
			}
			ch.loaded(start, end, _nodes.size(), _nodes);
			return;
		}
		ch.loaded(0, 0, 0, null);
	}

	@Override
	public void add(Node n, TreeNodeAddHandler ah) {

	}

	@Override
	public void remove(Node n, TreeNodeRemoveHandler rh) {

	}

}
