package daris.client.model.sc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.sink.Sink;
import daris.client.model.sink.SinkRef;
import daris.client.model.sink.user.SinkUserSettings;
import daris.client.model.sink.user.SinkUserSettingsRef;
import daris.client.model.sink.user.messages.SinkUserSelfSettingsSet;

public class DeliveryDestination {

    public static final String DOWNLOAD_DESTINATION_NAME = "browser";

    public static final DeliveryDestination BROWSER = new DeliveryDestination(DeliveryMethod.download, null, null);

    private SinkRef _sink;

    private DeliveryMethod _method;

    private Map<String, DeliveryArg> _args;

    public DeliveryDestination(XmlElement ce) throws Throwable {
        _method = DeliveryMethod.fromString(ce.value("delivery-method"));
        if (_method == DeliveryMethod.deposit) {
            String sinkUrl = ce.value("delivery-destination");
            String sinkName = Sink.nameFromUrl(sinkUrl);
            Sink.Type sinkType = Sink.Type.fromString(ce.value("delivery-destination/@sink-type"));
            _sink = new SinkRef(sinkType, sinkName);
            _args = DeliveryArg.instantiate(ce);
        }
    }

    public DeliveryDestination(DeliveryMethod method, SinkRef sink, Map<String, DeliveryArg> args) {

        _method = method;
        _sink = sink;
        _args = (args == null || args.isEmpty()) ? null : new LinkedHashMap<String, DeliveryArg>(args);
    }

    public String name() {
        if (_method == DeliveryMethod.download) {
            return DOWNLOAD_DESTINATION_NAME;
        } else {
            return _sink.name();
        }
    }

    public SinkRef sink() {
        if (_method == DeliveryMethod.download) {
            return null;
        } else {
            return _sink;
        }
    }

    public DeliveryMethod method() {
        return _method;
    }

    @Override
    public String toString() {
        if (_method == DeliveryMethod.download) {
            return name();
        } else {
            return _sink.url();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof DeliveryDestination) {
            DeliveryDestination ddo = (DeliveryDestination) o;
            return ddo.method() == _method && name().equals(ddo.name());
        }
        return false;
    }

    public DeliveryArg arg(String name) {
        if (_args == null || _args.isEmpty()) {
            return null;
        }
        return _args.get(name);
    }

    public boolean argExists(String name) {
        return arg(name) != null;
    }

    public void setArg(String name, DeliveryArg.Type type, Object value) {
        if (_args == null) {
            _args = new LinkedHashMap<String, DeliveryArg>();
        }
        if (value == null) {
            DeliveryArg arg = arg(name);
            if (arg != null) {
                if (arg.type()!=DeliveryArg.Type.secure_delivery_arg || !arg.committed()) {
                    _args.remove(name);
                }
            }
        } else {
            DeliveryArg arg = new DeliveryArg(name, type, value);
            _args.put(arg.name(), arg);
        }
    }

    public Collection<DeliveryArg> args() {
        if (_args == null || _args.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableCollection(_args.values());
    }

    public void applySinkSettings(SinkUserSettings settings) {
        settings.applyTo(this);
    }

    public void saveSinkSettings(ObjectMessageResponse<Null> rh) {
        if (_method == DeliveryMethod.download) {
            if (rh != null) {
                rh.responded(null);
            }
            return;
        }
        new SinkUserSelfSettingsSet(new SinkUserSettings(this)).send(rh);
    }

    public void loadSinkSettings(ObjectResolveHandler<SinkUserSettings> rh) {
        if (_method == DeliveryMethod.download) {
            if (rh != null) {
                rh.resolved(null);
            }
            return;
        }
        new SinkUserSettingsRef(_sink).resolve(rh);
    }

    public void loadAndApplySinkSettings(final ActionListener al) {
        loadSinkSettings(new ObjectResolveHandler<SinkUserSettings>() {
            @Override
            public void resolved(SinkUserSettings uss) {
                if (uss != null) {
                    applySinkSettings(uss);
                    al.executed(true);
                } else {
                    al.executed(false);
                }
            }
        });
    }

    public void saveUpdateArgs(XmlWriter w) {
        w.add("delivery", _method.name());
        if (_method == DeliveryMethod.deposit) {
            w.add("delivery-destination", _sink.url());
            if (_args != null) {
                Collection<DeliveryArg> args = _args.values();
                if (args != null) {
                    for (DeliveryArg arg : args) {
                        arg.saveUpdateArgs(w);
                    }
                }
            }
        }
    }

}
