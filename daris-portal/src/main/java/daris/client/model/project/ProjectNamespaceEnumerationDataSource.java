package daris.client.model.project;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.util.Transformer;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class ProjectNamespaceEnumerationDataSource implements DynamicEnumerationDataSource<String> {

    private String _dict;

    public ProjectNamespaceEnumerationDataSource() {
        _dict = Project.ASSET_NAMESPACE_DICTIONARY;
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("dictionary", _dict);
        w.add("term", value);
        Session.execute("dictionary.contains", w.document(), new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                handler.exists(value, xe.booleanValue("exists", false));
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<String> handler) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("dictionary", _dict);
        w.add("size", "infinity");
        Session.execute("dictionary.entries.list", w.document(), new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                List<EnumerationType.Value<String>> tvs = xe.elements("term",
                        new Transformer<XmlElement, EnumerationType.Value<String>>() {

                            @Override
                            protected Value<String> doTransform(XmlElement input) throws Throwable {
                                return new EnumerationType.Value<String>(input.value());
                            }
                        });
                long total = tvs == null ? 0 : tvs.size();
                handler.process(start, end, total, tvs);
            }
        });

    }

}
