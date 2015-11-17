package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.services.SvcArchiveContentList;
import nig.mf.plugin.pssd.servlets.ArchiveServlet;

public class ArchiveEntryListModule implements Module {

    public static final ArchiveEntryListModule INSTANCE = new ArchiveEntryListModule();

    public static final String NAME = ArchiveServlet.ModuleName.list.name();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // id
        String id = request.variableValue(ArchiveServlet.ARG_ID);
        // cid
        String cid = request.variableValue(ArchiveServlet.ARG_CID);

        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        XmlDoc.Element re = server.execute(sessionKey,
                SvcArchiveContentList.SERVICE_NAME, dm.root());
        response.setContent(re.toString(), "text/xml");
    }
}
