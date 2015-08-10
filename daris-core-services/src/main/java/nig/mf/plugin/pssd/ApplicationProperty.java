package nig.mf.plugin.pssd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nig.mf.plugin.pssd.sc.Layout;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDocMaker;

public class ApplicationProperty {

    public static final String APPLICATION_NAME = Application.DARIS;

    public static void set(ServiceExecutor executor, String name, String value)
            throws Throwable {
        // create the property
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("ifexists", "ignore");
        dm.push("property", new String[] { "app", APPLICATION_NAME, "name",
                name });
        executor.execute("application.property.create", dm.root());

        // set the property value
        dm = new XmlDocMaker("args");
        dm.add("property",
                new String[] { "app", APPLICATION_NAME, "name", name }, value);
        executor.execute("application.property.set", dm.root());
    }

    public static String get(ServiceExecutor executor, String name)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", APPLICATION_NAME);
        dm.add("name", name);
        return executor.execute("application.property.describe", dm.root())
                .value("property/value");
    }

    public static void destroy(ServiceExecutor executor, String name)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property",
                new String[] { "app", APPLICATION_NAME, "name", name });
        executor.execute("application.property.destroy", dm.root());
    }

    public static boolean exists(ServiceExecutor executor, String name)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", APPLICATION_NAME }, name);
        return executor.execute("application.property.exists", dm.root())
                .booleanValue("exists");
    }

    public static class TemporaryNamespace {

        public static final String TMP_NAMESPACE_PROPERTY = "namespace.tmp";

        public static String get(ServiceExecutor executor) throws Throwable {
            if (!ApplicationProperty.exists(executor, TMP_NAMESPACE_PROPERTY)) {
                String namespace = Application.defaultNamespace(executor)
                        + "/tmp";
                set(executor, namespace);
                return namespace;
            } else {
                return ApplicationProperty
                        .get(executor, TMP_NAMESPACE_PROPERTY);
            }
        }

        public static void set(ServiceExecutor executor, String namespace)
                throws Throwable {
            ApplicationProperty
                    .set(executor, TMP_NAMESPACE_PROPERTY, namespace);
        }

    }

    public static class ShoppingCartLayoutPattern {

        public static final String SHOPPING_CART_LAYOUT_PATTERN_PROPERTY = "shoppingcart.layout-pattern";

        private static final String FIELD_DELIMITER = "||";
        private static final String FIELD_REGEX = "\\|\\|";
        private static final String ENTRY_DELIMITER = "::";
        private static final String ENTRY_REGEX = "::";

        private static String encode(Layout.Pattern p) {
            StringBuilder sb = new StringBuilder();
            if (p.name != null) {
                sb.append(p.name);
            }
            sb.append(FIELD_DELIMITER);
            if (p.description != null) {
                sb.append(p.description);
            }
            sb.append(FIELD_DELIMITER);
            sb.append(p.pattern);
            return sb.toString();
        }

        private static String encodeAll(List<Layout.Pattern> ps) {
            if (ps == null || ps.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            int n = ps.size();
            for (int i = 0; i < n; i++) {
                sb.append(encode(ps.get(i)));
                if (i < n - 1) {
                    sb.append(ENTRY_DELIMITER);
                }
            }
            return sb.toString();
        }

        private static Layout.Pattern decode(String s) {
            if (s == null) {
                return null;
            }
            String[] fields = s.split(FIELD_REGEX);
            if (fields.length != 3) {
                throw new IllegalArgumentException(
                        "Failed to parse layout pattern entry: " + s);
            }
            return new Layout.Pattern(fields[0], "".equals(fields[1]) ? null
                    : fields[1], fields[2]);
        }

        private static List<Layout.Pattern> decodeAll(String s) {
            if (s == null) {
                return null;
            }
            String[] entries = s.split(ENTRY_REGEX);
            List<Layout.Pattern> lps = new ArrayList<Layout.Pattern>(
                    entries.length);
            for (int i = 0; i < entries.length; i++) {
                lps.add(decode(entries[i]));
            }
            return lps;
        }

        public static void setAll(ServiceExecutor executor,
                List<Layout.Pattern> layoutPatterns) throws Throwable {
            if (layoutPatterns == null || layoutPatterns.isEmpty()) {
                ApplicationProperty.destroy(executor,
                        SHOPPING_CART_LAYOUT_PATTERN_PROPERTY);
                return;
            }
            /*
             * validate
             */
            Set<String> names = new HashSet<String>();
            Set<String> patterns = new HashSet<String>();
            for (Layout.Pattern lp : layoutPatterns) {
                if (names.contains(lp.name)) {
                    throw new IllegalArgumentException(
                            "Duplicated layout pattern name: " + lp.name);
                } else {
                    names.add(lp.name);
                }
                if (patterns.contains(lp.pattern)) {
                    throw new IllegalArgumentException(
                            "Duplicated layout pattern:" + lp.pattern);
                } else {
                    patterns.add(lp.pattern);
                }
            }
            ApplicationProperty.set(executor,
                    SHOPPING_CART_LAYOUT_PATTERN_PROPERTY,
                    encodeAll(layoutPatterns));
        }

        public static List<Layout.Pattern> getAll(ServiceExecutor executor)
                throws Throwable {
            if (!ApplicationProperty.exists(executor,
                    SHOPPING_CART_LAYOUT_PATTERN_PROPERTY)) {
                List<Layout.Pattern> defaultPatterns = Arrays
                        .asList(Layout.Pattern.PSSD_DEFAULT);
                setAll(executor, defaultPatterns);
                return defaultPatterns;
            } else {
                return decodeAll(ApplicationProperty.get(executor,
                        SHOPPING_CART_LAYOUT_PATTERN_PROPERTY));
            }
        }

        public static Layout.Pattern getPattern(ServiceExecutor executor,
                String pattern) throws Throwable {
            List<Layout.Pattern> lps = getAll(executor);
            if (lps != null) {
                for (Layout.Pattern lp : lps) {
                    if (lp.pattern.equals(pattern)) {
                        return lp;
                    }
                }
            }
            return null;
        }

        public static boolean exists(ServiceExecutor executor,
                Layout.Pattern layoutPattern) throws Throwable {
            if (layoutPattern == null || layoutPattern.pattern == null
                    || layoutPattern.name == null) {
                return false;
            }
            List<Layout.Pattern> lps = getAll(executor);
            if (lps == null || lps.isEmpty()) {
                return false;
            }
            for (Layout.Pattern lp : lps) {
                if (lp.equals(layoutPattern)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean exists(ServiceExecutor executor, String pattern)
                throws Throwable {
            if (pattern == null) {
                return false;
            }
            List<Layout.Pattern> lps = getAll(executor);
            if (lps == null || lps.isEmpty()) {
                return false;
            }
            for (Layout.Pattern lp : lps) {
                if (lp.pattern.equals(pattern)) {
                    return true;
                }
            }
            return false;
        }

        public static void add(ServiceExecutor executor,
                Layout.Pattern layoutPattern) throws Throwable {
            if (layoutPattern == null || layoutPattern.pattern == null
                    || layoutPattern.name == null) {
                return;
            }
            List<Layout.Pattern> lps = getAll(executor);
            if (lps == null || lps.isEmpty()) {
                setAll(executor, Arrays.asList(layoutPattern));
                return;
            }
            for (Layout.Pattern lp : lps) {
                if (lp.name.equals(layoutPattern.name)) {
                    throw new IllegalArgumentException("Layout pattern named: "
                            + lp.name + " already exists.");
                }
                if (lp.pattern.equals(layoutPattern.pattern)) {
                    throw new IllegalArgumentException("Layout pattern: \""
                            + lp.pattern + "\" already exists.");
                }
            }
            List<Layout.Pattern> nlps = new ArrayList<Layout.Pattern>(lps);
            nlps.add(layoutPattern);
            setAll(executor, nlps);
        }

        public static void remove(ServiceExecutor executor, String name)
                throws Throwable {
            if (name == null) {
                return;
            }
            List<Layout.Pattern> lps = getAll(executor);
            if (lps == null || lps.isEmpty()) {
                return;
            }
            List<Layout.Pattern> nlps = new ArrayList<Layout.Pattern>(lps);
            for (Iterator<Layout.Pattern> it = nlps.iterator(); it.hasNext();) {
                Layout.Pattern lp = it.next();
                if (name.equals(lp.name)) {
                    it.remove();
                }
            }
            setAll(executor, nlps);
        }

        public static void main(String[] args) throws Throwable {
            String regex = "\\|\\|";
            System.out
                    .println("pssd-default||The default shopping cart layout pattern.||cid(-7,-5)/cid(-7,-4)/cid(-7,-3)/cid(-7,-2)/replace(if-null(variable(tx-to-type), xpath(asset/type)),'/','_')/cid(-1)if-null(xpath(daris:pssd-object/name),'','_')xpath(daris:pssd-object/name)"
                            .split(regex).length);
        }

    }

}
