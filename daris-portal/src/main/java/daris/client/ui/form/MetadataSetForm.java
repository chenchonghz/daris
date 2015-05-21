package daris.client.ui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Form;
import arc.gui.form.xml.XmlForm;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.doc.MetadataDocumentRefComparator;
import daris.client.ui.doc.MetadataDocumentSelectDialog;
import daris.client.ui.widget.LoadingBar;

public class MetadataSetForm extends ValidatedInterfaceComponent {

	public static final String DOCUMENT_ICON = Resource.INSTANCE.document16().getSafeUri().asString();
	public static final String VALID_ICON = Resource.INSTANCE.tickGreen16().getSafeUri().asString();
	public static final String INVALID_ICON = Resource.INSTANCE.exclamation16().getSafeUri().asString();

	private Map<String, MetadataDocumentRef> _docs;
	private Map<String, Form> _forms;
	private Map<String, HTML> _validityHtmls;

	private VerticalPanel _vp;
	private ListGrid<MetadataDocumentRef> _docGrid;
	private SimplePanel _sp;

	private ActionEntry _addActionEntry;
	private ActionEntry _removeActionEntry;
	private ActionEntry _clearActionEntry;

	private MetadataDocumentRef _selectedDoc;

	public MetadataSetForm(List<MetadataDocumentRef> docs) {
		_docs = new HashMap<String, MetadataDocumentRef>();
		if (docs != null && !docs.isEmpty()) {
			for (MetadataDocumentRef doc : docs) {
				_docs.put(doc.path(), doc);
			}
		}

		_forms = new HashMap<String, Form>();
		_validityHtmls = new HashMap<String, HTML>();

		_vp = new VerticalPanel();
		_vp.fitToParent();

		/*
		 * action menu
		 */
		MenuToolBar actionMenuToolBar = new MenuToolBar();
		actionMenuToolBar.setHeight(28);
		actionMenuToolBar.setWidth100();
		MenuButton actionMenuButton = new MenuButton("Action", null);
		actionMenuToolBar.add(actionMenuButton);
		Menu actionMenu = new Menu();
		_addActionEntry = new ActionEntry("Add document...", new Action() {
			@Override
			public void execute() {
				MetadataDocumentSelectDialog dlg = new MetadataDocumentSelectDialog(
						new MetadataDocumentSelectDialog.DocumentSelectionHandler() {

							@Override
							public void documentSelected(MetadataDocumentRef doc) {
								if (!_docs.containsKey(doc.path())) {
									addDocument(doc);
								}
							}
						});
				dlg.show(_vp.window());
			}
		});
		actionMenu.add(_addActionEntry);

		_removeActionEntry = new ActionEntry("Remove document", new Action() {

			@Override
			public void execute() {
				List<MetadataDocumentRef> docs = _docGrid.selections();
				if (docs != null && !docs.isEmpty()) {
					for (MetadataDocumentRef doc : docs) {
						removeDocument(doc);
					}
				}
			}
		});
		_removeActionEntry.disable();
		actionMenu.add(_removeActionEntry);

		_clearActionEntry = new ActionEntry("Clear all documents", new Action() {

			@Override
			public void execute() {
				clearDocuments();
			}
		});
		actionMenu.add(_clearActionEntry);

		actionMenuButton.setMenu(actionMenu);
		_vp.add(actionMenuToolBar);

		/*
		 * doc nav panel
		 */
		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.fitToParent();

		_docGrid = new ListGrid<MetadataDocumentRef>(ScrollPolicy.AUTO) {
			protected void preLoad() {

			}

			protected void postLoad(long start, long end, long total, List<ListGridEntry<MetadataDocumentRef>> entries) {
				if (entries == null || entries.isEmpty()) {
					return;
				}
				MetadataDocumentRef docToSelect = null;
				if (_docAdded != null) {
					docToSelect = _docAdded;
					_docAdded = null;
				} else {
					docToSelect = _selectedDoc;
				}
				if (docToSelect == null) {
					return;
				}
				for (ListGridEntry<MetadataDocumentRef> e : entries) {
					if (ObjectUtil.equals(e.data(), docToSelect)) {
						select(docToSelect);
						return;
					}
				}
			}
		};
		_docGrid.setEmptyMessage("");
		_docGrid.setLoadingMessage("loading...");
		_docGrid.setCursorSize(1000);
		_docGrid.addColumnDefn("path", "Document", "Document", new WidgetFormatter<MetadataDocumentRef, String>() {
			@Override
			public BaseWidget format(MetadataDocumentRef doc, String path) {
				HTML html = new HTML("<div><img src=\"" + DOCUMENT_ICON
						+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;" + path
						+ "&nbsp;</span></div>");
				html.setFontSize(11);
				return html;
			}
		}).setWidth(280);
		_docGrid.addColumnDefn("path", "Validity", "Validity", new WidgetFormatter<MetadataDocumentRef, String>() {

			@Override
			public BaseWidget format(MetadataDocumentRef doc, String path) {
				return _validityHtmls.get(path);
			}
		}).setWidth(200);
		_docGrid.setBorder(1, new RGB(0xdd,0xdd,0xdd));
		_docGrid.setMultiSelect(false);
		_docGrid.enableDropTarget(false);
		_docGrid.setDropHandler(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object o) {
				if (o != null) {
					if (o instanceof MetadataDocumentRef || o instanceof MetadataDocument) {
						return DropCheck.CAN;
					}
				}
				return DropCheck.CANNOT;
			}

			@Override
			public void drop(BaseWidget target, List<Object> data, DropListener dl) {
				for (Object o : data) {
					if (o instanceof MetadataDocumentRef) {
						MetadataDocumentRef doc = ((MetadataDocumentRef) o);
						String path = doc.path();
						if (!_docs.containsKey(path)) {
							addDocument(doc);
						}
					}
				}
				dl.dropped(DropCheck.CAN);
			}
		});
		_docGrid.setSelectionHandler(new SelectionHandler<MetadataDocumentRef>() {

			@Override
			public void selected(MetadataDocumentRef o) {
				List<MetadataDocumentRef> sdocs = _docGrid.selections();
				_removeActionEntry.disable();
				if (sdocs != null && !sdocs.isEmpty()) {
					_removeActionEntry.enable();
				}
				_selectedDoc = o;
				displayDocument(_selectedDoc);
			}

			@Override
			public void deselected(MetadataDocumentRef o) {
				List<MetadataDocumentRef> sdocs = _docGrid.selections();
				_removeActionEntry.disable();
				if (sdocs != null && !sdocs.isEmpty()) {
					_removeActionEntry.enable();
				}
				if (ObjectUtil.equals(_selectedDoc, o)) {
					_sp.clear();
				}
			}
		});
		_docGrid.setPreferredWidth(0.3);
		_docGrid.setHeight100();

		hsp.add(_docGrid);

		_sp = new SimplePanel();
		_sp.fitToParent();

		hsp.add(_sp);

		_vp.add(hsp);

	}

	private MetadataDocumentRef _docAdded;

	protected void addDocument(MetadataDocumentRef doc) {
		if (doc == null) {
			return;
		}
		if (_docs.containsKey(doc.path())) {
			return;
		}
		_docs.put(doc.path(), doc);
		updateValidityHtml(doc.path());
		_docAdded = doc;
		updateDocumentList();
	}

	private Form addForm(final MetadataDocument doc) {
		if (_forms.containsKey(doc.path())) {
			return _forms.get(doc.path());
		}
		final Form form = XmlForm.formFor(doc.definition().root());
		addMustBeValid(form);
		_forms.put(doc.path(), form);
		updateValidityHtml(doc.path());
		form.addChangeListener(new StateChangeListener() {
			@Override
			public void notifyOfChangeInState() {
				updateValidityHtml(doc.path());
			}
		});
		form.render();
		return form;
	}

	private void updateValidityHtml(String path) {
		MetadataDocumentRef doc = _docs.get(path);
		Form form = _forms.get(path);
		HTML html = _validityHtmls.get(path);
		if (doc == null) {
			_validityHtmls.remove(path);
			return;
		}
		if (html == null) {
			html = new HTML();
		}
		if (form == null) {
			html.setHTML("<div><img src=\""
					+ INVALID_ICON
					+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;Incomplete.</span></div>");
			_validityHtmls.put(path, html);
		} else {
			String icon = form.valid().valid() ? VALID_ICON : INVALID_ICON;
			StringBuilder sb = new StringBuilder();
			sb.append("<div>");
			sb.append("<img src=\"" + icon + "\" style=\"width:16px;height:16px;vertical-align:middle\">");
			if (!form.valid().valid()) {
				sb.append("<span style=\"\">&nbsp;" + form.valid().reasonForIssue() + "&nbsp;</span>");
			}
			sb.append("</div>");
			html.setHTML(sb.toString());
		}
		_validityHtmls.put(path, html);
	}

	protected void removeDocument(MetadataDocumentRef doc) {
		if (doc == null) {
			return;
		}
		if (!_docs.containsKey(doc.path())) {
			return;
		}
		_docs.remove(doc.path());
		removeForm(doc.path());
		updateValidityHtml(doc.path());
		if (ObjectUtil.equals(doc, _selectedDoc)) {
			_sp.clear();
		}
		updateDocumentList();

	}

	private Form removeForm(String path) {
		if (!_forms.containsKey(path)) {
			return null;
		}
		Form form = _forms.get(path);
		removeMustBeValid(form);
		_forms.remove(path);
		return form;
	}

	protected void clearDocuments() {
		if (_docs.isEmpty()) {
			return;
		}
		_docs.clear();
		clearForms();
		_validityHtmls.clear();
		updateDocumentList();
		_sp.clear();
		_removeActionEntry.disable();
	}

	private void clearForms() {
		if (_forms.isEmpty()) {
			return;
		}
		for (Form form : _forms.values()) {
			removeMustBeValid(form);
		}
		_forms.clear();
	}

	private void displayDocument(MetadataDocumentRef doc) {
		_sp.setContent(new LoadingBar("Loading document: " + doc.path() + "..."));
		Form form = _forms.get(doc.path());
		if (form != null) {
			form.render();
			_sp.setContent(new ScrollPanel(form, ScrollPolicy.AUTO));
		} else {
			doc.resolve(new ObjectResolveHandler<MetadataDocument>() {

				@Override
				public void resolved(MetadataDocument o) {
					if (o != null) {
						_sp.setContent(new ScrollPanel(addForm(o), ScrollPolicy.AUTO));
					}
				}
			});
		}
	}

	private void updateDocumentList() {
		List<ListGridEntry<MetadataDocumentRef>> es = new Vector<ListGridEntry<MetadataDocumentRef>>(_docs.size());
		ArrayList<MetadataDocumentRef> docs = new ArrayList<MetadataDocumentRef>(_docs.values());
		Collections.sort(docs, new MetadataDocumentRefComparator());
		for (MetadataDocumentRef doc : docs) {
			ListGridEntry<MetadataDocumentRef> e = new ListGridEntry<MetadataDocumentRef>(doc);
			e.set("path", doc.path());
			es.add(e);
		}
		_docGrid.setData(es, false);
	}

	@Override
	public Widget gui() {
		return _vp;
	}

	public void save(XmlWriter w) {

		if (!valid().valid()) {
			return;
		}
		Set<String> paths = _forms.keySet();
		for (String path : paths) {
			Form form = _forms.get(path);
			w.push(path);
			form.save(w);
			w.pop();
		}
	}
}
