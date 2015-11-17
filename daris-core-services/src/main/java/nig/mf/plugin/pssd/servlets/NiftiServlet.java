package nig.mf.plugin.pssd.servlets;

import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import nig.mf.plugin.pssd.servlets.modules.Module;
import nig.mf.plugin.pssd.servlets.modules.NiftiFileGetModule;
import nig.mf.plugin.pssd.servlets.modules.NiftiHeaderGetModule;
import nig.mf.plugin.pssd.servlets.modules.NiftiViewModule;

public class NiftiServlet extends AbstractServlet {
    public static final String PATH = "nifti.mfjp";

    public static final String URL_BASE = ROOT + "/" + PATH;

    public static final String NAME = "daris.nifti";

    public static final String DESCRIPTION = "Retrieve the NIFTI file, header or image from a NIFTI dataset.";

    public static final String ARG_CID = "cid";

    public static final String ARG_ID = "id";

    public static final String ARG_IDX = "idx";

    public static final String ARG_MODULE = "module";

    public static final String ARG_DISPOSITION = "disposition";

    public static final String ARG_FILENAME = "filename";

    public static enum ModuleName {

        file, header, view;
        public static ModuleName parse(HttpRequest request,
                ModuleName defaultModuleName) {
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

    public NiftiServlet() {
        super();
        arguments().add(ARG_ID, CiteableIdType.DEFAULT,
                "The asset id of the DICOM series.", 0);

        arguments().add(ARG_CID, CiteableIdType.DEFAULT,
                "The citeable id of the DICOM dataset/series.", 0);

        arguments().add(ARG_MODULE, new EnumType(ModuleName.values()),
                "The module to execute. Can be 'file', 'header' or 'view'. Defaults to 'view'.",
                0);

        arguments().add(ARG_IDX, new EnumType(ModuleName.values()),
                "This specifies the idx'th file in the DICOM dataset/series(archive). Defaults to one.",
                0);

        arguments().add(ARG_DISPOSITION, new EnumType(Disposition.values()),
                "How the content/archive should be treated by the caller. Defaults to attachment. This argument applies only for module: download.",
                0);

        arguments().add(ARG_FILENAME, StringType.DEFAULT,
                "Name for the content/archive file. Defaults to the object citeable id. This argument applies only for module: download.",
                0);
    }

    @Override
    protected void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        if (request.variableValue(ARG_ID) == null
                && request.variableValue(ARG_CID) == null) {
            throw new Exception(
                    "Either asset id or cid is required. Found none.");
        }
        if (request.variableValue(ARG_ID) != null
                && request.variableValue(ARG_CID) != null) {
            throw new Exception(
                    "Either asset id or cid is required. Found both.");
        }
        ModuleName moduleName = ModuleName.parse(request, ModuleName.file);
        Module module = null;
        switch (moduleName) {
        case file:
            module = NiftiFileGetModule.INSTANCE;
            break;
        case header:
            module = NiftiHeaderGetModule.INSTANCE;
            break;
        case view:
            module = NiftiViewModule.INSTANCE;
        default:
            module = NiftiViewModule.INSTANCE;
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