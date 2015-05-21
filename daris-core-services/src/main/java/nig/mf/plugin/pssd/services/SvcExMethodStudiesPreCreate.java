package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.LinkedHashSet;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.iio.metadata.StudyMethodMetadata;
import nig.mf.Executor;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.util.PluginExecutor;
import nig.mf.pssd.plugin.util.DistributedAsset;

/**
 * Service to pre-create a Study container, setting all Method specified
 * meta-data if desired.
 * 
 * @author nebk
 * 
 */
public class SvcExMethodStudiesPreCreate extends PluginService {
    private Interface _defn;

    public SvcExMethodStudiesPreCreate() throws Throwable {
        _defn = new Interface();
        Interface.Element me = new Interface.Element("pid", CiteableIdType.DEFAULT,
                "The identity of the parent ExMethod.", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);
        _defn.add(new Interface.Element(
                "step",
                StringType.DEFAULT,
                "Step in the Method to create Study for (degenerate steps dropped). If not specified, one Study is made for each Study producing step in the Method. ",
                0, Integer.MAX_VALUE));
        _defn.add(new Interface.Element("method-meta", BooleanType.DEFAULT,
                "Set the meta-data pre-specified by the Method ? Defaults to true.", 0, 1));
    }

    public String name() {
        return "om.pssd.ex-method.studies.precreate";
    }

    public String description() {
        return "Creates PSSD studies on the local server for a given step for the given ExMethod. Populates the meta-data with any pre-specified by the Method.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        // Distributed ID for parent ExMethod. It must be a primary or we are
        // not allowed
        // to create children under it.
        DistributedAsset dEID = new DistributedAsset(args.element("pid"));

        // Validate
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dEID));
        if (type == null) {
            throw new Exception("The asset associated with " + dEID.toString() + " does not exist");
        }
        if (!type.equals(ExMethod.TYPE)) {
            throw new Exception("Object " + dEID.getCiteableID() + " [type=" + type + "] is not an " + ExMethod.TYPE);
        }
        if (dEID.isReplica()) {
            throw new Exception("The supplied parent ExMethod is a replica and this service cannot create its child");
        }

        Collection<String> steps = args.values("step");
        Boolean doMethodMeta = args.booleanValue("method-meta", true);

        // Fetch steps if not given for local ExMethod
        XmlDoc.Element id = dEID.asXmlDoc("id");
        if (steps == null) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add(id);
            XmlDoc.Element r = executor().execute("om.pssd.ex-method.study.step.find", dm.root());
            if (r == null)
                return;

            // Get steps
            steps = r.values("ex-method/step");
        } else {
            // Make list unique
            Collection<String> steps2 = new LinkedHashSet<String>(steps);
            steps = steps2;
        }

        // Create for given steps
        for (String step : steps) {

            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add(id);
            dm.add("step", step);

            // Exception if step does not exist
            XmlDoc.Element r = executor().execute("om.pssd.ex-method.step.describe", dm.root());

            //
            XmlDoc.Element r2 = r.element("ex-method/step/study");
            if (r2 == null) {
                throw new Exception("Step " + step + " is not a Study making step");
            }

            // Make local Study
            createStudy(dEID, step, doMethodMeta, w);
        }

    }

    private void createStudy(DistributedAsset dEID, String step, Boolean doMethodMeta, XmlWriter w) throws Throwable {
        // Prepare creation XML
        XmlDocMaker dm2 = new XmlDocMaker("args");
        dm2.add(dEID.asXmlDoc("pid"));
        dm2.add("step", step);

        // Set the meta-data pre-specified by the Method.
        if (doMethodMeta) {
            Executor pExecutor = new PluginExecutor(executor());
            StudyMethodMetadata.addStudyMethodMeta(pExecutor, dEID, step, dm2);
        }

        // Create
        XmlDoc.Element r = executor().execute("om.pssd.study.create", dm2.root());
        String id = r.value("id");
        XmlDoc.Element t = new XmlDoc.Element("id", id);
        XmlDoc.Attribute a = new XmlDoc.Attribute("step", step);
        t.add(a);
        w.add(t);
    }
}
