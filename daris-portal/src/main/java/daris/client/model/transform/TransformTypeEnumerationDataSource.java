package daris.client.model.transform;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class TransformTypeEnumerationDataSource implements DynamicEnumerationDataSource<String> {

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        getTypes(new ObjectResolveHandler<List<String>>() {

            @Override
            public void resolved(List<String> types) {
                if (types == null) {
                    handler.exists(value, false);
                } else {
                    handler.exists(value, types.contains(value));
                }
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<String> handler) {
        getTypes(new ObjectResolveHandler<List<String>>() {

            @Override
            public void resolved(List<String> types) {
                if (types == null || types.isEmpty()) {
                    handler.process(start, end, 0, null);
                } else {
                    List<Value<String>> vs = new ArrayList<Value<String>>(types.size());
                    for (String type : types) {
                        vs.add(new Value<String>(type));
                    }
                    handler.process(start, end, vs.size(), vs);
                }
            }
        });
    }

    private void getTypes(final ObjectResolveHandler<List<String>> handler) {
        Session.execute("transform.type.list", new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                handler.resolved(xe.values("type"));
            }
        });
    }

}
