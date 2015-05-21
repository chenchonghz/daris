package daris.client.model.object.tree;

import arc.gui.image.Image;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.client.model.repository.RepositoryRef;

public class DObjectTree implements Tree {

	private RepositoryRef _root;
	private boolean _readOnly;

	public DObjectTree(RepositoryRef root) {

		_root = root;
	}

	@Override
	public Image icon() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container root() {

		return new DObjectTreeNode(null, _root);
	}

	@Override
	public boolean readOnly() {

		return _readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {

		_readOnly = readOnly;
	}

	@Override
	public void discard() {

	}

}
