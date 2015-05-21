package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.pssd.plugin.util.DistributedAsset;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcSubjectMethodReplace extends PluginService {

    private Interface _defn;

    public SvcSubjectMethodReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the Subject (managed by the local server). ", 1, 1));
        _defn.add(new Interface.Element("method", CiteableIdType.DEFAULT,
                "The identity of the Method (managed by the local server). Defaults to the existing Method used to create the Subject (which may have been updated). ", 0, 1));
        _defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
                "Set to true will replace the method for the descendent ExMethods and their children Studies. Defaults to false.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Replaces the method for the subject. Only operates on primary objects and their primary children.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {

        final String sid = args.value("id");
        DistributedAsset dSID = new DistributedAsset (null, sid);
        if (dSID.isReplica()) {
        	throw new Exception ("This service cannot operate on replica objects");
        }
        //
        final String mid = args.value("method");
        final boolean recursive = args.booleanValue("recursive", false);
        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {

                // Gets the current method id.
                XmlDocMaker dm = new XmlDocMaker("args");
                dm.add("cid", sid);
                String oldMid = executor.execute("asset.get", dm.root()).value("asset/meta/daris:pssd-subject/method");
                String newMid = null;
                if (mid!=null) {
                	newMid = mid;
                } else {
                	newMid = oldMid;
                }

                // Replaces the method with the new one.
                dm = new XmlDocMaker("args");
                dm.add("cid", sid);
                dm.push("meta");
                dm.push("daris:pssd-subject");
                dm.add("method", newMid);
                dm.pop();
                dm.pop();
                executor.execute("asset.set", dm.root());

                // Replaces the template with the one defined in the new method.
                dm = new XmlDocMaker("args");
                dm.add("id", sid);
                dm.add("mid", newMid);
                executor.execute("om.pssd.subject.method.template.replace", dm.root());

                if (recursive) {

                    /*
                     * Replace the method in the ex-methods (primary only)
                     */
                    dm = new XmlDocMaker("args");
                    dm.add("where", "(cid in '" + sid + "') and (rid hasno value) and (xpath(daris:pssd-ex-method/method/id)='" + oldMid
                            + "')");
                    dm.add("action", "get-cid");
                    Collection<String> emids = executor.execute("asset.query", dm.root()).values("cid");
                    if (emids != null && !emids.isEmpty()) {
                        for (String emid : emids) {
                            executor.execute("om.pssd.ex-method.method.replace", "<args><id>" + emid + "</id><method>"
                                    + newMid + "</method><recursive>"+recursive+"</recursive></args>", null, null);
                        }
                    }
                }

                return false;
            }
        }).execute(executor());
    }

    @Override
    public String name() {
        return "om.pssd.subject.method.replace";
    }

}
