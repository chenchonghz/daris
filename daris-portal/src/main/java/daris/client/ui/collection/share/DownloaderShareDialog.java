package daris.client.ui.collection.share;

import java.util.List;

import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.Output;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.model.authentication.Actor;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.collection.download.DownloaderSettings;
import daris.client.model.object.DObjectRef;
import daris.client.model.user.Self;

public class DownloaderShareDialog implements AsynchronousAction {

    private DObjectRef _obj;
    private DownloaderSettings _settings;

    private DownloaderShareForm _form;

    public DownloaderShareDialog(DObjectRef obj) {
        _obj = obj;
        _settings = new DownloaderSettings();
        _settings.addObject(_obj);
        _form = new DownloaderShareForm(_obj, _settings);
    }

    public void show(arc.gui.window.Window owner) {
        DialogProperties dp = new DialogProperties("Generate sharable downloader URL", _form);
        dp.setButtonAction(this);
        dp.setButtonLabel("Generate");
        dp.setCancelLabel("Dismiss");
        dp.setModal(false);
        dp.setActionEnabled(true);
        dp.setOwner(owner);
        dp.setSize(700, 500);
        Dialog.postDialog(dp).show();

    }

    @Override
    public void execute(final ActionListener al) {
        Self.getActor(new ObjectResolveHandler<Actor>() {
            @Override
            public void resolved(Actor actor) {
                generateToken(actor, al);
            }
        });
    }

    private void generateToken(Actor actor, final ActionListener al) {

        XmlStringWriter w = new XmlStringWriter();
        w.add("role", new String[] { "type", actor.actorType() }, actor.actorName());
        w.add("role", new String[] { "type", "role" }, "user");
        w.push("service", new String[] { "name", "daris.downloader.get" });
        _settings.save(w);
        w.pop();
        w.add("min-token-length", 20);
        w.add("max-token-length", 20);
        w.add("grant-caller-transient-roles", true);

        if (_settings.generateToken() && _settings.tokenExpiry() != null) {
            w.add("to", _settings.tokenExpiry());
        }
        if (_settings.generateToken() && _settings.tokenUseCount() > 0) {
            w.add("use-count", _settings.tokenUseCount());
        }
        w.add("tag", "daris-dowloader-sharable-url-" + _obj.id());
        Session.execute("secure.identity.token.create", w.document(), new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                String token = xe.value("token");
                _form.setUrl(urlFor(token));
                al.executed(false);
            }
        });

    }

    private String urlFor(String token) {
        return urlFor(token, _settings.targetPlatform().filename());
    }

    private static String urlFor(String token, String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append(com.google.gwt.user.client.Window.Location.getProtocol());
        sb.append("//");
        sb.append(com.google.gwt.user.client.Window.Location.getHost());
        sb.append("/mflux/execute.mfjp?token=");
        sb.append(token);
        sb.append("&filename=");
        sb.append(filename);
        return sb.toString();
    }
}
