package daris.client.model.dicom;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomAEList;

public class DicomAEEnum implements DynamicEnumerationDataSource<DicomAE> {

	private DicomAEList _list;

	public DicomAEEnum(DicomAEList.Type type, DicomAEList.Access access) {
		_list = new DicomAEList(type, access);
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String value, final DynamicEnumerationExistsHandler handler) {

		try {
			final DicomAE ae = DicomAE.fromString(value);
			_list.send(new ObjectMessageResponse<List<DicomAE>>() {

				@Override
				public void responded(List<DicomAE> as) {
					if (as != null) {
						handler.exists(value, as.contains(ae));
					} else {
						handler.exists(value, false);
					}
				}
			});
		} catch (Throwable e) {
			handler.exists(value, false);
			return;
		}

	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<DicomAE> handler) {

		_list.send(new ObjectMessageResponse<List<DicomAE>>() {

			@Override
			public void responded(List<DicomAE> as) {

				if (as != null) {
					if (!as.isEmpty()) {
						List<Value<DicomAE>> vs = new Vector<Value<DicomAE>>(as.size());
						for (DicomAE a : as) {
							vs.add(new Value<DicomAE>(a));
						}
						List<Value<DicomAE>> rvs = vs;
						int start1 = (int) start;
						int end1 = (int) end;
						long total = vs.size();
						if (start1 > 0 || end1 < vs.size()) {
							if (start1 >= vs.size()) {
								rvs = new Vector<Value<DicomAE>>();
							} else {
								if (end1 > vs.size()) {
									end1 = vs.size();
								}
								rvs = vs.subList(start1, end1);
							}
						}
						handler.process(start1, end1, total, rvs);
						return;
					}
				}
				handler.process(0, 0, 0, new Vector<Value<DicomAE>>());
			}
		});
	}

}
