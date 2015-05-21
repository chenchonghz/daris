package daris.client.ui.dicom;

import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;
import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.messages.DicomAEList;

public class DicomAEGrid extends ListGrid<DicomAE> {

	private DicomAEDataSource _ds;

	public DicomAEGrid() {
		this(DicomAEList.Access.ALL, DicomAEList.Type.ALL);
	}

	public DicomAEGrid(DicomAEList.Access access, DicomAEList.Type type) {
		super(ScrollPolicy.AUTO);
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(11);
		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("");
		setLoadingMessage("Loading DICOM Application Entities...");
		setCursorSize(10000);
		setMultiSelect(false);

		/*
		 * Define the columns
		 */
		addColumnDefn("name", "Name").setWidth(120);
		addColumnDefn("aet", "AE Title").setWidth(120);
		addColumnDefn("host", "Host").setWidth(180);
		addColumnDefn("port", "Port").setWidth(60);
		addColumnDefn("type", "Type").setWidth(80);
		addColumnDefn("access", "Access").setWidth(80);

		/*
		 * Set data source
		 */
		_ds = new DicomAEDataSource(type, access);
		setDataSource(new ListGridDataSource<DicomAE>(_ds, new Transformer<DicomAE, ListGridEntry<DicomAE>>() {

			@Override
			protected ListGridEntry<DicomAE> doTransform(DicomAE ae) throws Throwable {
				if (ae == null) {
					return null;
				}
				ListGridEntry<DicomAE> e = new ListGridEntry<DicomAE>(ae);
				e.set("name", ae.name());
				e.set("aet", ae.aet());
				e.set("host", ae.host());
				e.set("port", ae.port());
				e.set("type", ae.type());
				e.set("access",ae.access());
				return e;
			}
		}));
	}

	public void setAccess(DicomAEList.Access access) {
		_ds.setAccess(access);
		refresh();
	}

	public void setType(DicomAEList.Type type) {
		_ds.setType(type);
		refresh();
	}
}
