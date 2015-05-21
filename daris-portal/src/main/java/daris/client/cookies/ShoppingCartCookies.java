package daris.client.cookies;

public class ShoppingCartCookies {

    private static final int EXPIRE_DAYS = 30;
    private static final String USE_DTI = "mf.sc.use_dti";
    private static final String DTI_DST_DIR = "mf.sc.dti_dst_dir";
    private static final String DTI_DECOMPRESS = "mf.sc.dti_decompress";
    private static final String DTI_OVERWRITE = "mf.sc.dti_overwrite";

    public static void setUseDTI(boolean useDTI) {
        Cookies.set(USE_DTI, Boolean.toString(useDTI), EXPIRE_DAYS);
    }

    public static boolean useDTI() {
        String useDTI = Cookies.get(USE_DTI);
        if (useDTI == null) {
            return false;
        }
        return Boolean.parseBoolean(useDTI);
    }

    public static void removeUseDTI() {
        Cookies.remove(USE_DTI);
    }

    public static void setDTIDstDir(String dstDir) {
        Cookies.set(DTI_DST_DIR, dstDir, EXPIRE_DAYS);
    }

    public static void removeDTIDstDir() {
        Cookies.remove(DTI_DST_DIR);
    }

    public static String dtiDstDir() {
        return Cookies.get(DTI_DST_DIR);
    }

    public static void setDTIDecompress(boolean decompress) {
        Cookies.set(DTI_DECOMPRESS, Boolean.toString(decompress), EXPIRE_DAYS);
    }

    public static boolean dtiDecompress() {
        String decompress = Cookies.get(DTI_DECOMPRESS);
        if (decompress == null) {
            return false;
        }
        return Boolean.parseBoolean(decompress);
    }

    public static void removeDTICompress() {
        Cookies.remove(DTI_DECOMPRESS);
    }

    public static void setDTIOverwrite(boolean overwrite) {
        Cookies.set(DTI_OVERWRITE, Boolean.toString(overwrite), EXPIRE_DAYS);
    }

    public static boolean dtiOverwrite() {
        String overwrite = Cookies.get(DTI_OVERWRITE);
        if (overwrite == null) {
            return false;
        }
        return Boolean.parseBoolean(overwrite);
    }

    public static void removeDTIOverwrite() {
        Cookies.remove(DTI_OVERWRITE);
    }

}
