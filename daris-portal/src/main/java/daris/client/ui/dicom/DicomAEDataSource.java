package daris.client.ui.dicom;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.messages.DicomAEList;

public class DicomAEDataSource implements DataSource<DicomAE> {

	private DicomAEList _list;

	public DicomAEDataSource(DicomAEList.Type type, DicomAEList.Access access) {
		_list = new DicomAEList(type, access);
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public boolean supportCursor() {
		return false;
	}

	@Override
	public void load(final Filter f, final long start, final long end, final DataLoadHandler<DicomAE> lh) {
		_list.send(new ObjectMessageResponse<List<DicomAE>>() {

			@Override
			public void responded(List<DicomAE> aes) {
				if (aes != null) {
					List<DicomAE> es = aes;
					if (f != null) {
						List<DicomAE> fes = new Vector<DicomAE>();
						for (DicomAE ae : aes) {
							if (f.matches(ae)) {
								fes.add(ae);
							}
						}
						es = fes;
					}
					int total = es.size();
					int start1 = (int) start;
					int end1 = (int) end;
					if (start1 > 0 || end1 < es.size()) {
						if (start1 >= es.size()) {
							es = null;
						} else {
							if (end1 > es.size()) {
								end1 = es.size();
							}
							es = es.subList(start1, end1);
						}
					}
					lh.loaded(start1, end1, total, es, DataLoadAction.REPLACE);
				} else {
					lh.loaded(0, 0, 0, null, null);
				}
			}
		});
	}

	public void setType(DicomAEList.Type type) {
		_list.setType(type);
	}

	public void setAccess(DicomAEList.Access access) {
		_list.setAccess(access);
	}

}
