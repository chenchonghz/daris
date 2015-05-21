package daris.client.model.exmethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.mf.client.Output;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.transform.TransformBuilder;
import daris.client.model.transform.TransformDefinition;

public class ExMethodTransformStep extends ExMethodStep {

    public static class Iterator {

        public static enum IdType {
            asset_id, citeable_id;

            @Override
            public final String toString() {
                return super.toString().replace('_', '-');
            }

            public static IdType fromString(String s) {
                if (s != null) {
                    if (s.equals(asset_id.toString())) {
                        return asset_id;
                    }
                    if (s.equals(citeable_id.toString())) {
                        return citeable_id;
                    }
                }
                return null;
            }
        }

        private String _param;
        private String _query;
        private DObject.Type _scope;
        private IdType _idType;

        private String _exMethodId;

        public Iterator(XmlElement ie, String exMethodId) {
            _param = ie.value("parameter");
            _query = ie.value("query");
            _idType = IdType.fromString(ie.value("type"));
            _scope = DObject.Type.parse(ie.value("scope"));
            _exMethodId = exMethodId;
        }

        public String parameter() {
            return _param;
        }

        public String query() {
            return _query;
        }

        public IdType type() {
            return _idType;
        }

        public String fullQuery() {
            String pid = null;
            if (_scope == DObject.Type.ex_method) {
                pid = _exMethodId;
            } else if (_scope == DObject.Type.subject) {
                pid = IDUtil.getParentId(_exMethodId);
            } else if (_scope == DObject.Type.project) {
                pid = IDUtil.getParentId(_exMethodId, 2);
            } else {
                pid = _exMethodId;
            }
            StringBuilder sb = new StringBuilder();
            if (pid != null) {
                sb.append("(cid='" + pid + "' or cid starts with '" + pid + "')");
            }
            if (_query != null) {
                sb.append(" and (");
                sb.append(_query);
                sb.append(")");
            }
            return sb.toString();
        }

        public void iterate(final ObjectResolveHandler<Set<String>> rh) {
            XmlStringWriter w = new XmlStringWriter();
            w.add("where", fullQuery());
            if (_idType == IdType.citeable_id) {
                w.add("action", "get-cid");
            }
            w.add("size", "infinity");
            Session.execute("asset.query", w.document(), new ServiceResponseHandler() {

                @Override
                public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {

                    if (xe != null) {
                        List<String> values = null;
                        if (_idType == IdType.citeable_id) {
                            values = xe.values("cid");
                        } else if (_idType == IdType.asset_id) {
                            values = xe.values("id");
                        }
                        if(values!=null&&!values.isEmpty()){
                            rh.resolved(new HashSet<String>(values));
                            return;
                        }
                    }
                    rh.resolved(new HashSet<String>());
                }
            });
        }
    }

    private XmlElement _te;
    private Iterator _it;

    public ExMethodTransformStep(XmlElement te, String exmId, String exmProute, String stepPath, String name,
            State state, String notes, boolean editable) {
        super(exmId, exmProute, stepPath, name, state, notes, editable);
        _te = te;
        XmlElement ie = _te.element("iterator");
        if (ie == null) {
            _it = null;
        } else {
            _it = new Iterator(ie, exmId);
        }
    }

    public Iterator iterator() {
        return _it;
    }

    public long transformDefinitionUid() {
        long uid = -1;
        try {
            uid = _te.longValue("definition");
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
        return uid;
    }

    public int transformDefinitionVersion() {
        int version = 0;
        try {
            version = _te.intValue("definition/@version", 0);
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
        return version;
    }

    public void getTransformDefinition(final ObjectResolveHandler<TransformDefinition> rh) {
        long uid = transformDefinitionUid();
        int version = transformDefinitionVersion();
        Session.execute("transform.definition.describe", "<uid version=\"" + version + "\">" + uid + "</uid>",
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                        if (xe != null) {
                            XmlElement tde = xe.element("transform-definition");
                            if (tde != null) {
                                rh.resolved(new TransformDefinition(tde));
                                return;
                            }
                        }
                        rh.resolved(null);
                    }

                });
    }

    public void getTransformBuilder(final ObjectResolveHandler<TransformBuilder> rh) {
        getTransformDefinition(new ObjectResolveHandler<TransformDefinition>() {

            @Override
            public void resolved(final TransformDefinition td) {
                if (td != null) {
                    final StringBuilder name = new StringBuilder(td.name()==null?"Transform":td.name());
                    name.append(" [");
                    name.append(ExMethodTransformStep.this.exMethodId());
                    name.append("_");
                    name.append(ExMethodTransformStep.this.stepPath());
                    name.append("]");
                    if (_it != null) {
                        _it.iterate(new ObjectResolveHandler<Set<String>>() {
                            @Override
                            public void resolved(Set<String> values) {
                                Map<String, Set<String>> itValues = new HashMap<String, Set<String>>();
                                if (values != null && !values.isEmpty()) {
                                    itValues.put(_it.parameter(), values);
                                }
                                rh.resolved(new TransformBuilder(td, name.toString(), td.description(), itValues));
                            }
                        });
                    } else {
                        rh.resolved(new TransformBuilder(td, name.toString(), td.description(), null));
                    }
                } else {
                    rh.resolved(null);
                }
            }
        });
    }
}
