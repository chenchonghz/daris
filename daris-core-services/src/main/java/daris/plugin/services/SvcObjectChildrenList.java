package daris.plugin.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.project.DataUse;
import nig.mf.plugin.pssd.services.SvcObjectFind;
import nig.mf.plugin.pssd.user.ModelUserRoleSet;

public class SvcObjectChildrenList extends PluginService {

    public static final String SERVICE_NAME = "daris.object.children.list";

    private static final int PIPE_NB_THREADS = 4;

    private Interface _defn;

    public SvcObjectChildrenList() {

        _defn = new Interface();

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the parent object.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the parent object.", 0, 1));
        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Absolute cursor position.", 0, 1));

        Interface.Element seekTo = new Interface.Element("seek-to", XmlDocType.DEFAULT,
                "A child object to seek to from the given position. This is used for cursors to position to a page containing a specific asset. If using a cursor, specify either 'idx' or 'seek-to', but not both.",
                0, 1);
        seekTo.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the object to seek to.", 0, 1));
        seekTo.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object to seek to.", 0,
                1));
        _defn.add(seekTo);

        _defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
                "Number of results to return. Defaults to 100.", 0, 1));

        addSortArgument(_defn);

        _defn.add(new Interface.Element("count", BooleanType.DEFAULT,
                "If set, returns the number of matches in the set. Defaults to true.", 0, 1));

    }

    static void addSortArgument(Interface defn) {

        Interface.Element sort = new Interface.Element("sort", XmlDocType.DEFAULT, "Sort keys.", 0, 1);
        Interface.Element key = new Interface.Element("key", StringType.DEFAULT,
                "XPath of an document element or attribute to sort by. If not specified, the sort order applies to the asset modification times.",
                1, Integer.MAX_VALUE);
        key.add(new Interface.Attribute("order", new EnumType(new String[] { "asc", "desc" }),
                "Sort ascending or descending. Default is the global order.", 0));
        sort.add(key);
        sort.add(new Interface.Element("nulls", new EnumType(new String[] { "include", "exclude" }),
                "When sorting should paths with null values be included or excluded from the result set.", 0, 1));
        sort.add(new Interface.Element("order", new EnumType(new String[] { "asc", "desc" }),
                "Sort ascending or descending. Default is ascending.", 0, 1));
        defn.add(sort);
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
        return "Lists direct children of the specified object.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        String id = args.value("id");
        String cid = args.value("cid");
        PSSDObject.Type parentType = null;
        Long idx = args.longValue("idx", null);
        int size = args.intValue("size", 100);
        String seekTo = null;
        boolean count = args.booleanValue("count", true);
        if (args.elementExists("seek-to")) {
            String seekToId = args.value("seek-to/id");
            String seekToCid = args.value("seek-to/cid");
            if (seekToId == null && seekToCid == null) {
                throw new IllegalArgumentException("Missing argument: seek-to/id or seek-to/cid.");
            }
            if (seekToId != null && seekToCid != null) {
                throw new IllegalArgumentException("Expects argument: seek-to/id or seek-to/cid, but not both.");
            }
            if (seekToId == null) {
                seekToId = executor().execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null)
                        .value("asset/@id");
            }
            seekTo = seekToId;
        }
        if (seekTo == null && idx == null) {
            idx = 1L;
        }
        if (id != null && cid != null) {
            throw new IllegalArgumentException("Expects argument cid or id, but not both.");
        }

        StringBuilder sb = new StringBuilder();
        if (id == null && cid == null) {
            sb.append("model='om.pssd.project'");
        } else {
            if (cid == null) {
                XmlDoc.Element ae = executor().execute("asset.get", "<args><id>" + id + "</id></args>", null, null)
                        .element("asset");
                cid = ae.value("cid");
            }
            parentType = PSSDObject.Type.fromId(cid);
            sb.append("cid in '").append(cid).append("'");
        }

        String where = sb.toString();
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        if (idx != null) {
            dm.add("idx", idx);
        }
        if (seekTo != null) {
            dm.add("seek-to", seekTo);
        }
        dm.add("size", size);
        if (args.elementExists("sort")) {
            dm.add(args.element("sort"), true);
        }
        dm.add("action", "get-value");
        dm.add("entity", "object");
        dm.add("xpath", new String[] { "ename", "type" }, "meta/daris:pssd-object/type");
        dm.add("xpath", new String[] { "ename", "cid" }, "cid");
        dm.add("xpath", new String[] { "ename", "name" }, "meta/daris:pssd-object/name");
        if (parentType == PSSDObject.Type.project) {
            dm.add("xpath", new String[] { "ename", "subject-data-use" }, "meta/daris:pssd-subject/data-use");
        }
        if (parentType == PSSDObject.Type.study) {
            dm.add("xpath", new String[] { "ename", "dataset-type" }, "meta/daris:pssd-dataset/type");
            dm.add("xpath", new String[] { "ename", "dataset-processed" }, "meta/daris:pssd-derivation/processed");
        }
        dm.add("count", count);
        XmlDoc.Element re = executor().execute("asset.query", dm.root());

        if (re.elementExists("object")) {
            ModelUserRoleSet selfRoles = parentType == PSSDObject.Type.project ? ModelUser.selfRoles(executor()) : null;
            XmlDoc.Element nbcs = countChildren(executor(), where, idx, seekTo, size, args.element("sort"));
            List<XmlDoc.Element> oes = re.elements("object");
            for (XmlDoc.Element oe : oes) {
                if (parentType == PSSDObject.Type.project) {
                    DataUse subjectDataUse = DataUse.instantiate(oe.value("subject-data-use"));
                    if (!SvcObjectFind.validateSubjectDataUse(cid, subjectDataUse, selfRoles)) {
                        // violates subject data-use, skip it.
                        continue;
                    }
                }
                // @formatter:off
                w.add("object",
                        new String[] { "cid", oe.value("cid"), 
                                "id", oe.value("@id"), 
                                "type", oe.value("type"), 
                                "nbc", nbcs.value("nbc[@cid='" + oe.value("cid") + "']"), 
                                "dataset-type", oe.value("dataset-type"), 
                                "dataset-processed", oe.value("dataset-processed"), 
                                "name", oe.value("name") });
                // @formatter:on
            }
        }
        w.add(re.element("cursor"), true);
    }

    private XmlDoc.Element countChildren(ServiceExecutor executor, String where, Long idx, String seekTo, int size,
            XmlDoc.Element sort) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        if (idx != null) {
            dm.add("idx", idx);
        }
        if (seekTo != null) {
            dm.add("seek-to", seekTo);
        }
        dm.add("size", size);
        if (sort != null) {
            dm.add(sort, true);
        }
        dm.add("action", "pipe");
        dm.add("service", new String[] { "name", SvcObjectChildrenCount.SERVICE_NAME });
        dm.add("pipe-generate-result-xml", true);
        dm.add("pipe-nb-threads", PIPE_NB_THREADS);
        return executor.execute("asset.query", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
