package nig.mf.plugin.pssd.servlets;

import java.util.HashMap;
import java.util.Map;

import nig.mf.plugin.pssd.servlets.modules.Module;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartAbortModule;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartDescribeModule;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartDestroyModule;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartDownloadModule;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartListModule;
import nig.mf.plugin.pssd.servlets.modules.ShoppingCartOrderModule;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;

public class ShoppingCartServlet extends AbstractServlet {

    public static final String PATH = "shoppingcart.mfjp";

    public static final String URL_BASE = ROOT + "/" + PATH;

    public static final String NAME = "daris.shoppingcart";

    public static final String DESCRIPTION = "Describes the current status of the specified shopping cart.";

    public static final String ARG_SID = "sid";

    public static final String ARG_MODULE = "module";

    public static final String ARG_FORMAT = "format";

    public static final String ARG_DISPOSITION = "disposition";

    public static final String ARG_FILENAME = "filename";

    public static enum ModuleName {
        describe, order, abort, destroy, download, list;
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

    public static String urlFor(ModuleName moduleName, String sid, String skey, String token,
            String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ARG_MODULE, moduleName.name());
        map.put(ARG_SID, sid);
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

    public static String urlFor(ModuleName moduleName, String sid, String skey, String token,
            Map<String, String> args) {
        if (args == null) {
            args = new HashMap<String, String>();
        }
        args.put(ARG_MODULE, moduleName.name());
        args.put(ARG_SID, sid);
        args.put(ARG_SKEY, skey);
        args.put(ARG_TOKEN, token);
        return urlFor(URL_BASE, args);
    }

    public ShoppingCartServlet() {
        super();
        arguments().add(ARG_SID, LongType.POSITIVE_ONE, "The id of the shopping cart.", 0);

        arguments()
                .add(ARG_MODULE,
                        new EnumType(ModuleName.values()),
                        "The module to execute. Can be describe, proceed, abort, destroy and download. Defaults to describe.",
                        0);

        arguments()
                .add(ARG_FORMAT,
                        new EnumType(OutputFormat.values()),
                        "Output format. Can be xml or html. Defaults to html. This argument applies only for module: describe.",
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
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        ModuleName moduleName = ModuleName.parse(request, ModuleName.describe);
        String sid = request.variableValue(ARG_SID);
        if (moduleName != ModuleName.list && sid == null) {
            throw new Exception(ARG_SID + " argument is required by module " + moduleName
                    + " of servlet " + ShoppingCartServlet.URL_BASE);
        }
        Module module = null;
        switch (moduleName) {
        case describe:
            module = ShoppingCartDescribeModule.INSTANCE;
            break;
        case order:
            module = ShoppingCartOrderModule.INSTANCE;
            break;
        case abort:
            module = ShoppingCartAbortModule.INSTANCE;
            break;
        case destroy:
            module = ShoppingCartDestroyModule.INSTANCE;
            break;
        case download:
            module = ShoppingCartDownloadModule.INSTANCE;
            break;
        case list:
            module = ShoppingCartListModule.INSTANCE;
            break;
        default:
            if (sid != null) {
                // describe
                module = ShoppingCartDescribeModule.INSTANCE;
            } else {
                // list
                module = ShoppingCartListModule.INSTANCE;
            }
            break;
        }
        module.execute(server, sessionKey, request, response);
    }

    @Override
    protected String path() {
        return PATH;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String name() {
        return NAME;
    }

}
