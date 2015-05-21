package daris.client.model.query.filter.pssd;

import daris.client.model.object.DObjectRef;

public interface ProjectAware {

    DObjectRef project();

    void setProject(DObjectRef project);

}
