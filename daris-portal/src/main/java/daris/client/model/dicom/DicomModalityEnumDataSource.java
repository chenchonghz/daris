package daris.client.model.dicom;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

public class DicomModalityEnumDataSource implements DynamicEnumerationDataSource<DicomModality> {

    public DicomModalityEnumDataSource() {

    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        DicomModality.exists(value, new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean exists) {
                if (handler != null) {
                    handler.exists(value, exists);
                }
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<DicomModality> handler) {

        DicomModality.list(new ObjectResolveHandler<List<DicomModality>>() {
            @Override
            public void resolved(List<DicomModality> ms) {
                if (ms != null && !ms.isEmpty()) {
                    List<Value<DicomModality>> vs = new ArrayList<Value<DicomModality>>();
                    for (DicomModality m : ms) {
                        Value<DicomModality> v = new Value<DicomModality>(m.name, m.description, m);
                        vs.add(v);
                    }
                    List<Value<DicomModality>> rvs = vs;
                    int start1 = (int) start;
                    int end1 = (int) end;
                    long total = vs.size();
                    if (start1 > 0 || end1 < vs.size()) {
                        if (start1 >= vs.size()) {
                            rvs = null;
                        } else {
                            if (end1 > vs.size()) {
                                end1 = vs.size();
                            }
                            rvs = vs.subList(start1, end1);
                        }
                    }
                    handler.process(start1, end1, total, rvs);
                } else {
                    handler.process(0, 0, 0, null);
                }
            }
        });

    }
}
