package daris.essentials;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMMetadataPopulate extends PluginService {

    public static enum IfExists {
        ignore, merge, replace;
        public static IfExists fromString(String s) {
            IfExists[] vs = values();
            for (IfExists v : vs) {
                if (v.toString().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            return null;
        }
    }

    public static final String SERVICE_NAME = "dicom.metadata.populate";

    private Interface _defn;

    public SvcDICOMMetadataPopulate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the DICOM series.", 0,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the DICOM series.", 0,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "The selection query - all the DICOM series selected by the query will be processed.", 0, 1));
        _defn.add(new Interface.Element("ignore-errors", BooleanType.DEFAULT,
                "Set to true to ignore the errors and continue to process the rest. Defaults to true.", 0, 1));
        _defn.add(new Interface.Element("idx", IntegerType.POSITIVE,
                "This specifies the idx'th file in the DICOM series archive. Defaults to zero.", 0, 1));
        _defn.add(new Interface.Element("tag", new StringType(Pattern.compile("[0-9a-fA-F]{8}")), "The DICOM element tag.", 1,
                Integer.MAX_VALUE));        
        _defn.add(new Interface.Element("doc-tag", StringType.DEFAULT,
                "The tag attribute for the daris:dicom-dataset document.", 0, 1));
        _defn.add(new Interface.Element("if-exists", new EnumType(IfExists.values()),
                "What if the metadata has already populated. Defaults to merge.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Retrieve the DICOM meta data from the file in the content archive of the DICOM series asset and populate the asset meta data (daris:dicom-dataset document).";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        boolean ignoreErrors = args.booleanValue("ignore-errors", true);
        int idx = args.intValue("idx", 0);
        Set<String> tags = new TreeSet<String>(args.values("tag"));
        IfExists ifExists = IfExists.fromString(args.stringValue("if-exists", IfExists.merge.toString()));
        String docTag = args.value("doc-tag");

        // validate arguments
        boolean hasIdArg = args.elementExists("id");
        boolean hasCidArg = args.elementExists("cid");
        boolean hasWhereArg = args.elementExists("where");
        if (hasIdArg && !hasCidArg && !hasWhereArg) {
            // ids
            Collection<String> ids = args.values("id");
            populate(ids, false, idx, tags, ignoreErrors, ifExists, docTag, w);
        } else if (!hasIdArg && hasCidArg && !hasWhereArg) {
            // cids
            Collection<String> cids = args.values("cid");
            populate(cids, true, idx, tags, ignoreErrors, ifExists, docTag, w);
        } else if (!hasIdArg && !hasCidArg && hasWhereArg) {
            // where
            String where = args.value("where");
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("where", "(" + where + ") and (mf-dicom-series has value) and (asset has content)");
            dm.add("size", "infinity");
            PluginTask.setCurrentThreadActivity("Querying for matching DICOM series.");
            Collection<String> ids = executor().execute("asset.query", dm.root()).values("id");
            PluginTask.clearCurrentThreadActivity();
            populate(ids, false, idx, tags, ignoreErrors, ifExists, docTag, w);
        } else if (!hasIdArg && !hasCidArg && !hasWhereArg) {
            throw new IllegalArgumentException("Expecting exactly one id, cid or where argument. Found none.");
        } else {
            throw new IllegalArgumentException("Expecting exactly one id, cid or where argument. Found more than one.");
        }
    }

    private void populate(Collection<String> ids, boolean citeable, int idx, Set<String> tags, boolean ignoreErrors,
            IfExists ifExists, String docTag, XmlWriter w) throws Throwable {

        int total = ids == null ? 0 : ids.size();
        int nbPopulated = 0;
        int nbErrors = 0;
        if (total == 0) {
            w.add("populated", new String[] { "total", "0", "error", "0" }, 0);
            return;
        }
        PluginTask.threadTaskBeginSetOf(total);
        for (String id : ids) {
            PluginTask.checkIfThreadTaskAborted();
            PluginTask.setCurrentThreadActivity("Check asset " + id + ".");
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add(citeable ? "cid" : "id", id);
            dm.add("lock", new String[] { "timeout", String.valueOf(300) }, true);
            XmlDoc.Element ae = executor().execute("asset.get", dm.root()).element("asset");
            if (!ae.elementExists("meta/mf-dicom-series") || !ae.elementExists("content")) {
                nbErrors++;
                if (ignoreErrors) {
                    continue;
                } else {
                    throw new Exception("Asset " + id + " is not a valid DICOM series.");
                }
            }
            boolean mdExists = ae.elementExists("meta/daris:dicom-dataset");
            if (ifExists == IfExists.ignore && mdExists) {
                nbPopulated++;
                continue;
            }
            PluginTask.clearCurrentThreadActivity();

            int nbSlices = ae.intValue("meta/mf-dicom-series/size", Integer.MAX_VALUE);
            if (idx > nbSlices - 1) {
                idx = nbSlices - 1;
            }
            String assetId = ae.value("@id");

            PluginTask.checkIfThreadTaskAborted();
            PluginTask.setCurrentThreadActivity("Retrieving DICOM metadata from asset (id=" + assetId + ")");
            dm = new XmlDocMaker("args");
            dm.add("id", new String[] { "idx", Integer.toString(idx) }, assetId);
            dm.add("defn", true);
            XmlDoc.Element re = null;
            try {
                re = executor().execute("dicom.metadata.get", dm.root());
            } catch (Throwable t) {
                nbErrors++;
                t.printStackTrace(System.out);
                if (ignoreErrors) {
                    continue;
                } else {
                    throw t;
                }
            }
            PluginTask.clearCurrentThreadActivity();

            PluginTask.checkIfThreadTaskAborted();

            dm = new XmlDocMaker("args");
            dm.add("id", assetId);
            dm.add("unlock", true);
            if (ifExists == IfExists.replace) {
                // NOTE: there is a bug in asset set when turning on
                // only-if-same-namespace attr.

                // dm.add("replace-all-meta", new String[] {
                // "only-if-same-name", String.valueOf(true),
                // "only-if-same-namespace", String.valueOf(true) }, true);

                dm.add("replace-all-meta", new String[] { "only-if-same-name", String.valueOf(true) }, true);
            }
            dm.push("meta");
            dm.push("daris:dicom-dataset", new String[] { "ns", "dicom", "tag", docTag });
            dm.push("object", new String[] { "idx", Integer.toString(idx) });
            for (String tag : tags) {
                XmlDoc.Element de = re.element("de[@tag='" + tag + "']");
                if (de != null && !de.booleanValue("value/@null")) {
                    dm.push("de", new String[] { "tag", tag, "type", de.value("@type") });
                    String defn = de.value("defn");
                    if (defn != null) {
                        dm.add("defn", defn);
                    }
                    Collection<String> values = de.values("value");
                    if (values != null) {
                        for (String v : values) {
                            dm.add("value", v);
                        }
                    }
                    dm.pop();
                }
            }
            dm.pop();
            dm.pop();
            dm.pop();
            PluginTask.setCurrentThreadActivity("Updating asset " + assetId);
            executor().execute("asset.set", dm.root(), null, null);
            PluginTask.clearCurrentThreadActivity();
            nbPopulated++;
            PluginTask.threadTaskCompletedMultipleOf(nbPopulated, total);
        }
        PluginTask.threadTaskCompleted();
        w.add("populated", new String[] { "total", Integer.toString(total), "error", Integer.toString(nbErrors) },
                nbPopulated);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public boolean canBeAborted() {
        return true;
    }
}
