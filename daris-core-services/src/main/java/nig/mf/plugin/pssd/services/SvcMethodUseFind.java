package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcMethodUseFind extends PluginService {
    private Interface _defn;

    public SvcMethodUseFind() {
        _defn = new Interface();
        Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the Method.", 1, 1);
        _defn.add(new Interface.Element(
                "pdist",
                IntegerType.DEFAULT,
                "Specifies the peer distance for a distributed query. Defaults to infinity in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
                0, 1));
        _defn.add(new Interface.Element ("top-level", BooleanType.DEFAULT, "Restrict the search to the top-level meta-data elements of any ExMethods.  Otherwise, searches  down all levels of ExMethods (e.g. branches or embeddedMethods", 0, 1));
        _defn.add(me);
    }

    public String name() {
        return "om.pssd.method.use.find";
    }

    public String description() {
        return "Discovers the ExMethods that use the given Method";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String pdist = args.value("pdist");
        Boolean topLevel = args.booleanValue("top-level", false);

        // Validate
        DistributedAsset dID = new DistributedAsset(args.element("id"));
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID));
        if (type == null) {
            throw new Exception("The asset associated with " + dID.toString() + " does not exist");
        }
        if (!type.equals(Method.TYPE)) {
            throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
        }
        //
        Collection<String> ids = Method.inUseByExMethods(executor(), dID, topLevel, pdist);
        if (ids!=null) {
        	for (String id : ids) {
        		w.add("id", id);
        	}
        }
    }

}
