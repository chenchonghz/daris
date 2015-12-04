package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcCollectionTranscodeList extends PluginService {

    public static final String SERVICE_NAME = "daris.collection.transcode.list";

    private Interface _defn;

    public SvcCollectionTranscodeList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the root/parent object.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "List the available transcodes for the objects in the specified collection.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        SortedSet<String> types = SvcCollectionTypeList.listTypes(executor(),
                cid, "asset has content");
        if (types != null) {
            for (String type : types) {
                List<String> transcodeToTypes = transcodeToTypesFor(executor(),
                        type);
                if (transcodeToTypes != null && !transcodeToTypes.isEmpty()) {
                    w.push("transcode", new String[] { "from", type });
                    for (String toType : transcodeToTypes) {
                        w.add("to", toType);
                    }
                    w.pop();
                }
            }
        }
    }

    public static List<String> transcodeToTypesFor(ServiceExecutor executor,
            String type) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("from", type);

        Collection<String> toTypes = executor
                .execute("asset.transcode.describe", dm.root())
                .values("transcode/to");
        if (toTypes != null && !toTypes.isEmpty()) {
            return new ArrayList<String>(toTypes);
        }
        return null;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
