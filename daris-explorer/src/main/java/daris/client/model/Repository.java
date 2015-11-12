package daris.client.model;

import arc.xml.XmlDoc;

public class Repository extends DObject {

    private String _descriptionAssetId;
    private String _acronym;
    private String _name;
    private int _numberOfProjects;

    public Repository(XmlDoc.Element re) throws Throwable {
        super(re);
        _descriptionAssetId = re.value("@id");
        _acronym = re.value("name/@acronym");
        _name = re.value("name");
        _numberOfProjects = re.intValue("number-of-projects");
    }

    public String acronym() {
        return _acronym;
    }

    @Override
    public final String name() {
        return _name;
    }

    @Override
    public String description() {
        return _name;
    }

    @Override
    public String assetId() {
        return _descriptionAssetId;
    }

    public int numberOfProjects() {
        return _numberOfProjects;
    }

    @Override
    public int numberOfChildren() {
        return numberOfProjects();
    }

    @Override
    public Type type() {
        return DObject.Type.REPOSITORY;
    }

}
