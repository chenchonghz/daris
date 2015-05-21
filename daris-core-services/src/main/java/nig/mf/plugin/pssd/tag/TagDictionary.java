package nig.mf.plugin.pssd.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nig.mf.plugin.pssd.DataObject;
import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.method.ExMethod;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class TagDictionary {
	public static final String DICTIONARY_NAMESPACE  = "daris-tags";

    public static final String[] TYPES = new String[] { Project.TYPE.toString(), Subject.TYPE.toString(),
            ExMethod.TYPE.toString(), Study.TYPE.toString(), DataSet.TYPE.toString(), DataObject.TYPE.toString() };

    public static class Entry {
        private String _term;
        private String _defn;

        public Entry(String term, String definition) {
            _term = term;
            _defn = definition;
        }

        public String term() {
            return _term;
        }

        public String definition() {
            return _defn;
        }
    }

    public static enum IfExists {
        ignore, replace, error;

        public static IfExists parse(String s, IfExists def) {
            if (s != null) {
                return valueOf(s);
            }
            return def;
        }
    }

    public static final String NAME_PREFIX = "pssd.tags.";

    private String _name;
    private PSSDObject.Type _type;

    TagDictionary(String name, PSSDObject.Type type) {
        _name = name;
        _type = type;
    }

    public String name() {
        return _name;
    }

    public PSSDObject.Type type() {
        return _type;
    }

    public boolean exists() throws Throwable {
        return PluginThread.serviceExecutor()
                .execute("dictionary.exists", "<args><name>" + name() + "</name></args>", null, null)
                .booleanValue("exists");
    }

    protected void create(IfExists ifExists) throws Throwable {

        /*
         * check if the dictionary exists
         */
        if (exists()) {
            if (ifExists == IfExists.error) {
                throw new Exception("Tag dictionary " + name() + " already exists.");
            } else if (ifExists == IfExists.replace) {
                destroy();
            } else {
                return;
            }
        }
        ServiceExecutor executor = PluginThread.serviceExecutor();

        /*
         * create the dictionary
         */
        executor.execute("dictionary.create", "<args><name>" + name() + "</name></args>", null, null);
    }

    protected void destroy() throws Exception, Throwable {

        if (!exists()) {
            return;
        }
        PluginThread.serviceExecutor().execute("dictionary.destroy", "<args><name>" + name() + "</name></args>", null,
                null);
    }

    protected void addEntry(String name, String description, IfExists ifExists) throws Throwable {

        /*
         * if the dictionary does not exist, try to create it.
         */
        if (!exists()) {
            create(IfExists.ignore);
        }

        /*
         * check if the entry exists
         */
        if (containsEntry(name)) {
            if (ifExists == IfExists.error) {
                throw new Exception("Tag " + name + " already exists.");
            } else if (ifExists == IfExists.replace) {
                removeEntry(name);
            } else {
                return;
            }
        }

        /*
         * add the entry
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("dictionary", name());
        dm.add("term", name);
        if (description != null) {
            dm.add("definition", description);
        }
        PluginThread.serviceExecutor().execute("dictionary.entry.add", dm.root(), null, null);
    }

    protected void removeEntry(String name) throws Throwable {

        if (!exists()) {
            return;
        }
        if (!containsEntry(name)) {
            return;
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("dictionary", name());
        dm.add("term", name);
        PluginThread.serviceExecutor().execute("dictionary.entry.remove", dm.root(), null, null);
    }

    public boolean containsEntry(String name) throws Throwable {
        if (!exists()) {
            return false;
        }
        return PluginThread
                .serviceExecutor()
                .execute("dictionary.entry.exists",
                        "<args><dictionary>" + name() + "</dictionary><term>" + name + "</term></args>", null, null)
                .booleanValue("exists");
    }

    public List<Entry> entries() throws Throwable {
        if (!exists()) {
            return null;
        }
        List<XmlDoc.Element> ees = PluginThread
                .serviceExecutor()
                .execute("dictionary.entries.describe",
                        "<args><dictionary>" + name() + "</dictionary><size>infinity</size></args>", null, null)
                .elements("entry");
        if (ees != null && !ees.isEmpty()) {
            List<Entry> es = new ArrayList<Entry>(ees.size());
            for (XmlDoc.Element ee : ees) {
                es.add(new Entry(ee.value("term"), ee.value("definition")));
            }
            return es;
        }
        return null;
    }

    public void listEntries(XmlWriter w) throws Throwable {
        Collection<String> terms = PluginThread
                .serviceExecutor()
                .execute("dictionary.entries.list",
                        "<args><dictionary>" + name() + "</dictionary><size>infinity</size></args>", null, null)
                .values("term");
        if (terms != null) {
            for (String term : terms) {
                w.add("tag", term);
            }
        }
    }

    public void describeEntries(XmlWriter w) throws Throwable {
        List<XmlDoc.Element> ees = PluginThread
                .serviceExecutor()
                .execute("dictionary.entries.describe",
                        "<args><dictionary>" + name() + "</dictionary><size>infinity</size></args>", null, null)
                .elements("entry");
        if (ees == null || ees.isEmpty()) {
            return;
        }
        for (XmlDoc.Element ee : ees) {
            w.push("tag");
            w.add("name", ee.value("term"));
            String description = ee.value("definition");
            if (description != null) {
                w.add("description", description);
            }
            w.pop();
        }
    }

}
