package nig.mf.plugin.pssd.sc;


public class Layout {

    public static final Type DEFAULT_TYPE = Type.custom;

    public static enum Type {

        custom, flat, preserved;

        public static Type fromString(String type) {
            Type[] ts = values();
            for (int i = 0; i < ts.length; i++) {
                if (ts[i].toString().equalsIgnoreCase(type)) {
                    return ts[i];
                }
            }
            return null;
        }

    }

    public static class Pattern {

        public static final Pattern PSSD_DEFAULT = new Pattern(
                "pssd-default",
                "The default shopping cart layout pattern.",
                "cid(-7,-5)/cid(-7,-4)/cid(-7,-3)/cid(-7,-2)/replace(if-null(variable(tx-to-type), xpath(asset/type)),'/','_')/cid(-1)if-null(xpath(daris:pssd-object/name),'','_')replace(replace(xpath(daris:pssd-object/name),'/','-'),' ','_')");

        public final String name;
        public final String description;
        public final String pattern;

        public Pattern(String name, String description, String pattern) {
            this.name = name;
            this.description = description;
            this.pattern = pattern;
        }

        public boolean equals(Object o) {
            if (o != null && o instanceof Layout.Pattern) {
                Layout.Pattern po = (Layout.Pattern) o;
                if (this.pattern == null) {
                    return po.pattern == null;
                } else {
                    return this.pattern.equals(po.pattern);
                }
            }
            return false;
        }

    }

}
