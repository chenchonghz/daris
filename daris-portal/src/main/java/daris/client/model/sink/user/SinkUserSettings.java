package daris.client.model.sink.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sink.Sink;
import daris.client.model.sink.SinkRef;
import daris.client.model.sink.user.messages.SinkUserSelfSettingsSet;

public class SinkUserSettings {

    public static final String SERVICE_GET = "user.self.settings.get";
    public static final String SERVICE_SET = "user.self.settings.set";

    private static class Arg {
        public final String name;
        public final boolean wallet;
        public final String value;

        private Arg(String name, boolean wallet, String value) {
            this.name = name;
            this.wallet = wallet;
            this.value = value;
        }

        private Arg(XmlElement ae) throws Throwable {
            this(ae.value("@name"), ae.booleanValue("@wallet", false), ae.value());
        }
    }

    private SinkRef _sink;
    private Map<String, Arg> _args;

    public SinkUserSettings(SinkRef sink, XmlElement se) throws Throwable {
        _sink = sink;
        if (se != null) {
            // _sink = sinkFromApp(se.value("@app"));
            List<XmlElement> aes = se.elements("arg");
            if (aes != null && !aes.isEmpty()) {
                _args = new HashMap<String, Arg>();
                for (XmlElement ae : aes) {
                    Arg arg = new Arg(ae);
                    _args.put(arg.name, arg);
                }
            }
        }
    }

    public SinkUserSettings(DeliveryDestination destination) {
        assert destination.method() == DeliveryMethod.deposit;
        _sink = destination.sink();
        Collection<DeliveryArg> dArgs = destination.args();
        if (dArgs != null) {
            _args = new HashMap<String, Arg>();
            for (DeliveryArg dArg : dArgs) {
                switch (dArg.type()) {
                case delivery_arg:
                    _args.put(dArg.name(), new Arg(dArg.name(), false, String.valueOf(dArg.value())));
                    break;
                case secure_wallet_delivery_arg:
                    _args.put(dArg.name(), new Arg(dArg.name(), true, String.valueOf(dArg.value())));
                    break;
                default:
                    break;
                }
            }
            if (_args.isEmpty()) {
                _args = null;
            }
        }

    }

    public SinkRef sink() {
        return _sink;
    }

    public void applyTo(DeliveryDestination destination) {
        if (_args != null) {
            Collection<Arg> args = _args.values();
            for (Arg arg : args) {
                destination.setArg(arg.name, arg.wallet ? DeliveryArg.Type.secure_wallet_delivery_arg
                        : DeliveryArg.Type.delivery_arg, arg.value);
            }
        }
    }

    public void save(XmlWriter w) {
        w.add("app", appFromSink(_sink));
        w.push("settings");
        if (_args != null) {
            Collection<Arg> args = _args.values();
            if (args != null) {
                for (Arg a : args) {
                    if (a.wallet) {
                        w.add("arg", new String[] { "name", a.name, "wallet", Boolean.toString(a.wallet) }, a.value);
                    } else {
                        w.add("arg", new String[] { "name", a.name }, a.value);
                    }
                }
            }
        }
        w.pop();
    }

    public static String appFromSink(SinkRef sink) {
        return sink.type().typeName() + "-sink." + sink.name();
    }

    public static SinkRef sinkFromApp(String appName) {
        String[] tokens = appName.split("-sink\\.");
        return new SinkRef(Sink.Type.fromString(tokens[0]), tokens[1]);
    }

    public static void load(final SinkRef sink, final ObjectResolveHandler<SinkUserSettings> rh) {
        new SinkUserSettingsRef(sink).resolve(rh);
    }

    public static void save(SinkUserSettings settings, final ObjectMessageResponse<Null> rh) {
        new SinkUserSelfSettingsSet(settings).send(rh);
    }

}
