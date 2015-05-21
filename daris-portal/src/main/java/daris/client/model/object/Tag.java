package daris.client.model.object;

import arc.mf.model.dictionary.DictionaryRef;

public class Tag {

    private String _objectCid;
    private int _id;
    private String _name;
    private String _description;

    public Tag(String objectCid, int id, String name, String description) {
        _objectCid = objectCid;
        _id = id;
        _name = name;
        _description = description;
    }

    public Tag(String objectCid, int id, String name) {
        this(objectCid, id, name, null);
    }

    public DictionaryRef dictionary() {
        return new DictionaryRef(TagDictionary.tagDictionaryNameFor(_objectCid));
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public int id() {
        return _id;
    }

    public String objectCid() {
        return _objectCid;
    }

}
