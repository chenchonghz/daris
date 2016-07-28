package daris.client.ui.dicom;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import arc.gui.InterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.Form;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.client.util.ObjectUtil;
import arc.mf.dtype.ConstantType;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Node;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.dicom.sr.ContentItem;
import daris.client.model.dicom.sr.StructuredReport;
import daris.client.model.dicom.sr.StructuredReportRef;
import daris.client.model.dicom.sr.tree.SRTree;
import daris.client.ui.dicom.action.DicomSRExportAction;
import daris.client.ui.widget.LoadingMessage;

public class DicomStructuredReportViewer implements InterfaceComponent {

	private StructuredReportRef _o;

	private ContentItem _selected;

	private SimplePanel _sp;

	private SimplePanel _itemFormSP;

	private Button _exportButton;

	public DicomStructuredReportViewer(DicomDataSet ds) {
		this(new StructuredReportRef(ds));
	}

	public DicomStructuredReportViewer(StructuredReportRef o) {
		_o = o;

		_sp = new SimplePanel();
		_sp.fitToParent();

		_sp.setContent(new LoadingMessage("Retrieve DICOM structured report..."));
		_o.resolve(new ObjectResolveHandler<StructuredReport>() {

			@Override
			public void resolved(StructuredReport sr) {
				updateGUI(sr);
			}
		});
	}

	private void updateGUI(StructuredReport sr) {

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.fitToParent();

		TreeGUI tree = new TreeGUI(new SRTree(sr), ScrollPolicy.AUTO);
		tree.setEventHandler(new TreeGUIEventHandler() {

			@Override
			public void clicked(Node n) {

			}

			@Override
			public void selected(Node n) {
				_selected = (ContentItem) n.object();
				updateItemForm(_selected);
				_exportButton.setEnabled(_selected != null);
			}

			@Override
			public void deselected(Node n) {
				ContentItem o = (ContentItem) n.object();
				if (ObjectUtil.equals(_selected, o)) {
					_selected = null;
					updateItemForm(_selected);
				}
				_exportButton.setEnabled(_selected != null);
			}

			@Override
			public void opened(Node n) {

			}

			@Override
			public void closed(Node n) {

			}

			@Override
			public void added(Node n) {

			}

			@Override
			public void removed(Node n) {

			}

			@Override
			public void changeInMembers(Node n) {

			}
		});
		tree.setHeight100();
		tree.setPreferredWidth(0.5);
		hsp.add(tree);

		_itemFormSP = new SimplePanel();
		_itemFormSP.fitToParent();

		hsp.add(_itemFormSP);

		vp.add(hsp);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
		bb.setHeight(32);
		_exportButton = bb.addButton("Export...");
		_exportButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				new DicomSRExportAction(_o, _sp.window()).execute();
			}
		});
		_exportButton.setEnabled(_selected != null);
		vp.add(bb);

		_sp.setContent(vp);
	}

	private void updateItemForm(ContentItem item) {
		_itemFormSP.clear();
		if (item == null) {
			return;
		}
		Form form = new Form();

		form.setShowDescriptions(false);
		form.setShowHelp(false);
		form.setSpacing(15);
		form.setPadding(25);
		form.fitToParent();

		Field<String> name = new Field<String>("Name", ConstantType.DEFAULT, null, 1, 1);
		name.setValue(item.name());
		form.add(name);

		Field<String> relationship = new Field<String>("Relationship", ConstantType.DEFAULT, null, 0, 1);
		relationship.setValue(item.relationship());
		form.add(relationship);

		Field<String> code = new Field<String>("Code", ConstantType.DEFAULT, null, 1, 1);
		code.setValue(item.code());
		form.add(code);

		Field<String> type = new Field<String>("Type", ConstantType.DEFAULT, null, 1, 1);
		type.setValue(item.type());
		form.add(type);

		Field<String> value = new Field<String>("Value", ConstantType.DEFAULT, null, 0, 1);
		value.setValue(item.value());
		form.add(value);

		form.render();

		_itemFormSP.setContent(form);
	}

	@Override
	public Widget gui() {
		return _sp;
	}

	public BaseWidget widget() {
		return _sp;
	}

}
