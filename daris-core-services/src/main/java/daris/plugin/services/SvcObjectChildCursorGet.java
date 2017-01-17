package daris.plugin.services;

import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.client.util.CiteableIdUtil;

public class SvcObjectChildCursorGet extends PluginService {

    public static final String SERVICE_NAME = "daris.object.child.cursor.get";

    private Interface _defn;

    public SvcObjectChildCursorGet() {
        _defn = new Interface();

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the child object.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the child object.", 0, 1));

        _defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
                "Number of results to return. Defaults to 100.", 0, 1));

        SvcObjectChildrenList.addSortArgument(_defn);
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
        return "Gets the cursor position for the specified child object when listing all children of its parent.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        SimpleEntry<String, String> ids = ServiceUtils.getObjectIdentifiers(executor(), args);
        String id = ids.getKey();
        String cid = ids.getValue();

        String pid = CiteableIdUtil.isProjectId(cid) ? null : CiteableIdUtil.getParentId(cid);

        int size = args.intValue("size", 100);

        StringBuilder sb = new StringBuilder();
        if (pid == null) {
            sb.append("model='om.pssd.project'");
        } else {
            sb.append("cid in '").append(pid).append("'");
        }
        String where = sb.toString();

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        dm.add("seek-to", id);
        dm.add("size", size);
        if (args.elementExists("sort")) {
            dm.add(args.element("sort"), true);
        }
        dm.add("count", true);
        dm.add("action", "get-id");
        XmlDoc.Element re = executor().execute("asset.query", dm.root());
        w.add(re.element("cursor"), true);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
