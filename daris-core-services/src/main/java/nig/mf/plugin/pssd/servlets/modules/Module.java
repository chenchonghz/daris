package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;

public interface Module {

    String name();

    void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable;

}
