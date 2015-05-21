package daris.client.model.object;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.model.dictionary.Dictionary;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.messages.CheckDictionaryExistance;
import arc.mf.model.dictionary.messages.CreateDictionary;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.IDUtil;

public class TagDictionary {

    public static final String PREFIX = "daris-tags:pssd.tags.";

    public static String tagDictionaryNameFor(String cid) {
        if (cid == null) {
            return null;
        }
        String projectCid = IDUtil.getProjectId(cid);
        if (projectCid == null) {
            return null;
        }
        String type = IDUtil.typeFromId(cid).toString();
        return "daris-tags:pssd." + type + ".tags." + projectCid;
    }

    public static DictionaryRef tagDictionaryFor(DObjectRef o) {
        return new DictionaryRef(tagDictionaryNameFor(o.id()));
    }

    public static void exists(DictionaryRef dict, ObjectMessageResponse<Boolean> rh) {
        new CheckDictionaryExistance(dict.name()).send(rh);
    }

    public static void createIfNotExists(final DictionaryRef dict, final ObjectMessageResponse<DictionaryRef> rh) {
        exists(dict, new ObjectMessageResponse<Boolean>() {
            @Override
            public void responded(Boolean exists) {
                if (rh == null) {
                    return;
                }
                if (exists) {
                    rh.responded(dict);
                } else {
                    new CreateDictionary(new Dictionary(dict.name(), null, false)).send(rh);
                }
            }
        });
    }

    public static void countEntries(DictionaryRef dict, final ObjectMessageResponse<Integer> rh) {
        exists(dict, new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean r) {
                if (r != null && r == true) {
                    Session.execute("dictionary.entries.list", "<count>true</count>", new ServiceResponseHandler() {

                        @Override
                        public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                            if (rh != null) {
                                rh.responded(xe.intValue("total", 0));
                            }
                        }
                    });
                } else {
                    if (rh != null) {
                        rh.responded(0);
                    }
                }
            }
        });
    }

    public static void isEmpty(DictionaryRef dict, final ObjectMessageResponse<Boolean> rh) {
        countEntries(dict, new ObjectMessageResponse<Integer>() {

            @Override
            public void responded(Integer r) {
                if (rh != null) {
                    rh.responded(r == 0);
                }
            }
        });
    }
}
