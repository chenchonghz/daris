package nig.mf.plugin.pssd.tag;

import nig.mf.plugin.pssd.PSSDObject;

public class GlobalTagDictionary extends TagDictionary {

    public GlobalTagDictionary(PSSDObject.Type type) {
        super(TagDictionary.DICTIONARY_NAMESPACE + ":pssd." + type + ".tags", type);

    }

    public static GlobalTagDictionary dictionaryFor(PSSDObject.Type type) {
        return new GlobalTagDictionary(type);
    }

    public void create(IfExists ifExists) throws Throwable {
        /*
         * check if the user has sufficient privilege
         */
        // if (!Self.isSystemAdministrator()) {
        // throw new Exception("You do not sufficient priviledge to create a dictionary.");
        // }
        super.create(ifExists);
    }

    public void destroy() throws Exception, Throwable {

        /*
         * check if the user has sufficient privilege
         */
        // if (!Self.isSystemAdministrator()) {
        // throw new Exception("You do not sufficient priviledge to desctory dictionary " + name() +
        // ".");
        // }
        super.destroy();
    }

    public void addEntry(String name, String description, IfExists ifExists) throws Throwable {

        /*
         * check if the user has sufficient privilege
         */
        // if (!Self.isSystemAdministrator()) {
        // throw new Exception("You do not have sufficient priviledge to modify dictionary " +
        // name() + ".");
        // }

        super.addEntry(name, description, ifExists);
    }

    public void removeEntry(String name) throws Throwable {

        /*
         * check if the user has sufficient privilege
         */
        // if (!Self.isSystemAdministrator()) {
        // throw new Exception("You do not have sufficient priviledge to modify dictionary "
        // + name() + ".");
        // }
        super.removeEntry(name);
    }

}
