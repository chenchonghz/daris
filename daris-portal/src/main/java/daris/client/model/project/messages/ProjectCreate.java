package daris.client.model.project.messages;

import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.project.Project;
import daris.client.model.repository.RepositoryRef;

public class ProjectCreate extends DObjectCreate {

    public ProjectCreate(Project o) {

        super(RepositoryRef.INSTANCE, o);
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.project.create";
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        super.messageServiceArgs(w);
        // Turn on fillin for project creation.
        w.add("fillin", true);
    }

}
