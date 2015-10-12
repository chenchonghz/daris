package daris.client.model.type;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

public class TypeStringEnum extends ObjectRef<List<String>> implements
        DynamicEnumerationDataSource<String> {

    private String _stype;

    public TypeStringEnum() {
        this(null);
    }

    public TypeStringEnum(String stype) {
        _stype = stype;
    }

    @Override
    public boolean supportPrefix() {
        return true;
    }

    @Override
    public void exists(final String value,
            final DynamicEnumerationExistsHandler handler) {
        resolve(new ObjectResolveHandler<List<String>>() {

            @Override
            public void resolved(List<String> types) {
                if (types != null && !types.isEmpty()) {
                    handler.exists(value, types.contains(value));
                } else {
                    handler.exists(value, false);
                }
            }
        });
    }

    @Override
    public void retrieve(final String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<String> handler) {
        resolve(new ObjectResolveHandler<List<String>>() {

            @Override
            public void resolved(List<String> types) {
                if (types == null || types.isEmpty()) {
                    handler.process(0, 0, 0, null);
                }
                List<Value<String>> vs = new ArrayList<Value<String>>();
                for (String t : types) {
                    if (prefix==null||t.startsWith(prefix)) {
                        Value<String> v = new Value<String>(t);
                        vs.add(v);
                    }
                }
                List<Value<String>> rvs = vs;
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
                return;
            }
        });
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        if (_stype != null) {
            w.add("stype", _stype);
        }
    }

    @Override
    protected String resolveServiceName() {
        return "type.list";
    }

    @Override
    protected List<String> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return xe.values("type");
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "List of MIME type";
    }

    @Override
    public String idToString() {
        return null;
    }

}
