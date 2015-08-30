package nig.mf.plugin.pssd.util;

import java.util.Collection;

import nig.mf.plugin.pssd.Application;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Class to manipulate generic local (not federated) registry assets. These
 * registry assets can store lists of information such as project role members,
 * or DICOM servers that have been registered. They are like dictionaries, but
 * allow a richer XML structure.
 * 
 * For each registry type, you need to create a document type matching the
 * element type. These document types should be limited to a single element.
 * 
 * @author nebk
 *
 */
public class AssetRegistry {

    public static final String ASSET_NAMESPACE = "registries";

    public enum AccessType {
        PUBLIC, PRIVATE, ALL
    };

    /**
     * Parse the access type string into the enum
     * 
     * @param accessType
     *            must have value 'public' or 'private'
     * @return
     */
    public static AccessType parseAccessType(String accessType)
            throws Throwable {
        AssetRegistry.AccessType type = AssetRegistry.AccessType.PUBLIC;
        if (accessType.equalsIgnoreCase("public")) {
            type = AccessType.PUBLIC;
        } else if (accessType.equalsIgnoreCase("private")) {
            type = AccessType.PRIVATE;
        } else if (accessType.equalsIgnoreCase("all")) {
            type = AccessType.ALL;
        } else {
            throw new Exception("Access type must be 'public' or 'private'");
        }
        return type;
    }

    /**
     * Find the desired registry asset (singleton).
     * 
     * @param executor
     * @param registryAssetType
     *            For public objects, this string is used for both the asset
     *            type and asset name. It's up to the caller to define and
     *            manage the name. Must be unique. For example,
     *            "pssd-role-member-registry", 'pssd-dicom-server-registry" For
     *            private objects, it's just the asset type (they don't get a
     *            name as there are more than one)
     * @param accessType
     *            specifies whether the registry is the 'public' or 'private'
     *            singleton
     * @return - Asset ID of registry object. Will be null if does not exist
     * @throws Throwable
     */
    public static String findRegistry(ServiceExecutor executor,
            String registryAssetType, AccessType accessType) throws Throwable {

        // We only query on the asset name as if all roles have been removed, no
        // Document will
        // be attached to the singleton asset.
        XmlDocMaker dm = new XmlDocMaker("args");
        if (accessType == AccessType.PUBLIC) {

            // The asset has this name and there is only one
            // It has the type as well (but originally did not so until
            // I write a migration process we keep looking by name only)
            // The private objects don't have a name so they remain separated.
            String query = "name='" + registryAssetType + "'";
            dm.add("where", query);
            dm.add("pdist", 0); // Force to local query
        } else {

            // The asset has this type and there is only one for this user
            // The asset will have an ACL on it so only the user can find their
            // assets
            String query = "(type='" + registryAssetType
                    + "') and (created by me) and (asset hasno name)";
            dm.add("where", query);
            dm.add("pdist", 0); // Force to local query
        }
        XmlDoc.Element r = executor.execute("asset.query", dm.root());

        // See if exists
        Collection<XmlDoc.Element> ids = r.elements("id");
        String id = null;
        if (ids == null || ids.size() == 0) {
            // Does not exist
        } else if (ids.size() > 1) {
            // Trouble
            if (accessType == AccessType.PUBLIC) {
                throw new Exception(
                        "There are multiple public registry objects of name "
                                + registryAssetType
                                + ". Contact the administrator");
            } else {
                throw new Exception(
                        "There are multiple private registry objects of type "
                                + registryAssetType
                                + " for the calling user. Contact the administrator");
            }
        } else {
            id = r.value("id");
        }
        return id;

    }

    /**
     * Find the specified (by name) registry asset (singleton); create if does
     * not exist
     * 
     * @param executor
     * @param registryAssetType
     *            For public objects, this string is used for both the asset
     *            type and asset name. It's up to the caller to define and
     *            manage the name. Must be unique. For example,
     *            "pssd-role-member-registry", 'pssd-dicom-server-registry" For
     *            private objects, it's just the asset type (they don't get a
     *            name as there are more than one)
     * @param accessType
     *            specifies whether the registry is the 'public' or 'private'
     *            singleton
     * @return - Asset ID of registry object. Will be null if not created/exists
     * @throws Throwable
     */
    public static String findAndCreateRegistry(ServiceExecutor executor,
            String registryAssetType, AccessType accessType) throws Throwable {

        // We can only return one id at a time...
        if (accessType == AccessType.ALL) {
            throw new Exception(
                    "AccessType 'all' is not supported in function AssetRegistry.findAndCreateRegistry");
        }

        // CHeck user holds the appropriate role to create a public registry
        // object
        if (accessType == AccessType.PUBLIC)
            checkHasAdminRole(executor);

        // Find if exists
        String id = findRegistry(executor, registryAssetType, accessType);

        // Create if needed (auto-create namespace also)
        if (id == null) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm = new XmlDocMaker("args");
            dm.add("namespace", new String[] { "create", "true" },
                    Application.defaultNamespace(executor) + "/"
                            + ASSET_NAMESPACE);

            if (accessType == AccessType.PRIVATE) {

                dm.add("type", registryAssetType);

                // Add an ACL so only this user can see it and so there will
                // only be one per user
                addUserAcl(executor, dm);
            } else if (accessType == AccessType.PUBLIC) {
                dm.add("name", registryAssetType); // This makes it unique
                dm.add("type", registryAssetType);

                // Add read-write ACLs for administration
                addACL(dm, Role.objectAdminRoleName(), true);
                addACL(dm, Role.powerModelUserRoleName(), true);

                // Add read ACL so all model users can see it
                addACL(dm, Role.modelUserRoleName(), false);
            }

            // Create
            XmlDoc.Element r2 = executor.execute("asset.create", dm.root());
            id = r2.value("id");
        }

        // Return id
        return id;
    }

    /**
     * Find and destroy the specified (by name) asset registry asset.
     * 
     * @param executor
     * @param registryAssetType
     *            For public objects, this string is used for both the asset
     *            type and asset name. It's up to the caller to define and
     *            manage the name. Must be unique. For example,
     *            "pssd-role-member-registry", 'pssd-dicom-server-registry" For
     *            private objects, it's just the asset type (they don't get a
     *            name as there are more than one)
     * @param accessType
     *            specifies whether the registry is the 'public' or 'private'
     *            singleton
     * @return - Asset ID of registry object. Will be null if not created/exists
     * @throws Throwable
     */
    public static String destroyRegistry(ServiceExecutor executor,
            String registryAssetName, AccessType accessType) throws Throwable {

        // Find if exists
        String id = findRegistry(executor, registryAssetName, accessType);
        if (id == null)
            return null;

        // Destroy
        XmlDocMaker dm = new XmlDocMaker("args");
        dm = new XmlDocMaker("args");
        dm.add("id", id);
        executor.execute("asset.destroy", dm.root());
        return id;
    }

    /**
     * Add the new item to the specified registry.
     * 
     * @param id
     *            - The asset ID of the registry asset
     * @param item
     *            - The XML item to add- must match the single element specified
     *            in the given registry document type
     * @param documentType
     *            The Document Type of the specific registry elements you are
     *            working with (e.g. pssd-dicom-server-registry)
     * @throws Throwable
     */
    public static void addItem(ServiceExecutor executor, String id,
            XmlDoc.Element item, String documentType) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.push("meta", new String[] { "action", "add" });
        dm.push(documentType);
        dm.add(item);
        dm.pop();
        dm.pop();
        executor.execute("asset.set", dm.root());
    }

    /**
     * Remove the specified item from the registry.
     * 
     * @param id
     *            the Registry asset id
     * @param docId
     *            the id of the document we want to remove
     * @param doc
     *            - The element to remove
     * @param documentType
     *            The Document Type of the specific registry elements you are
     *            working with (e.g. daris:pssd-dicom-server-registry
     * @throws Throwable
     */
    public static void removeItem(ServiceExecutor executor, String id,
            String docId, XmlDoc.Element item, String documentType)
            throws Throwable {

        XmlDocMaker dm2 = new XmlDocMaker("args");
        dm2.add("id", id);
        dm2.push("meta", new String[] { "action", "remove" });
        dm2.push(documentType, new String[] { "id", docId });
        dm2.pop();
        dm2.pop();
        executor.execute("asset.set", dm2.root());
    }

    /**
     * List the contents of the registry in the writer
     * 
     * @param executor
     * @param id
     *            - Asset ID of registry asset
     * @param the
     *            Xpath to iterate over. E.g.
     *            asset/meta/daris:pssd-role-member-registry/role
     * @param w
     * @throws Throwable
     */
    public static void list(ServiceExecutor executor, String id, String xpath,
            XmlWriter w) throws Throwable {

        // Get the registry asset
        XmlDoc.Element r = getRegistryAsset(executor, id);

        // Iterate over given Xpath
        Collection<XmlDoc.Element> elements = r.elements(xpath);
        if (elements != null) {
            for (XmlDoc.Element el : elements) {
                w.add(el);
            }
        }
    }

    /**
     * Find out if the specified item is a member of the registry
     * 
     * @param executor
     * @param registryAssetType
     *            For public objects, this string is used for both the asset
     *            type and asset name. It's up to the caller to define and
     *            manage the name. Must be unique. For example,
     *            "pssd-role-member-registry", 'pssd-dicom-server-registry" For
     *            private objects, it's just the asset type (they don't get a
     *            name as there are more than one)
     * @param documentType
     *            The Document Type of the specific registry elements you are
     *            working with (e.g. daris:pssd-dicom-server-registry)
     * @param item
     *            The item to test
     * @param accessType
     *            specifies whether the registry is the 'public' or 'private'
     *            singleton
     * @param checkTopName
     *            If true, compares the names of the elements at the top level,
     *            if false, does not.
     * @return true or false
     * @throws Throwable
     */
    public static boolean checkExists(ServiceExecutor executor,
            String registryAssetName, String documentType, XmlDoc.Element item,
            AccessType accessType, Boolean checkTopName) throws Throwable {

        // Find if exists
        String id = findRegistry(executor, registryAssetName, accessType);
        if (id == null)
            return false;

        // Get the registry asset
        XmlDoc.Element r = getRegistryAsset(executor, id);

        // Find collection and check
        Collection<XmlDoc.Element> docs = r.elements("asset/meta/"
                + documentType);
        return (hasItem(executor, docs, item, checkTopName) != null);
    }

    /**
     * Find out if the specified item is a member of the registry It critically
     * depends on the elements of the item being in the same order as in the
     * registry. Services like DvcDICCOMAEAdd make sure of this.
     * 
     * @param executor
     * @param id
     *            Registry asset id
     * @param documentType
     *            The Document Type of the specific registry elements you are
     *            working with (e.g. pssd-dicom-server-registry)
     * @param item
     *            The item to test
     * @param checkTopName
     *            If true, compares the names of the elements at the top level,
     *            if false, does not.
     * @return Returns the document id if a match is found
     * @throws Throwable
     */
    public static String checkExists(ServiceExecutor executor, String id,
            String documentType, XmlDoc.Element item, Boolean checkTopName)
            throws Throwable {

        XmlDoc.Element r = getRegistryAsset(executor, id);
        if (r == null)
            return null;

        // Find collection and check
        Collection<XmlDoc.Element> docs = r.elements("asset/meta/"
                + documentType);
        return hasItem(executor, docs, item, checkTopName);
    }

    private static void addACL(XmlDocMaker dm, String role, Boolean readWrite)
            throws Throwable {
        dm.push("acl");
        dm.add("actor", new String[] { "type", "role" }, role);
        if (readWrite) {
            dm.add("access", "read-write");
        } else {
            dm.add("access", "read");
        }
        dm.pop();
    }

    private static void addUserAcl(ServiceExecutor executor, XmlDocMaker dm)
            throws Throwable {

        XmlDoc.Element r = executor.execute("user.self.describe");
        XmlDoc.Element u = r.element("user");
        String domain = u.value("@domain");
        String user = u.value("@user");
        //
        dm.push("acl");
        dm.add("actor", new String[] { "type", "user" }, domain + ":" + user);
        dm.add("access", "read-write");
        dm.pop();
    }

    private static void checkHasAdminRole(ServiceExecutor executor)
            throws Throwable {
        // For public registry objects, the caller needs admin rights to create
        ServerRoute route = null;
        if (!PSSDUtil.hasRole(route, executor, Role.objectAdminRoleName())
                && !PSSDUtil.hasRole(route, executor,
                        Role.powerModelUserRoleName())) {
            throw new Exception(
                    "Caller must have the 'daris:pssd.object.admin' or 'daris:pssd.model.power.user' role to create public registry objects");
        }
    }

    /**
     * This function finds out if an item already exists in the registry. It
     * critically depends on the elements of the item being in the same order as
     * in the registry. Services like DvcDICCOMAEAdd make sure of this.
     * 
     * @param executor
     * @param docs
     * @param item
     * @param checkTopName
     * @return
     * @throws Throwable
     */
    private static String hasItem(ServiceExecutor executor,
            Collection<XmlDoc.Element> docs, XmlDoc.Element item,
            Boolean checkTopName) throws Throwable {

        if (docs == null)
            return null;

        // Iterate over documents
        for (XmlDoc.Element doc : docs) {

            // Find all per-existing elements. Should be one per document...
            Collection<XmlDoc.Element> elements = doc.elements();
            if (elements != null) {
                for (XmlDoc.Element el : elements) {

                    // If we don't want to compare names, just make them the
                    // same :-)
                    if (!checkTopName) {
                        el.setName(item.name());
                    }

                    // Note that the elements must be in the same order for this
                    // String comparison to work.
                    // This is why SvcDICOMAE{Add,Remove} re-order the element
                    // to the same order
                    // rather than what the user may have specified.
                    // if (el.equalsIncludeDescendants(item)) {
                    String s1 = el.toString();
                    String s2 = item.toString();
                    /*
                     * System.out.println("Comparing strings:");
                     * System.out.println("------->" + s1 + "(length=" +
                     * s1.length() + ")"); System.out.println("------->" + s2 +
                     * "(length=" + s2.length() + ")");
                     * System.out.println("   Result string compare   = " +
                     * s1.equals(s2));
                     */
                    if (s1.equals(s2)) {
                        return doc.value("@id");
                    }
                }
            }
        }
        return null;
    }

    private static XmlDoc.Element getRegistryAsset(ServiceExecutor executor,
            String id) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("pdist", 0); // Force local
        return executor.execute("asset.get", dm.root());
    }
}
