package daris.client.idp;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.mf.client.util.UnhandledException;
import arc.mf.object.ObjectResolveHandler;
import arc.xml.XmlDoc;

public class IdentityProviderList {

    public static void resolve(String host, int port, boolean encrypt,
            String domain, ObjectResolveHandler<List<IdentityProvider>> rh) {
        if(host==null||port<0||domain==null||domain.isEmpty()){
            if(rh!=null){
                rh.resolved(null);
            }
            return;
        }
        new Thread(() -> {
            RemoteServer server = new RemoteServer(host, port, true, encrypt);
            ServerClient.Connection cxn;
            try {
                cxn = server.open();
                List<XmlDoc.Element> pes = cxn
                        .execute("authentication.domain.provider.list",
                                "<domain>" + domain + "</domain>")
                        .elements("provider");
                if (pes != null && !pes.isEmpty()) {
                    List<IdentityProvider> providers = new ArrayList<IdentityProvider>(
                            pes.size());
                    for (XmlDoc.Element pe : pes) {
                        providers.add(new IdentityProvider(pe));
                    }
                    if (providers != null) {
                        if (rh != null) {
                            rh.resolved(providers);
                            return;
                        }
                    }
                }
                if (rh != null) {
                    rh.resolved(null);
                }
            } catch (Throwable e) {
                rh.resolved(null);
                UnhandledException.report("Resolving identity providers", e);
            }

        }).start();
    }

}
