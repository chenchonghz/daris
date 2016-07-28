package daris.client.ui.archive;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.object.DObject;

public class ArchiveExplorer implements InterfaceComponent {

	private DObject _obj;
	private ArchiveEntryCollectionRef _arc;

	private HorizontalSplitPanel _hsp;
	private VerticalSplitPanel _vsp;
	private ArchiveEntryNavigator _nav;
	private ArchiveEntryViewer _viewer;

	public ArchiveExplorer(DObject obj) {
		this(obj, false);
	}

	public ArchiveExplorer(DObject obj, boolean vertical) {
		_obj = obj;
		_arc = new ArchiveEntryCollectionRef(obj);
		_nav = new ArchiveEntryNavigator(_arc);
		_nav.fitToParent();
		boolean mayContainViewableImage = true;
		if (_obj instanceof DicomDataSet) {
			mayContainViewableImage = ((DicomDataSet) _obj).containsViewableImage();
		}
		_viewer = new ArchiveEntryViewer(_arc, mayContainViewableImage);
		_nav.addSelectionHandler(new SelectionHandler<ArchiveEntry>() {

			@Override
			public void selected(ArchiveEntry o) {
				_viewer.setEntry(o);
			}

			@Override
			public void deselected(ArchiveEntry o) {
				_viewer.setEntry(null);
			}
		});
		if (vertical) {
			_vsp = new VerticalSplitPanel(5);
			_vsp.fitToParent();
			_vsp.add(_viewer.gui());
			_nav.setWidth100();
			_nav.setPreferredHeight(0.4);
			_vsp.add(_nav);
		} else {
			_hsp = new HorizontalSplitPanel(5);
			_hsp.fitToParent();
			_nav.setHeight100();
			_nav.setPreferredWidth(0.4);
			_hsp.add(_nav);
			_hsp.add(_viewer.gui());
		}

	}

	@Override
	public Widget gui() {
		return widget();
	}

	public BaseWidget widget() {
		return _vsp != null ? _vsp : _hsp;
	}

}
