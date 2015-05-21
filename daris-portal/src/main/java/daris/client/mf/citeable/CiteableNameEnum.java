package daris.client.mf.citeable;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;

public class CiteableNameEnum implements DynamicEnumerationDataSource<String> {

    public CiteableNameEnum() {
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        new CiteableNameList().send(new ObjectMessageResponse<List<String>>() {

            @Override
            public void responded(List<String> names) {
                if (names != null) {
                    handler.exists(value, names.contains(value));
                }
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<String> handler) {
        new CiteableNameList().send(new ObjectMessageResponse<List<String>>() {

            @Override
            public void responded(List<String> names) {
                if (names != null && !names.isEmpty()) {
                    List<Value<String>> values = new ArrayList<Value<String>>();
                    for (String name : names) {
                        values.add(new Value<String>(name));
                    }
                    List<Value<String>> rvs = values;
                    int total = values.size();
                    int start1 = (int) start;
                    start1 = start1 < 0 ? 0 : start1;
                    int end1 = (int) end;
                    end1 = end1 > total ? total : end1;
                    if (start1 < total) {
                        rvs = values.subList(start1, end1);
                    } else {
                        rvs = null;
                    }
                    handler.process(start1, end1, total, rvs);
                } else {
                    handler.process(0, 0, 0, null);
                }
            }
        });
    }

}
