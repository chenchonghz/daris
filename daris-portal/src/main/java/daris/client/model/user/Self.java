package daris.client.model.user;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.model.authentication.Actor;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

public class Self {

    public static void getActor(final ObjectResolveHandler<Actor> rh) {

        Session.execute("actor.self.describe", new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs)
                    throws Throwable {
                if (xe != null) {
                    String actorType = xe.value("actor/@type");
                    String actorName = xe.value("actor/@name");
                    if (actorName != null) {
                        rh.resolved(new Actor(actorName, actorType));
                        return;
                    }
                }
                rh.resolved(null);
            }
        });
    }

}
