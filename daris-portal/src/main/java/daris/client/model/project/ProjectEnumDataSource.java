package daris.client.model.project;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.object.DObjectCollectionRef;
import daris.client.model.object.DObjectRef;

public class ProjectEnumDataSource implements DynamicEnumerationDataSource<DObjectRef> {

    public ProjectEnumDataSource() {

    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (value == null) {
            handler.exists(value, false);
            return;
        }
        Session.execute("asset.query", "<where>model='om.pssd.project' and cid='" + value
                + "'</where><action>count</action>", new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                handler.exists(value, xe != null && xe.longValue("value") > 0);
            }
        });

    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<DObjectRef> handler) {
        new DObjectCollectionRef().resolve(new CollectionResolveHandler<DObjectRef>() {

            @Override
            public void resolved(List<DObjectRef> projects) throws Throwable {
                if (projects != null && !projects.isEmpty()) {
                    List<Value<DObjectRef>> values = new ArrayList<Value<DObjectRef>>();
                    for (DObjectRef project : projects) {
                        values.add(new Value<DObjectRef>(project.id(), project.idToString(), project));
                    }
                    List<Value<DObjectRef>> rvs = values;
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
