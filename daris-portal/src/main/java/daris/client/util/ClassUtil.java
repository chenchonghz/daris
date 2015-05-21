package daris.client.util;

public class ClassUtil {

    /**
     * Since Class.getSimpleName() method is not supported by GWT JRE. This method is to retrieve the simple name of the
     * class, which the last token of the full class name separated by dot.
     * 
     * @param The
     *            class.
     * @return
     */
    public static String simpleClassNameOf(Class<?> c) {
        String cname = c.getName();
        return cname.substring(cname.lastIndexOf('.') + 1);
    }

    public static String simpleClassNameOf(Object o) {
        return simpleClassNameOf(o.getClass());
    }

    public static void main(String[] args) {
        System.out.println(simpleClassNameOf(String.class));
    }

}
