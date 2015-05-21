package daris.client.ui.doc;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.object.register.ObjectUpdateHandle;
import arc.gui.object.register.ObjectUpdateListener;
import arc.gui.window.Window;
import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.asset.document.tree.MetadataDocumentLeafNode;
import arc.mf.object.ObjectResolveHandler;

public class MetadataDocumentGUI implements ObjectGUI {

	public static final MetadataDocumentGUI INSTANCE = new MetadataDocumentGUI();

	private MetadataDocumentGUI() {

	}

	@Override
	public String idToString(Object o) {
		if (o != null) {
			if (o instanceof MetadataDocumentRef) {
				return ((MetadataDocumentRef) o).idToString();
			}
		}
		return null;
	}

	@Override
	public String icon(Object o, int size) {
		return MetadataDocumentLeafNode.ICON.path();
	}

	@Override
	public Menu actionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Menu memberActionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object reference(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needToResolve(Object o) {
		if (o != null) {
			if (o instanceof MetadataDocumentRef) {
				return ((MetadataDocumentRef) o).needToResolve();
			}
		}
		return false;
	}

	@Override
	public void displayDetails(final Object o, final ObjectDetailsDisplay dd, boolean forEdit) {

		if (!forEdit) {
			if (o instanceof MetadataDocumentRef) {
				((MetadataDocumentRef) o).resolve(new ObjectResolveHandler<MetadataDocument>() {
					@Override
					public void resolved(MetadataDocument doc) {
						HTML html = new HTML(MetadataDocumentHTMLUtil.toHTMLString(doc));
						html.fitToParent();
						dd.display(o, new ScrollPanel(html, ScrollPolicy.VERTICAL));
					}
				});
			}
		}
	}

	@Override
	public void open(Window w, Object o) {
		// TODO Auto-generated method stub

	}

	@Override
	public DropHandler dropHandler(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DragWidget dragWidget(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectUpdateHandle createUpdateMonitor(Object o, ObjectUpdateListener ul) {
		// TODO Auto-generated method stub
		return null;
	}

}
