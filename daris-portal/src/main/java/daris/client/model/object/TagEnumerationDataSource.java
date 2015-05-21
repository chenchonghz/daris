package daris.client.model.object;

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

public class TagEnumerationDataSource implements DynamicEnumerationDataSource<String> {

    private DObjectRef _project;
    private DObject.Type _type;

    public TagEnumerationDataSource(DObjectRef project, DObject.Type type) {
        _project = project;
        _type = type;
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String tag, final DynamicEnumerationExistsHandler handler) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("project", _project.id());
        w.add("type", _type.toString());
        w.add("tag", tag);

        Session.execute("om.pssd.object.tag.dictionary.entry.exists", w.document(), new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                handler.exists(tag, xe.booleanValue("exists"));
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<String> handler) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("project", _project.id());
        w.add("type", _type.toString());

        Session.execute("om.pssd.object.tag.dictionary.entry.list", w.document(), new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                List<EnumerationType.Value<String>> tvs = xe.elements("tag",
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
