package daris.plugin.asset.path;

import java.util.regex.Pattern;

public class FileName {

    public static final String UNSAFE_CHARS = "\\\\\\/:\\*\\?\\\"\\|\\{\\}<>~#%&"; // \/:*?"|{}<>~#%&

    public static final String SAFE_CHARS = "^" + UNSAFE_CHARS;

    public static final String PATTERN = "^[" + SAFE_CHARS + "]+$";

    public static boolean isSafeFileName(String name) {
        if (name != null) {
            return Pattern.matches(FileName.PATTERN, name);
        }
        return false;
    }

    public static String replaceUnsafeCharacters(String fileName,
            String replacement) {
        return fileName.replaceAll("[" + UNSAFE_CHARS + "]+", replacement);
    }

    public static void main(String[] args) throws Throwable {

        String[] fns = new String[] { "\\", "/", ":", "*", "?", "\"", "|", "{",
                "}", "<", ">", "~", "#", "%", "&", "a_b-1.txt" };
        for (String fn : fns) {
            System.out.println(String.format("%16s\t%s", fn,
                    isSafeFileName(fn) ? "Y" : "N"));
        }
        System.out.println(
                replaceUnsafeCharacters("sT2-TSE-T_SENSE_sT2/TSE/T", "_"));

    }

}
