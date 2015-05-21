package daris.client.ui.doc;

import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.object.ObjectEventHandler;
import arc.gui.gwt.object.ObjectNavigator;
import arc.gui.gwt.object.ObjectNavigatorSelectionHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.asset.document.MetadataNamespaceRef;
import arc.mf.model.asset.document.tree.MetadataTree;
import arc.mf.object.tree.Node;
import daris.client.ui.DObjectGUIRegistry;

public class MetadataDocumentNavigatorPanel extends HorizontalSplitPanel {

	private Object _o;

	public MetadataDocumentNavigatorPanel() {

		ObjectNavigator nav = new ObjectNavigator(new MetadataTree(MetadataTree.DisplayTo.DOCUMENTS_ONLY),
				new ObjectNavigatorSelectionHandler() {

					@Override
					public void clickedObject(Node n, Object o, boolean readOnly) {
						// TODO Auto-generated method stub

					}

					@Override
					public void selectedObject(Node n, Object o, boolean readOnly) {
						_o = o;
						if (o instanceof MetadataDocumentRef) {
							selectedDocument((MetadataDocumentRef) o);
						} else if(o instanceof MetadataNamespaceRef){
							selectedNamespace((MetadataNamespaceRef)o );
						}
					}

					@Override
					public void deselectedObject(Node n, Object o) {
						// TODO Auto-generated method stub

					}
				}, new ObjectEventHandler() {

					@Override
					public void added(Object o) {
						// TODO Auto-generated method stub

					}

					@Override
					public void modified(Object o) {
						// TODO Auto-generated method stub

					}

					@Override
					public void changeInMembers(Object o) {
						// TODO Auto-generated method stub

					}

					@Override
					public void removed(Object o) {
						// TODO Auto-generated method stub

					}
				});
		nav.setPreferredWidth(0.4);
		nav.setHeight100();

		ObjectDetailedView dv = new ObjectDetailedView(ScrollPolicy.NONE);
		dv.setDisplayLoadingMessage(true);
		dv.setForEdit(false);
		dv.setObjectRegistry(DObjectGUIRegistry.get());
		dv.fitToParent();
		nav.setObjectDetailView(dv);

		fitToParent();
		add(nav);
		add(dv);
	}

	public Object object() {
		return _o;
	}

	public MetadataDocumentRef document() {
		if (_o == null) {
			return null;
		}
		if (_o instanceof MetadataDocumentRef) {
			return (MetadataDocumentRef) _o;
		}
		return null;
	}

	protected void selectedDocument(MetadataDocumentRef document) {

	}

	public String namespace() {
		if (_o == null) {
			return null;
		}
		if (_o instanceof MetadataNamespaceRef) {
			return ((MetadataNamespaceRef) _o).name();
		}
		if (_o instanceof MetadataDocumentRef) {
			return ((MetadataDocumentRef) _o).namespace();
		}
		return null;
	}
	
	protected void selectedNamespace(MetadataNamespaceRef namespace) {
		
	}
}
