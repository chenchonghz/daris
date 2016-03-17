package nig.mf.plugin.pssd.dicom;

public class DicomStringUtils {
    /**
     * Remove the unicode char 0x00 which is illegal character in XML.
     * 
     * @param s
     * @return
     */
    public static String removeZeros(String s) {
        if (s == null) {
            return s;
        }
        return s.replaceAll("[\\000]+", "");
    }

}
