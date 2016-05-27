package nig.dicom.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

//import com.pixelmed.dicom.DicomFileUtilities;

public class DicomModify {

    public static final String DEFAULT_AE_TITLE = "DCMTOOLS";

    public static final String APP_NAME = "DCMODIFY";

    public static final String ELEMENT_DELIMITER = ";";

    public static void main(String[] args) throws Throwable {

        boolean backup = false;

        if (args == null || args.length < 2) {
            printUsage();
            System.exit(1);
        }

        Map<AttributeTag, String> attrs = null;
        List<File> dcmFiles = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--backup") || args[i].equals("-b")) {
                backup = true;
            } else {
                if (attrs == null) {
                    attrs = parseAttributes(args[i]);
                } else {
                    File f = new File(args[i]);
                    if (!f.exists()) {
                        System.err.println("Error: " + f.getAbsolutePath()
                                + " does not exist.");
                        continue;
                    }
                    if (!DicomFileCheck.isDicomFile(f)) {
                        System.err.println("Error: " + args[i]
                                + " is not a valid DICOM file.");
                        continue;
                    }
                    dcmFiles.add(f);
                }
            }
        }

        if (attrs == null || attrs.isEmpty()) {
            System.err
                    .println("Error: missing or invalid element(s) argument.");
            printUsage();
            System.exit(1);
        }

        if (dcmFiles.isEmpty()) {
            System.err.println("Error: no dicom file/directory is specified.");
            printUsage();
            System.exit(1);
        }

        for (File dcmFile : dcmFiles) {
            editFile(dcmFile, attrs, backup);
        }

    }

    public static void editFile(File f, Map<AttributeTag, String> attrs,
            boolean backup) throws Throwable {

        if (backup) {
            // Make a backup.
            File bf = new File(f.getAbsolutePath() + ".bak");
            Files.copy(Paths.get(f.getAbsolutePath()),
                    Paths.get(bf.getAbsolutePath()));
        }

        // System.out.print("Modifying " + f.getPath() + "... ");
        AttributeList list = new AttributeList();
        list.read(f);

        Attribute mediaStorageSOPClassUIDAttr = list
                .get(TagFromName.MediaStorageSOPClassUID);
        String mediaStorageSOPClassUID = null;
        if (mediaStorageSOPClassUIDAttr != null) {
            mediaStorageSOPClassUID = mediaStorageSOPClassUIDAttr
                    .getSingleStringValueOrNull();
        }
        Attribute mediaStorageSOPInstanceUIDAttr = list
                .get(TagFromName.MediaStorageSOPInstanceUID);
        String mediaStorageSOPInstanceUID = null;
        if (mediaStorageSOPInstanceUIDAttr != null) {
            mediaStorageSOPInstanceUID = mediaStorageSOPInstanceUIDAttr
                    .getSingleStringValueOrNull();
        }
        // String implementationClassUID =
        // list.get(TagFromName.ImplementationClassUID).getSingleStringValueOrNull();
        // String implementationVersionName =
        // list.get(TagFromName.ImplementationVersionName).getSingleStringValueOrNull();

        /*
         * Cleanup
         */
        list.removeGroupLengthAttributes();
        list.removeMetaInformationHeaderAttributes();
        list.remove(TagFromName.DataSetTrailingPadding);
        list.correctDecompressedImagePixelModule();
        list.insertLossyImageCompressionHistoryIfDecompressed();

        if (mediaStorageSOPClassUID != null
                && mediaStorageSOPInstanceUID != null) {
            FileMetaInformation.addFileMetaInformation(list,
                    mediaStorageSOPClassUID, mediaStorageSOPInstanceUID,
                    TransferSyntax.ExplicitVRLittleEndian, DEFAULT_AE_TITLE);
        } else {
            FileMetaInformation.addFileMetaInformation(list,
                    TransferSyntax.ExplicitVRLittleEndian, DEFAULT_AE_TITLE);
        }

        // Put the new tag in place
        if (attrs != null) {
            for (AttributeTag tag : attrs.keySet()) {
                Attribute attr = list.get(tag);
                String value = attrs.get(tag);
                if (attr != null) {
                    attr.setValue(value);
                } else {
                    list.putNewAttribute(tag).addValue(value);
                }
            }
        }

        File tf = File.createTempFile(f.getName(), ".tmp", f.getParentFile());
        list.write(new FileOutputStream(tf),
                TransferSyntax.ExplicitVRLittleEndian, true, true);
        Files.move(Paths.get(tf.getAbsolutePath()),
                Paths.get(f.getAbsolutePath()),
                StandardCopyOption.REPLACE_EXISTING);
        // System.out.println("done");
    }

    public static void printUsage() {

        System.out.println("Usage:");
        System.out.println(
                "\t DCMODIFY  [options]  <tag=\"value\">  <dicom-files>\n");
        System.out.println("\t Options:");
        System.out.println(
                "\t          -b|--backup                       create a backup file with extension .bak\n");
        // System.out
        // .println("\t -o|--output <output-directory> save the output into the
        // specified directory.\n");
        System.out.println("Examples:");
        System.out
                .println("\t DCMODIFY    \"(0010,0010)=Smith^John\"  test.dcm");
        System.out.println("\t DCMODIFY    \"(0010,0020)=12345\"  ./*.dcm");
        System.out.println(
                "\t DCMODIFY    \"(0010,0010)=Smith^Tom;(0010,0020)=12345\"  ./*.dcm");
        System.out
                .println("\t DCMODIFY -b \"(0010,0010)=Smith^John\"  test.dcm");
        System.out.println("\t DCMODIFY -b \"(0010,0010)=Smith^Tom\"  ./*.dcm");
        System.out.println(
                "\t DCMODIFY --backup  \"(0010,0020)=12345\"  ./*.dcm");

    }

    private static Map<AttributeTag, String> parseAttributes(String arg) {
        arg = trimQuotes(arg).trim();

        String[] as = arg.split(ELEMENT_DELIMITER);
        Map<AttributeTag, String> attrs = new LinkedHashMap<AttributeTag, String>();
        for (String a : as) {
            parseAttribute(attrs, a);
        }
        return attrs;
    }

    private static void parseAttribute(Map<AttributeTag, String> attrs,
            String attr) {

        attr = trimQuotes(attr).trim();
        String[] parts = attr.split("=");

        String tag = parts[0].trim();
        String aValue = parts[1].trim();

        tag = stripParenthesis(tag);
        String[] tagParts = tag.split(",");
        String hGroup = tagParts[0].trim();
        int group = Integer.parseInt(hGroup, 16);
        String hElement = tagParts[1].trim();
        int element = Integer.parseInt(hElement, 16);
        AttributeTag aTag = new AttributeTag(group, element);

        attrs.put(aTag, aValue);
    }

    private static String trimQuotes(String s) {
        while ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String stripParenthesis(String s) {
        while (s.startsWith("(") && s.endsWith(")")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

}
