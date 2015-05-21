package daris.client.model.secure.wallet;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class SecureWallet {

    public static class Availability {
        private boolean _exists;
        private boolean _canBeUsed;

        public Availability(boolean exists, boolean canBeUsed) {
            _exists = exists;
            _canBeUsed = canBeUsed;
        }

        public boolean exists() {
            return _exists;
        }

        public boolean canBeUsed() {
            return _canBeUsed;
        }
    }

    public static void getEntry(String key, final ObjectResolveHandler<SecureWalletEntry> rh) {
        Session.execute("secure.wallet.get", "<key>" + key + "</key>", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (xe != null) {
                    XmlElement ve = xe.element("value");
                    if (ve != null) {
                        rh.resolved(new SecureWalletEntry(ve));
                        return;
                    }
                }
                rh.resolved(null);
            }
        });
    }

    public static void setEntry(String key, String value, Usage usage, final ObjectMessageResponse<Null> rh) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("key", key);
        w.add("value", value);
        if (usage != null) {
            w.add("usage", new String[] { "type", usage.type.name() }, usage.usage);
        }
        Session.execute("secure.wallet.set", w.document(), new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                rh.responded(new Null());
            }
        });
    }

    public static void removeEntry(String key, final ObjectMessageResponse<Null> rh) {
        Session.execute("secure.wallet.remove", "<key>" + key + "</key>", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                rh.responded(new Null());
            }
        });
    }

    public static void removeAllEntries(final ObjectMessageResponse<Null> rh) {
        Session.execute("secure.wallet.all.remove", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                rh.responded(new Null());
            }
        });
    }

    public static void canBeUsed(final ObjectMessageResponse<Availability> rh) {
        Session.execute("secure.wallet.can.be.used", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (xe != null) {
                    rh.responded(new Availability(xe.booleanValue("can/@exists", false), xe.booleanValue("can", false)));
                } else {
                    rh.responded(null);
                }
            }
        });
    }

    public static void listEntries(ObjectMessageResponse<List<SecureWalletEntryRef>> rh) {
        listEntries(null, rh);
    }

    public static void listEntries(final SecureWalletEntryRef.Filter filter,
            final ObjectMessageResponse<List<SecureWalletEntryRef>> rh) {
        Session.execute("secure.wallet.entry.list", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (xe != null) {
                    List<XmlElement> kes = xe.elements("key");
                    if (kes != null && !kes.isEmpty()) {
                        List<SecureWalletEntryRef> ks = new ArrayList<SecureWalletEntryRef>();
                        for (XmlElement ke : kes) {
                            SecureWalletEntryRef entry = new SecureWalletEntryRef(ke);
                            if (filter == null || filter.matches(entry)) {
                                ks.add(entry);
                            }
                        }
                        rh.responded(ks);
                        return;
                    }
                }
                rh.responded(null);
            }
        });
    }

    public static void recreate(String password, final ObjectMessageResponse<Null> rh) {
        Session.execute("secure.wallet.recreate", "<password>" + password + "</password>",
                new ServiceResponseHandler() {
                    @Override
                    public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                        rh.responded(new Null());
                    }
                });
    }

    /**
     * Completely destroy the user wallet. User will need to log in again to
     * re-create.
     * 
     * @param rh
     */
    public static void destroy(final ObjectMessageResponse<Null> rh) {
        Session.execute("secure.wallet.destroy", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                rh.responded(new Null());
            }
        });
    }

    public static void contains(String key, final ObjectMessageResponse<Boolean> rh) {
        Session.execute("secure.wallet.contains", "<key>" + key + "</key>", new ServiceResponseHandler() {
            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                if (xe != null && xe.element("exists") != null) {
                    rh.responded(xe.booleanValue("exists", false));
                } else {
                    rh.responded(null);
                }
            }
        });
    }

    public static void setPassword(String oldPasswd, String passwd, final ObjectMessageResponse<Null> rh) {
        Session.execute("secure.wallet.password.set", "<old-password>" + oldPasswd + "</old-password><password>"
                + passwd + "</password>", new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                rh.responded(new Null());
            }
        });
    }

}
