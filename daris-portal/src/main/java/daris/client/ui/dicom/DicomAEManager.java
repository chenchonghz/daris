package daris.client.ui.dicom;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.ActionListener;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Resource;
import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.DicomAE.Access;
import daris.client.model.dicom.messages.DicomAEAccess;
import daris.client.model.dicom.messages.DicomAEList;
import daris.client.model.dicom.messages.DicomAERemove;
import daris.client.ui.dicom.action.DicomAEAddAction;
import daris.client.ui.util.ButtonUtil;

public class DicomAEManager {

	public static final String ICON_ADD = Resource.INSTANCE.add16().getSafeUri().asString();
	public static final String ICON_REFRESH = Resource.INSTANCE.refreshGreen16().getSafeUri().asString();
	public static final String ICON_REMOVE = Resource.INSTANCE.remove16().getSafeUri().asString();

	private Window _win;
	private boolean _showing;

	private VerticalPanel _vp;
	private DicomAEGrid _grid;
	private Button _addButton;
	private Button _removeButton;
	private Button _refreshButton;
	
	Field<DicomAEList.Type> _typeField;
	Field<DicomAEList.Access> _accessField;

	public DicomAEManager() {

		/*
		 * Container
		 */
		_vp = new VerticalPanel();
		_vp.fitToParent();

		/*
		 * The filters form
		 */
		Form filtersForm = new Form();
		filtersForm.setNumberOfColumns(2);
		filtersForm.primarySection().setName("Filters");
		filtersForm.setHeight(30);
		filtersForm.setWidth100();
		
		_typeField = new Field<DicomAEList.Type>(new FieldDefinition("Type",
				new EnumerationType<DicomAEList.Type>(DicomAEList.Type.values()), null, null, 1, 1));
		_typeField.setValue(DicomAEList.Type.ALL);
		_typeField.addListener(new FormItemListener<DicomAEList.Type>() {

			@Override
			public void itemValueChanged(FormItem<DicomAEList.Type> f) {
				_grid.setType(f.value());
				_accessField.setEnabled(DicomAEList.Type.LOCAL != f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<DicomAEList.Type> f, Property property) {

			}
		});
		filtersForm.add(_typeField);
		
		_accessField = new Field<DicomAEList.Access>(new FieldDefinition("Access",
				new EnumerationType<DicomAEList.Access>(DicomAEList.Access.values()), null, null, 1, 1));
		_accessField.setValue(DicomAEList.Access.ALL);
		_accessField.addListener(new FormItemListener<DicomAEList.Access>() {

			@Override
			public void itemValueChanged(FormItem<DicomAEList.Access> f) {
				_grid.setAccess(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<DicomAEList.Access> f, Property property) {

			}
		});
		filtersForm.add(_accessField);


		filtersForm.render();

		_vp.add(filtersForm);

		/*
		 * The grid
		 */

		_grid = new DicomAEGrid(DicomAEList.Access.ALL, DicomAEList.Type.ALL);

		_vp.add(_grid);

		/*
		 * The button bar
		 */
		ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);

		_refreshButton = ButtonUtil.createButton(ICON_REFRESH, 16, 16, "Refresh", null, false, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_grid.refresh();
			}
		});
		bb.add(_refreshButton);

		_addButton = ButtonUtil.createButton(ICON_ADD, 16, 16, "Add", null, false, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ActionInterface<DicomAE> action = new DicomAEAddAction(_win) {
					@Override
					public void added() {
						_grid.refresh();
					}
				};
				action.execute();
			}
		});
		bb.add(_addButton);

		_removeButton = ButtonUtil.createButton(ICON_REMOVE, 16, 16, "Delete", null, false, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (_grid.selections() != null) {
					if (!_grid.selections().isEmpty()) {
						final DicomAE ae = _grid.selections().get(0);
						Dialog.confirm(_win, "Remove DICOM AE", "Do you want to remove DICOM AE: " + ae.toString()
								+ "?", new ActionListener() {
							@Override
							public void executed(boolean succeeded) {
								new DicomAERemove(ae).send(new ObjectMessageResponse<Null>() {

									@Override
									public void responded(Null r) {
										_grid.refresh();
									}
								});

							}
						});

					}
				}
			}
		});
		_removeButton.disable();
		bb.add(_removeButton);

		_vp.add(bb);

		_grid.setSelectionHandler(new SelectionHandler<DicomAE>() {

			@Override
			public void selected(DicomAE o) {
				_removeButton.disable();

				if (o.type() == DicomAE.Type.REMOTE) {
					if (o.access() == DicomAE.Access.PUBLIC) {
						new DicomAEAccess().send(new ObjectMessageResponse<List<DicomAE.Access>>(){

							@Override
							public void responded(List<Access> as) {
								if(as!=null){
									_removeButton.setEnabled(as.contains(DicomAE.Access.PUBLIC));
								}
							}});
					} else {
						_removeButton.enable();
					}
				}
			}

			@Override
			public void deselected(DicomAE o) {

			}
		});

		/*
		 * 
		 */
		_showing = false;
	}

	public void show(Window owner, double w, double h) {
		if (!_showing) {
			WindowProperties wp = new WindowProperties();
			wp.setModal(false);
			wp.setCanBeResized(true);
			wp.setCanBeClosed(true);
			wp.setCanBeMoved(true);
			wp.setSize(w, h);
			wp.setOwnerWindow(owner);
			wp.setTitle("DICOM Application Entities");
			_win = Window.create(wp);
			_win.setContent(_vp);
			_win.addCloseListener(new WindowCloseListener() {
				@Override
				public void closed(Window w) {
					_showing = false;
				}
			});
			_win.centerInPage();
			_win.show();
			_showing = true;
		}
	}

	public void show(Window owner) {
		show(owner, 0.6, 0.5);
	}

	private static DicomAEManager _instance;

	public static DicomAEManager get() {
		if (_instance == null) {
			_instance = new DicomAEManager();
		}
		return _instance;
	}

}
