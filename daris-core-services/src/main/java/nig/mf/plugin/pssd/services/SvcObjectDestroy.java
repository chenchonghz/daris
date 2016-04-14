package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.mf.plugin.pssd.user.Self;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectDestroy extends PluginService {
    private Interface _defn;

    public SvcObjectDestroy() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the local object.", 0, Integer.MAX_VALUE));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The asset id of the local object.", 0, Integer.MAX_VALUE));
        _defn.add(new Interface.Element(
                "destroy-cid",
                BooleanType.DEFAULT,
                "Destroy the CID as as well as the asset (defaults to false). Not destroying the CID allows it to be reused",
                0, 1));
        _defn.add(new Interface.Element(
                "check-remote-children",
                BooleanType.DEFAULT,
                "Check if object has children on remote peers (defaults to false). If so, cannot destroy.",
                0, 1));
        _defn.add(new Interface.Element(
                "ptag",
                StringType.DEFAULT,
                "When checking for remote children, only query peers with this ptag. If none, query all peers.",
                0, 1));
        _defn.add(new Interface.Element(
                "pdist",
                IntegerType.DEFAULT,
                "Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",
                0, 1));
        _defn.add(new Interface.Element(
                "destroy",
                BooleanType.DEFAULT,
                "If the object you are destroying is a Project, you must set this to true. Default is false.",
                0, 1));
        _defn.add(new Interface.Element(
                "hard-destroy",
                BooleanType.DEFAULT,
                "Hard destroy assets (defaults to false). The destroy type is controlled by the server property asset.soft.destroy. If that is set to true (soft destroy) then this over-rides that and hard destroys the assets.",
                0, 1));
    }

    public String name() {
        return "om.pssd.object.destroy";
    }

    public String description() {
        return "Destroys the local object (and children) on the local server. When check-remote-children is true, if the object has children on another server in a federation it will not destroy.  If the object is a Project or an RSubject, then all associated object-specific roles and invalid (dangling) ACLs (associated with these roles) are also destroyed. If the object is an R-Subject, it will not be destroyed if it is inuse with existing local Subjects.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {

        // Destroy service must be run locally (or via a peer command)
        // String proute = null; // local only
        Collection<String> assetIds = args.values("id");
        Collection<String> cids = args.values("cid");

        if ((cids == null || cids.isEmpty())
                && (assetIds == null || assetIds.isEmpty())) {
            throw new IllegalArgumentException(
                    "Either id argument or cid argument is required. Found none.");
        }

        boolean destroyCid = args.booleanValue("destroy-cid", false);
        String ptag = args.stringValue("ptag");
        String pdist = args.stringValue("pdist", "infinity");
        boolean checkRemoteChildren = args.booleanValue(
                "check-remote-children", false);
        boolean destroyProject = args.booleanValue("destroy", false);
        boolean hardDestroy = args.booleanValue("hard-destroy", false);

        if (cids != null) {
            for (String cid : cids) {
                destroyObject(executor(), cid, destroyCid, ptag, pdist,
                        checkRemoteChildren, destroyProject, hardDestroy);
            }
        }

        if (assetIds != null) {
            for (String assetId : assetIds) {
                String cid = executor().execute("asset.get",
                        "<args><id>" + assetId + "</id></args>", null, null)
                        .value("asset/cid");
                if (cid != null) {
                    destroyObject(executor(), cid, destroyCid, ptag, pdist,
                            checkRemoteChildren, destroyProject, hardDestroy);
                }
            }
        }

    }

    public static void destroyObject(ServiceExecutor executor,
            final String cid, final boolean destroyCid, String ptag,
            String pdist, boolean checkRemoteChildren, boolean destroyProject,
            final boolean hardDestroy) throws Throwable {

        // Must hold an "admin" role on local server
        Self.isAdmin(cid, false);

        DistributedAsset dID = new DistributedAsset(null, cid);
        String objectType = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor,
                dID);
        if (objectType == null) {
            throw new Exception("The asset associated with " + dID.toString()
                    + " does not exist or is not a PSSD object");

        }

        // TRy not to destroy the repository :-)
        int depth = CiteableIdUtil.getIdDepth(cid);
        if (depth < CiteableIdUtil.projectDepth()) {
            throw new Exception(
                    "You are attempting to destroy objects at a level higher than Projects (e.g. the entire repository). This is not allowed");
        }

        // See if have children somewhere in federation. If it does, you can't
        // destroy this
        // object without first destroying them. Perhaps this is not a good
        // policy
        // and it should just destroy all remote assets too...
        //
        // If the object is a replica, its children must be replica. If the
        // object
        // is primary, its children could be primary or replicas.

        if (checkRemoteChildren) {
            ResultAssetType assetType = ResultAssetType.all;
            if (dID.isReplica())
                assetType = ResultAssetType.replica;

            // The following code is a problem with system:manager which does
            // not have
            // full federation rights. Fix this ?
            if (dID.hasRemoteChildren(assetType, ptag, pdist)) {
                if (dID.isReplica()) {
                    throw new Exception(
                            "CID "
                                    + cid
                                    + " has replica children on other servers in the federation. Cannot destroy.");

                } else {
                    throw new Exception(
                            "CID "
                                    + cid
                                    + " has (primary/replica) children on other servers in the federation. Cannot destroy.");
                }
            }
        }

        // Project and RSubject objects are special. Find out if we have one

        final boolean isProject = Project.isObjectProject(executor, dID);
        final boolean isRSubject = isProject ? false : RSubject
                .isObjectRSubject(executor, dID);
        if (isProject && !destroyProject) {
            throw new Exception(
                    "You must set argument 'destroy' to true to destroy a whole Project tree.");
        }

        // Abandon if RSubject is related to SUbjects (must be local)
        if (isRSubject) {
            if (RSubject.hasRelatedSubjects(executor, dID)) {
                throw new Exception("The RSubject " + cid
                        + " has related Subjects; wil not destroy");
            }
        }

        //
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><cid>" + cid + "</cid></args>", null, null).element(
                "asset");
        final String ns = ae.value("namespace");

        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                // Destroy the assets
                XmlDocMaker dm = new XmlDocMaker("args");
                dm.add("cid", cid);
                dm.add("members", true);
                if (hardDestroy) {
                    executor.execute("asset.hard.destroy", dm.root());
                } else {
                    executor.execute("asset.destroy", dm.root());
                }

                // Destroy all local roles associated with the object and clean
                // up any
                // dangling ACLs
                if (isProject) {
                    System.out
                            .println("The objects is a project - destroy roles");
                    Project.destroyRoles(executor, cid);
                    PSSDUtils.removeInvalidACLs(executor);
                    // destroy project specific tag dictionaries if any.
                    ProjectSpecificTagDictionary.destroyAll(executor, cid);
                } else if (isRSubject) {
                    RSubject.destroyRoles(executor, cid);
                    PSSDUtils.removeInvalidACLs(executor);
                }

                // Destroy CID as well if desired
                if (destroyCid) {
                    nig.mf.pssd.plugin.util.CiteableIdUtil.destroyCID(null,
                            executor, cid);
                }
                // Destroy Namespace
                if (ns.endsWith("/" + cid)) {
                    if (executor.execute(
                            "asset.query",
                            "<args><action>count</action><where>namespace>='"
                                    + ns + "'</where></args>", null, null)
                            .intValue("value") == 0) {
                        executor.execute("asset.namespace.destroy",
                                "<args><namespace>" + ns
                                        + "</namespace><force>true</force></args>", null, null);
                    }
                }
                // Generate system event
                SystemEventChannel.generate(new PSSDObjectEvent(Action.DESTROY,
                        cid, SvcCollectionMemberCount.countMembers(executor,
                                cid)));

                return false;
            }
        }).execute(executor);

    }

}
