package daris.plugin.services;

import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectAttach extends PluginService {

    public static final String RELATIONSHIP_TYPE = "attachment";
    public static final String INVERSE_RELATIONSHIP_TYPE = "attached-to";

    public static final String SERVICE_NAME = "daris.object.attach";

    public static enum IfExists {
        RENAME, ERROR;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static String[] stringValues() {
            IfExists[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].toString();
            }
            return svs;
        }

        public static IfExists fromString(String s, IfExists defaultAction) {
            if (s != null) {
                IfExists[] vs = values();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i].toString().equalsIgnoreCase(s)) {
                        return vs[i];
                    }
                }
            }
            return defaultAction;
        }
    }

    private Interface _defn;

    public SvcObjectAttach() {

        _defn = new Interface();

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object to attach to.", 0,
                1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the object to attach to.", 0, 1));

        Interface.Element attachment = new Interface.Element("attachment", XmlDocType.DEFAULT,
                "The attachement details", 1, 1);
        attachment.add(new Interface.Element("name", StringType.DEFAULT, "The name of the attachment file.", 1, 1));
        attachment.add(new Interface.Element("description", StringType.DEFAULT,
                "The description of the attachment file.", 0, 1));
        _defn.add(attachment);
        _defn.add(new Interface.Element("ifexists", new EnumType(IfExists.stringValues()),
                "What to do if the attachment with the same name exists. Defaults to rename.", 0, 1));

    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Attach local files to the specified object. The name and uri of the attachment file must be supplied.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public int minNumberOfInputs() {

        return 1;
    }

    public int maxNumberOfInputs() {

        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs inputs, Outputs out, XmlWriter w) throws Throwable {

        SimpleEntry<String, String> ids = ServiceUtils.getObjectIdentifiers(executor(), args);
        String id = ids.getKey();
        IfExists ifExists = IfExists.fromString(args.value("ifexists"), IfExists.RENAME);
        String attachmentName = args.value("attachment/name");
        String attachmentDescription = args.value("attachment/description");

        XmlDoc.Element ae = ServiceUtils.getAssetMeta(executor(), id, null);
        String namespace = ae.value("namespace");

        boolean exists = ServiceUtils.assetExists(executor(), "path=" + namespace + "/" + attachmentName);

        if (exists) {
            if (ifExists == IfExists.ERROR) {
                throw new Exception(
                        "Asset with name '" + attachmentName + "' already exists in namespace: " + namespace + ".");
            }
        }

        attach(executor(), id, namespace, attachmentName, attachmentDescription, inputs.input(0), exists, w);
    }

    public static void attach(ServiceExecutor executor, String objectAssetId, String namespace, String attachmentName,
            String attachmentDescription, PluginService.Input input, boolean rename, XmlWriter w) throws Throwable {
        /*
         * create the asset without name
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        if (!rename) {
            dm.add("name", attachmentName);
        }
        dm.add("namespace", namespace);
        if (attachmentDescription != null) {
            dm.add("description", attachmentDescription);
        }
        dm.push("related");
        dm.add("from", new String[] { "relationship", RELATIONSHIP_TYPE }, objectAssetId);
        dm.pop();
        String attachmentAssetId = executor.execute("asset.create", dm.root(), new Inputs(input), null).value("id");
        if (!rename) {
            return;
        }

        /*
         * set the asset name
         */
        dm = new XmlDocMaker("args");
        if (rename) {
            attachmentName = attachmentAssetId + "_" + attachmentName;
        }
        dm.add("name", attachmentName);
        dm.add("id", attachmentAssetId);
        executor.execute("asset.set", dm.root());
        w.add(RELATIONSHIP_TYPE, new String[] { "id", attachmentAssetId, "name", attachmentName });
    }

}
