package daris.dicom.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap.SimpleEntry;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

import daris.util.ChecksumUtils;

public class DicomChecksumUtils {

    public static SimpleEntry<String, Boolean> getPixelDataChecksum(InputStream in, String checksumType)
            throws Throwable {
        AttributeList attributeList = new AttributeList();
        DicomInputStream dis = new DicomInputStream(in);
        try {
            attributeList.read(dis);
            return getPixelDataChecksum(attributeList, checksumType);
        } finally {
            dis.close();
        }
    }

    public static String getPixelDataChecksum(InputStream in, boolean bigEndian, String checksumType) throws Throwable {
        AttributeList attributeList = new AttributeList();
        DicomInputStream dis = new DicomInputStream(in);
        try {
            attributeList.read(dis);
            return getPixelDataChecksum(attributeList, bigEndian, checksumType);
        } finally {
            dis.close();
        }
    }

    public static SimpleEntry<String, Boolean> getPixelDataChecksum(File dicomFile, String checksumType)
            throws Throwable {
        AttributeList attributeList = new AttributeList();
        attributeList.read(dicomFile);
        return getPixelDataChecksum(attributeList, checksumType);
    }

    public static String getPixelDataChecksum(File dicomFile, boolean bigEndian, String checksumType) throws Throwable {
        AttributeList attributeList = new AttributeList();
        attributeList.read(dicomFile);
        return getPixelDataChecksum(attributeList, bigEndian, checksumType);
    }

    public static SimpleEntry<String, Boolean> getPixelDataChecksum(AttributeList attributeList, String checksumType)
            throws Throwable {
        boolean bigEndian = TransferSyntax.isBigEndian(attributeList.get(TagFromName.TransferSyntaxUID)
                .getSingleStringValueOrDefault(TransferSyntax.DeflatedExplicitVRLittleEndian));
        String checksum = getPixelDataChecksum(attributeList, bigEndian, checksumType);
        return new SimpleEntry<String, Boolean>(checksum, bigEndian);
    }

    public static String getPixelDataChecksum(AttributeList attributeList, boolean bigEndian, String checksumType)
            throws Throwable {
        Attribute pixelDataAttribute = attributeList.getPixelData();
        if (pixelDataAttribute == null) {
            throw new Exception("No PixelData element found.");
        }
        return getPixelDataChecksum(pixelDataAttribute, bigEndian, checksumType);
    }

    public static String getPixelDataChecksum(Attribute pixelDataAttribute, boolean bigEndian, String checksumType)
            throws Throwable {
        String vr = new String(pixelDataAttribute.getVR());
        byte[] bytes;
        if ("OW".equalsIgnoreCase(vr)) {
            short[] words = pixelDataAttribute.getShortValues();
            ByteBuffer buffer = ByteBuffer.allocate(words.length * 2)
                    .order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            for (short word : words) {
                buffer.putShort(word);
            }
            bytes = buffer.array();
        } else if ("OB".equalsIgnoreCase(vr)) {
            bytes = pixelDataAttribute.getByteValues();
        } else {
            throw new Exception("Invalid VR: " + vr + " for PixelData(7FE0,0010).");
        }
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            return ChecksumUtils.getChecksum(in, checksumType);
        } finally {
            in.close();
        }
    }

}
