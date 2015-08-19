package daris.client.model.dicom;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class LocalAETitleEnumDataSource implements
        DynamicEnumerationDataSource<String> {

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value,
            final DynamicEnumerationExistsHandler handler) {
        Session.execute("daris.dicom.local.ae.title.list",
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        if (xe != null && xe.element("title") != null) {
                            List<String> values = xe.values("title");
                            handler.exists(value,
                                    values != null && values.contains(value));
                            return;
                        }
                        handler.exists(value, false);
                    }
                });

    }

    @Override
    public void retrieve(String prefix, long start, long end,
            final DynamicEnumerationDataHandler<String> handler) {
        Session.execute("daris.dicom.local.ae.title.list",
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        if (xe != null) {
                            List<String> titles = xe.values("title");
                            if (titles != null && !titles.isEmpty()) {
                                List<Value<String>> values = new ArrayList<Value<String>>(
                                        titles.size());
                                for (String title : titles) {
                                    values.add(new Value<String>(title));
                                }
                                handler.process(0L, values == null ? 0L
                                        : (long) values.size(), (long) values
                                        .size(), values);
                                return;
                            }
                        }
                        handler.process(0, 0, 0, null);
                    }
                });

    }

}
