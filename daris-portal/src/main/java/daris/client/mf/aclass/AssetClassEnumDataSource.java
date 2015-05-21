package daris.client.mf.aclass;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.util.Predicate;
import arc.mf.client.util.Transform;
import arc.mf.client.util.Transformer;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class AssetClassEnumDataSource implements DynamicEnumerationDataSource<AssetClassRef> {

    private AssetClassSchemeRef _scheme;

    public AssetClassEnumDataSource(AssetClassSchemeRef scheme) {
        _scheme = scheme;
    }

    public void setScheme(AssetClassSchemeRef scheme) {
        _scheme = scheme;
    }

    @Override
    public boolean supportPrefix() {
        return true;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (_scheme == null || value == null) {
            handler.exists(value, false);
            return;
        }
        String path = _scheme.path() + ":" + value;
        new AssetClassExists(path).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean r) {
                handler.exists(value, r != null && r);
            }
        });
    }

    @Override
    public void retrieve(final String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<AssetClassRef> handler) {
        if (_scheme == null) {
            handler.process(0, 0, 0, null);
            return;
        }
        XmlStringWriter w = new XmlStringWriter();
        w.add("scheme", new String[] { "lang", _scheme.language() }, _scheme.scheme());
        Session.execute(null, "asset.class.describe", w.document(), null, 0, new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                List<EnumerationType.Value<AssetClassRef>> cvs = xe.elements("class",
                        new Transformer<XmlElement, EnumerationType.Value<AssetClassRef>>() {
                            @Override
                            protected EnumerationType.Value<AssetClassRef> doTransform(XmlElement ce) throws Throwable {
                                String lang = ce.value("@lang");
                                String className = ce.value("name");
                                AssetClassRef c = new AssetClassRef(_scheme, lang, className, ce.value("description"),
                                        false);
                                return new Value<AssetClassRef>((lang == null ? "" : (lang + ":")) + className, null, c);
                            }
                        });
                if (prefix != null) {
                    cvs = Transform.filter(cvs, new Predicate<Value<AssetClassRef>>() {

                        @Override
                        protected boolean doEval(Value<AssetClassRef> cv) throws Throwable {
                            return cv.value().path().startsWith(prefix);
                        }
                    });
                }
                if (cvs == null) {
                    handler.process(0, 0, 0, null);
                    return;
                }
                int total = cvs.size();

                long lstart = start;
                long lend = end;

                if (lstart >= cvs.size()) {
                    handler.process(start, end, total, null);
                    return;
                }

                if (lend > cvs.size()) {
                    lend = cvs.size();
                }

                cvs = cvs.subList((int) lstart, (int) lend);
                handler.process(start, end, total, cvs);
            }
        }, true);
    }

}
