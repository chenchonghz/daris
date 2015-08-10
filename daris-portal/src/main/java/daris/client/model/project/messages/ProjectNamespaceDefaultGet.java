package daris.client.model.project.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ProjectNamespaceDefaultGet extends ObjectMessage<String> {

    public static final String SERVICE_NAME = "daris.project.namespace.default.get";

    public ProjectNamespaceDefaultGet() {

    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected String instantiate(XmlElement xe) throws Throwable {
        return xe.value("namespace");
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return null;
    }

}
