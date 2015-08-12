package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcProjectRSubjectFind extends PluginService {
    private Interface _defn;

    public SvcProjectRSubjectFind() {

        _defn = new Interface();
        Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the project.", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);
        _defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
                "Specify type of asset to find. Defaults to all.", 0, 1));
    }

    public String name() {

        return "om.pssd.project.r-subject.find";
    }

    public String description() {

        return "Find all of the R-Subjects associated with the given Project.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        DistributedAsset dID = new DistributedAsset(args.element("id"));
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID));
        if (type == null) {
            throw new Exception("The asset associated with " + dID.toString() + " does not exist");
        }
        if (!type.equals(Project.TYPE)) {
            throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
        }
        //
        ResultAssetType assetType = ResultAssetType.instantiate(args.stringValue("asset-type",
                ResultAssetType.all.toString()));

        // Find and format
        RSubject.findProjectRelatedRSubjects(executor(), dID, assetType, w);
    }
}
