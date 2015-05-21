package daris.client.model.sc;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.sc.Layout.Pattern;
import daris.client.model.sc.messages.ShoppingCartLayoutPatternList;

public class LayoutPatternEnum implements DynamicEnumerationDataSource<Layout.Pattern> {

    public LayoutPatternEnum() {
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String patternName, final DynamicEnumerationExistsHandler handler) {

        if (patternName == null) {
            handler.exists(patternName, false);
            return;
        }
        new ShoppingCartLayoutPatternList().send(new ObjectMessageResponse<List<Layout.Pattern>>() {

            @Override
            public void responded(List<Pattern> patterns) {
                if (patterns != null && !patterns.isEmpty()) {
                    for (Layout.Pattern p : patterns) {
                        if (patternName.equals(p.name())) {
                            handler.exists(patternName, true);
                            return;
                        }
                    }
                }
                handler.exists(patternName, false);
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<Pattern> handler) {
        new ShoppingCartLayoutPatternList().send(new ObjectMessageResponse<List<Layout.Pattern>>() {

            @Override
            public void responded(List<Pattern> patterns) {
                if (patterns != null && !patterns.isEmpty()) {
                    List<Value<Layout.Pattern>> vs = new ArrayList<Value<Layout.Pattern>>(patterns.size());
                    for (Layout.Pattern p : patterns) {
                        Value<Layout.Pattern> v = new Value<Layout.Pattern>(p, p.name() + ": " + p.pattern() + " : "
                                + p.description());
                        vs.add(v);
                    }
                    List<Value<Layout.Pattern>> rvs = vs;
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
