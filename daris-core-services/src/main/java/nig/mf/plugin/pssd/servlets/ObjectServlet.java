package nig.mf.plugin.pssd.servlets;

import java.util.HashMap;
import java.util.Map;

import nig.mf.plugin.pssd.servlets.modules.Module;
import nig.mf.plugin.pssd.servlets.modules.ObjectDescribeModule;
import nig.mf.plugin.pssd.servlets.modules.ObjectDownloadModule;
import nig.mf.plugin.pssd.servlets.modules.ObjectListModule;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;

public class ObjectServlet extends AbstractServlet {

    public static final String PATH = "object.mfjp";

    public static final String URL_BASE = ROOT + "/" + PATH;

    public static final String NAME = "daris.object";

    public static final String DESCRIPTION = "Describes the metadata for the specified object.";

    public static final String ARG_CID = "cid";

    public static final String ARG_MODULE = "module";

    public static final String ARG_FORMAT = "format";

    public static final String ARG_SORT = "sort";

    public static final String ARG_PROCEED = "proceed";

    public static final String ARG_RECURSIVE = "recursive";

    public static final String ARG_TRANSCODE = "transcode";

    public static final String ARG_DISPOSITION = "disposition";

    public static final String ARG_FILENAME = "filename";

    public static final String TRANSCODE_VALUE_SEPARATOR = "-";

    public static final String TRANSCODE_TOKEN_SEPARATOR = "|";

    public static enum ModuleName {

        describe, list, download;
        public static ModuleName parse(HttpRequest request, ModuleName defaultModuleName) {
            String name = request.variableValue(ARG_MODULE);
            ModuleName moduleName = parse(name);
            if (moduleName == null) {
                return defaultModuleName;
            }
            return moduleName;
        }

        public static ModuleName parse(String name) {
            if (name != null) {
                ModuleName[] vs = values();
                for (ModuleName v : vs) {
                    if (v.name().equalsIgnoreCase(name)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    public ObjectServlet() {
        super();
        arguments().add(ARG_CID, CiteableIdType.DEFAULT, "The citeable id of the object.", 1);

        arguments().add(ARG_MODULE, new EnumType(ModuleName.values()),
                "The module to execute. Can be describe, list and download. Defaults to describe",
                0);

        arguments()
                .add(ARG_FORMAT,
                        new EnumType(OutputFormat.values()),
                        "Output format. Can be xml or html. Defaults to html. This argument is only for module: describe.",
                        0);

        arguments()
                .add(ARG_SORT,
                        BooleanType.DEFAULT,
                        "Sorts the result members by their id. Defaults to true. This argument is only for moduel: list.",
                        0);

        arguments()
                .add(ARG_PROCEED,
                        BooleanType.DEFAULT,
                        "Set to true to proceed when downloading the object, otherwise, returns to the download settings page. Defaults to true. This argument applies only for module: download.",
                        0);

        arguments()
                .add(ARG_RECURSIVE,
                        BooleanType.DEFAULT,
                        "Set to true to include all the descendants when downloading the object. Defaults to false. This argument applies only for module: download.",
                        0);
        arguments()
                .add(ARG_TRANSCODE,
                        StringType.DEFAULT,
                        "The transcodes to be applied when downloading the object. The value of this argument is in the form of from_type1-to_type1|from_type2-to_type2. This argument applies only for module: download.",
                        0);
        arguments()
                .add(ARG_DISPOSITION,
                        new EnumType(Disposition.values()),
                        "How the content/archive should be treated by the caller. Defaults to attachment. This argument applies only for module: download.",
                        0);
        arguments()
                .add(ARG_FILENAME,
                        StringType.DEFAULT,
                        "Name for the content/archive file. Defaults to the object citeable id. This argument applies only for module: download.",
                        0);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected String path() {
        return PATH;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        ModuleName moduleName = ModuleName.parse(request, ModuleName.describe);
        Module module = null;
        switch (moduleName) {
        case describe:
            module = ObjectDescribeModule.INSTANCE;
            break;
        case list:
            module = ObjectListModule.INSTANCE;
            break;
        case download:
            module = ObjectDownloadModule.INSTANCE;
            break;
        default:
            module = ObjectDescribeModule.INSTANCE;
            break;
        }
        module.execute(server, sessionKey, request, response);
    }

    public static String urlFor(ModuleName moduleName, String cid, String skey, String token,
            String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ARG_MODULE, moduleName.name());
        map.put(ARG_CID, cid);
        map.put(ARG_SKEY, skey);
        map.put(ARG_TOKEN, token);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                String name = args[i];
                String value = args[i + 1];
                if (name != null && value != null) {
                    map.put(name, value);
                }
            }
        }
        return urlFor(URL_BASE, map);
    }

    public static String urlFor(ModuleName moduleName, String cid, String skey, String token,
            Map<String, String> args) {
        if (args == null) {
            args = new HashMap<String, String>();
        }
        args.put(ARG_MODULE, moduleName.name());
        args.put(ARG_CID, cid);
        args.put(ARG_SKEY, skey);
        args.put(ARG_TOKEN, token);
        return urlFor(URL_BASE, args);
    }

}
