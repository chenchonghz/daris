package daris.client.model.file;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.dti.DTI;
import arc.mf.client.dti.DTIReadyListener;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
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

public class LocalDirectoryRootNode implements Container, DTIReadyListener {

	public static final Image ICON_COMPUTER = new Image(Resource.INSTANCE.computer16().getSafeUri().asString(), 16, 16);

	public static final String ROOT_NAME = "Local Files";

	private long _start;
	private long _end;
	private TreeNodeContentsHandler _ch;

	public LocalDirectoryRootNode() {

	}

	@Override
	public void add(Node n, TreeNodeAddHandler ah) {

	}

	@Override
	public void contents(long start, long end, final TreeNodeContentsHandler ch) {

		// If not enabled, then return nothing .. add a listener in case the DTI
		// becomes active.
		if (!DTI.enabled()) {
			// ch.loaded(start, end, 0, null);
			_start = start;
			_end = end;
			_ch = ch;
			DTI.addReadyListener(this);
			return;
		}

		DTI.fileSystem().roots(new FileHandler() {
			@Override
			public void process(long start, long end, long total, List<LocalFile> files) {

				if (files == null) {
					ch.loaded(start, end, total, null);
				} else {
					List<Node> contents = new Vector<Node>(files.size());
					for (LocalFile f : files) {
						contents.add(new LocalDirectoryNode(f));
					}

					ch.loaded(start, end, total, contents);
				}
			}
		});
	}

	@Override
	public void remove(Node n, TreeNodeRemoveHandler rh) {

	}

	@Override
	public String name() {

		return ROOT_NAME;
	}

	@Override
	public Object object() {

		return null;
	}

	@Override
	public String type() {

		return getClass().getName();
	}

	@Override
	public Image icon() {

		return ICON_COMPUTER;
	}

	@Override
	public Image openIcon() {

		return ICON_COMPUTER;
	}

	@Override
	public boolean sorted() {

		return true;
	}

	@Override
	public String path() {

		return null;
	}

	@Override
	public void description(TreeNodeDescriptionHandler dh) {

		if (DTI.enabled()) {
			dh.description("Files on the local file system. These can be copied into/from the server.");
		}

		dh.description("Files on the local file system.<br/>Not available - desktop integration has not activated.");
	}

	@Override
	public boolean readOnly() {

		return true;
	}

	@Override
	public void unsubscribe(Object key) {

	}

	@Override
	public void failed(String reason) {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			_ch.loaded(_start, _end, 0, null);
			_ch = null;
		}

	}

	@Override
	public void activated() {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			contents(_start, _end, _ch);
		}

	}

	@Override
	public void deactivated() {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			_ch.loaded(_start, _end, 0, null);
			_ch = null;
		}
	}

	@Override
	public void discard() {

		DTI.removeReadyListener(this);
	}

	@Override
	public Fuzzy hasChildren() {

		// TODO Auto-generated method stub
		return Fuzzy.MAYBE;
	}

	@Override
	public List<BaseWidget> adornments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object subscribe(DynamicBoolean descend, NodeListener l) {
		// TODO Auto-generated method stub
		return null;
	}

}