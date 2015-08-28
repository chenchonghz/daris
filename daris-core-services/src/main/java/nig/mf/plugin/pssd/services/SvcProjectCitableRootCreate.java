package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcProjectCitableRootCreate extends PluginService {

    public static final String DICT_CID_ROOT_NAMES = "daris:pssd.project.cid.rootnames";

    public static final String SERVICE_NAME = "daris.project.citeable.root.create";
    public static final String SERVICE_DESCRIPTION = "Creates a new citeable root for daris projects with given name.";

    private Interface _defn;

    public SvcProjectCitableRootCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The citeable name.", 1, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String name = args.value("name");
        executor().execute("citeable.named.id.create",
                "<args><name>" + name + "</name></args>", null, null);
        executor().execute(
                "dictionary.entry.add",
                "<args><dictionary>" + DICT_CID_ROOT_NAMES
                        + "</dictionary><term>" + name + "</term></args>",
                null, null);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
