package daris.client.ui.doc;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.asset.document.MetadataNamespaceRef;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.util.ButtonUtil;

public class MetadataDocumentSelectDialog {
	public static final double WIDTH = 0.5;
	public static final double HEIGHT = 0.5;

	public static interface DocumentSelectionHandler {
		void documentSelected(MetadataDocumentRef doc);
	}

	private DocumentSelectionHandler _sh;

	private Window _win;
	private VerticalPanel _vp;
	private MetadataDocumentNavigatorPanel _nav;
	private Button _cancelButton;
	private Button _selectButton;

	public MetadataDocumentSelectDialog(DocumentSelectionHandler sh) {

		_sh = sh;

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_nav = new MetadataDocumentNavigatorPanel() {
			protected void selectedDocument(MetadataDocumentRef doc) {
				_selectButton.enable();
			}

			protected void selectedNamespace(MetadataNamespaceRef ns) {
				_selectButton.disable();
			}
		};
		_vp.add(_nav);

		ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 32);
		_cancelButton = bb.addButton("Cancel");
		_cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_win.close();
			}
		});
		_selectButton = bb.addButton("Select");
		_selectButton.setMarginRight(20);
		_selectButton.disable();
		_selectButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_win.close();
				if (_sh != null) {
					_sh.documentSelected(_nav.document());
				}
			}
		});
		_vp.add(bb);
	}

	public void show(Window owner) {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setTitle("Select Metadata Document Type");
		wp.setCanBeResized(true);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(true);
		wp.setOwnerWindow(owner);
		wp.setSize(WIDTH, HEIGHT);
		wp.setCenterInPage(true);
		_win = Window.create(wp);
		_win.setContent(_vp);
		_win.centerInPage();
		_win.show();
	}

}
