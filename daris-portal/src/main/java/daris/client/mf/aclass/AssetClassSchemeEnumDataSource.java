package daris.client.mf.aclass;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.util.Predicate;
import arc.mf.client.util.Transform;
import arc.mf.client.util.Transformer;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class AssetClassSchemeEnumDataSource implements DynamicEnumerationDataSource<AssetClassSchemeRef> {

    @Override
    public boolean supportPrefix() {
        return true;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (value == null) {
            handler.exists(value, false);
            return;
        }
        new AssetClassSchemeExists(value).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean r) {
                boolean exists = r != null && r;
                handler.exists(value, exists);
            }
        });
    }

    @Override
    public void retrieve(final String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<AssetClassSchemeRef> handler) {
        Session.execute(null, "asset.class.scheme.list", null, null, 0, new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                List<EnumerationType.Value<AssetClassSchemeRef>> svs = xe.elements("scheme",
                        new Transformer<XmlElement, EnumerationType.Value<AssetClassSchemeRef>>() {
                            @Override
                            protected EnumerationType.Value<AssetClassSchemeRef> doTransform(XmlElement se)
                                    throws Throwable {
                                AssetClassSchemeRef scheme = new AssetClassSchemeRef(se.value("@lang"), se.value());
                                EnumerationType.Value<AssetClassSchemeRef> value = new Value<AssetClassSchemeRef>(
                                        scheme);
                                return value;
                            }
                        });
                if (prefix != null) {
                    svs = Transform.filter(svs, new Predicate<Value<AssetClassSchemeRef>>() {

                        @Override
                        protected boolean doEval(Value<AssetClassSchemeRef> sv) throws Throwable {
                            return sv.value().path().startsWith(prefix);
                        }
                    });
                }
                if (svs == null) {
                    handler.process(0, 0, 0, null);
                    return;
                }
                int total = svs.size();

                long lstart = start;
                long lend = end;

                if (lstart >= svs.size()) {
                    handler.process(start, end, total, null);
                    return;
                }

                if (lend > svs.size()) {
                    lend = svs.size();
                }

                svs = svs.subList((int) lstart, (int) lend);
                handler.process(start, end, total, svs);
            }
        }, true);

    }

}
