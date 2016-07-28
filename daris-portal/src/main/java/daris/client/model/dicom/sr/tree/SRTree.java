package daris.client.model.dicom.sr.tree;

import arc.gui.image.Image;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.client.model.dicom.sr.StructuredReport;

public class SRTree implements Tree {

	private StructuredReport _sr;
	private SRTreeNode _root;

	public SRTree(StructuredReport sr) {
		_sr = sr;
		_root = new SRTreeNode(_sr.root());
	}

	@Override
	public Image icon() {
		return null;
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

	}

}
