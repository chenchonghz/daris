package nig.mf.plugin.analyzer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ucar.netcdf.BufferedImageInputStream;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.content.PluginContentAnalyzer.StreamInput;
import arc.mf.plugin.image.ImageRegistry;
import arc.mime.NamedMimeType;
import arc.streams.SizedInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
//import edu.ucla.loni.analyze.plugin.AnalyzeImageReaderSpi;
import edu.ucla.loni.analyze.plugin.AnalyzeInputStream;
import edu.ucla.loni.jdicom.plugin.DicomImageReaderSpi;
import edu.ucla.loni.minc.plugin.MincImageReaderSpi;
import edu.ucla.loni.nifti.plugin.NiftiImageReaderSpi;

public class ImageIOUtil {

    public static final String LONI_NIFTI1_READER = "edu.ucla.loni.nifti.plugin.NiftiImageReader";
    public static final String LONI_MINC_READER = "edu.ucla.loni.minc.plugin.MincImageReader";
    public static final String LONI_DICOM_READER = "edu.ucla.loni.jdicom.plugin.DicomImageReader";
    public static final String LONI_ANALYZE_READER = "edu.ucla.loni.analyze.plugin.AnalyzeImageReader";

    public static final List<String> LOGICAL_TYPES_NIFTI1 = Collections.unmodifiableList(Arrays.asList("nifti/series",
            "image/nii", "image/hdr"));
    public static final List<String> LOGICAL_TYPES_MINC = Collections.unmodifiableList(Arrays.asList("minc/series",
            "image/mnc"));
    public static final List<String> LOGICAL_TYPES_DICOM = Collections.unmodifiableList(Arrays.asList("dicom/series",
            "image/dcm"));
    public static final List<String> LOGICAL_TYPES_ANALYZE = Collections.unmodifiableList(Arrays.asList(
            "analyze/series", "analyze/series/nl", "analyze/series/rl", "image/hdr"));

    private static Map<String, String> LONI_READERS = new HashMap<String, String>();
    static {
        for (String type : LOGICAL_TYPES_NIFTI1) {
            LONI_READERS.put(type, LONI_NIFTI1_READER);
        }
        for (String type : LOGICAL_TYPES_MINC) {
            LONI_READERS.put(type, LONI_MINC_READER);
        }
        for (String type : LOGICAL_TYPES_DICOM) {
            LONI_READERS.put(type, LONI_DICOM_READER);
        }
        for (String type : LOGICAL_TYPES_ANALYZE) {
            LONI_READERS.put(type, LONI_ANALYZE_READER);
        }
    }

    public static final String DOC_TYPE_NIFTI1 = "daris:nifti-1";

    public static final String MIME_TYPE_CONTENT_UNKNOWN = "content/unknown";
    public static final String MIME_TYPE_TAR = "application/x-tar";
    public static final String MIME_TYPE_DCM = "image/dcm";
    public static final String MIME_TYPE_MNC = "image/mnc";
    public static final String MIME_TYPE_NII = "image/nii";

    public static XmlDoc.Element getMetadata(ImageReader reader, String lctype) throws Throwable {
        IIOMetadata iioMetadata = reader.getStreamMetadata();
        if (iioMetadata != null) {
            String formatName = iioMetadata.getNativeMetadataFormatName();
            Node rootNode = iioMetadata.getAsTree(formatName);
            if (LOGICAL_TYPES_NIFTI1.contains(lctype)) {
                return extractNifti1Metadata(rootNode);
            } else if (LOGICAL_TYPES_MINC.contains(lctype)) {
                return extractMincMetadata(rootNode);
            } else if (LOGICAL_TYPES_ANALYZE.contains(lctype)) {
                return extractAnalyzeMetadata(rootNode);
            } else if (LOGICAL_TYPES_DICOM.contains(lctype)) {
                return extractDicomMetadata(rootNode);
            }
        }
        return null;
    }

    private static XmlDoc.Element extractNifti1Metadata(Node node) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker(DOC_TYPE_NIFTI1);
        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                dm.add(attr.getNodeName(), attr.getNodeValue());
            }
        }
        return dm.root();
    }

    private static XmlDoc.Element extractMincMetadata(Node node) throws Throwable {
        // TODO: implement
        System.out.println("Warning: extracting minc metadata has not been implemented by the content analyzer.");
        return null;
    }

    private static XmlDoc.Element extractAnalyzeMetadata(Node node) throws Throwable {
        // TODO: implement
        System.out.println("Warning: extracting analyze metadata has not been implemented by the content analyzer.");
        return null;
    }

    private static XmlDoc.Element extractDicomMetadata(Node node) throws Throwable {
        // TODO: implement
        System.out.println("Warning: extracting dicom metadata has not been implemented by the content analyzer.");
        return null;
    }

    public static ImageReaderDetails getImageReader(PluginService.Input in) throws Throwable {
        InputStreamDetails isd = new InputStreamDetails(in.stream(), in.length(), in.mimeType(), null, null, true);
        return getImageReader(analyze(isd));
    }

    public static ImageReaderDetails getImageReader(ServiceExecutor executor, String assetId)
            throws Throwable {
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        XmlDoc.Element re = executor.execute("asset.get", "<args><id>" + assetId + "</id></args>", null, outputs);
        if (!re.elementExists("asset/content")) {
            throw new IOException("Asset " + assetId + " does not have content.");
        }
        String type = re.value("asset/type");
        String ctype = re.value("asset/content/type");
        String cext = re.value("asset/content/type/@ext");
        String lctype = re.value("asset/content/ltype");
        long csize = re.longValue("asset/content/size");
        InputStreamDetails isd = new InputStreamDetails(outputs.output(0).stream(), csize, ctype, cext, lctype!=null?lctype:type, true);
        return getImageReader(analyze(isd));
    }

    public static ImageReaderDetails getImageReader(StreamInput in, String lctype) throws Throwable {
        return getImageReader(in.stream(), in.length(), in.type(), lctype);
    }

    private static ImageReaderDetails getImageReader(InputStreamDetails isd) throws Throwable {
        return getImageReader(isd.stream(), isd.length(), isd.type(), isd.logicalType());
    }

    private static ImageReaderDetails getImageReader(InputStream in, long csize, String ctype, String lctype) throws Throwable {

        /*
         * is input stream gzipped?
         */
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        if (FileSignatures.isGZIP(in)) {
            InputStreamDetails isd = new InputStreamDetails(in, csize, ctype, null, lctype, true);
            isd = gunzip(isd);
            ctype = isd.type();
            csize = isd.length();
            if (lctype == null && isd.logicalType() != null) {
                // set lctype only if it was not set.
                lctype = isd.logicalType();
            }
        }

        ArchiveInputDetails aid = null;
        if (ctype != null && ArchiveRegistry.isAnArchive(ctype)) {
            ArchiveInput ai = ArchiveRegistry.createInput(new SizedInputStream(in, csize), new NamedMimeType(ctype),
                    ArchiveInput.ACCESS_SEQUENTIAL);
            aid = analyze(ai);
            ctype = aid.type();
            if (lctype == null && aid.logicalType() != null) {
                lctype = aid.logicalType();
            }
        }

        /*
         * Find the image reader by logical mime type from the Mediaflux
         * ImageRegistry.
         */
        if (lctype == null) {
            // lctype is not specified and it can not be detected. Since it is
            // the key for looking up image readers. No image reader can be
            // found.
            return null;
        }
        String loniClass = LONI_READERS.get(lctype);
        if (loniClass == null) {
            System.out.println("Warning: no (LONI) image reader class for logical mime type: " + lctype
                    + " can be used.");
            return null;
        }
        ImageReader reader = null;
        Iterator<ImageReader> iterator = ImageRegistry.readersForMIMEType(lctype);
        if (iterator != null) {
            while (iterator.hasNext()) {
                ImageReader r = iterator.next();
                if (r.getClass().getName().equals(loniClass)) {
                    reader = r;
                    break;
                }
            }
        }
        if (reader == null) {
            System.out.println("Warning: no LONI image reader for logical mime type: " + lctype + " is registered.");
            return null;
        }

        /*
         * Set the input for the image reader.
         */
        if (aid != null) {
            File[] files = aid.files();
            if (ctype != null && ctype.equals("image/hdr") && files != null && files[0] != null && files[1] != null) {
                if (LOGICAL_TYPES_NIFTI1.contains(lctype) || LOGICAL_TYPES_ANALYZE.contains(lctype)) {
                    reader.setInput(new AnalyzeInputStream(new BufferedImageInputStream(new TempFileImageInputStream(
                            files[0])), new BufferedImageInputStream(new TempFileImageInputStream(files[1]))));
                } else {
                    System.out
                            .println("Warning: found NIFTI1/ANALYZE .hdr and .img pair. However, they are not the proper input for logical mime type: "
                                    + lctype + ".");
                }
            }
            if (ctype != null && ctype.equals("image/nii") && files != null && files[0] != null) {
                if (LOGICAL_TYPES_NIFTI1.contains(lctype)) {
                    reader.setInput(new BufferedImageInputStream(new TempFileImageInputStream(files[0])));
                } else {
                    System.out
                            .println("Warning: found NIFTI1 content. However, it is not the proper input for logical mime type: "
                                    + lctype + ".");
                }
            }
            if (ctype != null && ctype.equals("image/dcm") && files != null && files[0] != null) {
                if (LOGICAL_TYPES_DICOM.contains(lctype)) {
                    reader.setInput(new BufferedImageInputStream(new TempFileImageInputStream(files[0])));
                } else {
                    System.out
                            .println("Warning: found DICOM content. However, it is not the proper input for logical mime type: "
                                    + lctype + ".");
                }
            }
            if (ctype != null && ctype.equals("image/mnc") && files != null && files[0] != null) {
                if (LOGICAL_TYPES_MINC.contains(lctype)) {
                    reader.setInput(new BufferedImageInputStream(new TempFileImageInputStream(files[0])));
                } else {
                    System.out
                            .println("Warning: found MINC content. However, it is not the proper input for logical mime type: "
                                    + lctype + ".");
                }
            }
        } else {
            /*
             * the content is not in an archive.
             */
            File tmpFile = PluginTask.createTemporaryFile();
            try {
                StreamCopy.copy(in, tmpFile);
            } finally {
                in.close();
            }
            reader.setInput(new BufferedImageInputStream(new TempFileImageInputStream(tmpFile)));
        }

        if (reader.getInput() == null) {
            /*
             * No input is successfully set. log it.
             */
            System.out
                    .println("Warning: no proper input is set for image reader: " + reader.getClass().getName() + ".");
            return null;
        }
        return new ImageReaderDetails(reader, lctype);
    }

    private static ArchiveInputDetails analyze(ArchiveInput ai) throws Throwable {
        /*
         * the content is encapsulated in an archive (ctype: zip/aar/tar)
         */
        File[] files = new File[2];
        String type = null;
        String ltype = null;
        try {
            ArchiveInput.Entry e = ai.next();
            while (e != null) {
                if (!e.isDirectory()) {
                    InputStream is = e.stream();
                    if (!is.markSupported()) {
                        is = new BufferedInputStream(is);
                    }
                    try {
                        /*
                         * check file extension
                         */
                        if (e.name().endsWith(".hdr")) {
                            type = "image/hdr";
                            if (FileSignatures.isNIFTI1Pair(is)) {
                                ltype = "nifti/series";
                            } else {
                                ltype = "analyze/series";
                            }
                            files[0] = PluginTask.createTemporaryFile(".hdr");
                            StreamCopy.copy(is, files[0]);
                        } else if (e.name().endsWith(".img")) {
                            files[1] = PluginTask.createTemporaryFile(".img");
                            StreamCopy.copy(is, files[1]);
                        } else if (e.name().endsWith(".img.gz")) {
                            files[1] = PluginTask.createTemporaryFile(".img");
                            is = new BufferedInputStream(new GZIPInputStream(is));
                            StreamCopy.copy(is, files[1]);
                        } else if (e.name().endsWith(".nii")) {
                            files[0] = PluginTask.createTemporaryFile(".nii");
                            files[1] = null;
                            StreamCopy.copy(is, files[0]);
                            type = "image/nii";
                            ltype = "nifti/series";
                        } else if (e.name().endsWith(".nii.gz")) {
                            files[0] = PluginTask.createTemporaryFile(".nii");
                            files[1] = null;
                            is = new BufferedInputStream(new GZIPInputStream(is));
                            StreamCopy.copy(is, files[0]);
                            type = "image/nii";
                            ltype = "nifti/series";
                        } else if (e.name().endsWith(".dcm")) {
                            files[0] = PluginTask.createTemporaryFile(".dcm");
                            files[1] = null;
                            StreamCopy.copy(is, files[0]);
                            type = "image/dcm";
                            ltype = "dicom/series";
                        } else if (e.name().endsWith(".mnc")) {
                            files[0] = PluginTask.createTemporaryFile(".mnc");
                            files[1] = null;
                            StreamCopy.copy(is, files[0]);
                            type = "image/mnc";
                            ltype = "minc/series";
                        } else {
                            /*
                             * check stream
                             */
                            if (FileSignatures.isGZIP(is)) {
                                is = new BufferedInputStream(new GZIPInputStream(is));
                            }
                            if (FileSignatures.isNIFTI1(is)) {
                                files[0] = PluginTask.createTemporaryFile(".nii");
                                files[1] = null;
                                StreamCopy.copy(is, files[0]);
                                type = "image/nii";
                                ltype = "nifti/series";
                            } else if (FileSignatures.isDICOM(is)) {
                                files[0] = PluginTask.createTemporaryFile(".dcm");
                                files[1] = null;
                                StreamCopy.copy(is, files[0]);
                                type = "image/dcm";
                                ltype = "dicom/series";
                            } else if (FileSignatures.isMINC(is)) {
                                files[0] = PluginTask.createTemporaryFile(".mnc");
                                files[1] = null;
                                StreamCopy.copy(is, files[0]);
                                type = "image/mnc";
                                ltype = "minc/series";
                            }
                        }
                    } finally {
                        is.close();
                    }
                }
                ai.closeEntry();
                if (type != null && ltype != null & files[0] != null) {
                    break;
                }
                e = ai.next();
            }
        } finally {
            ai.close();
        }
        return new ArchiveInputDetails(files, type, ltype);
    }

    private static InputStreamDetails analyze(InputStreamDetails isd) throws Throwable {
        isd.enableMarkSupport();
        if (FileSignatures.isGZIP(isd.stream())) {
            isd = gunzip(isd);
        } else {
            if (FileSignatures.isDICOM(isd.stream())) {
                isd.setType(MIME_TYPE_DCM);
                isd.setExtension("dcm");
                isd.setLogicalType(LOGICAL_TYPES_DICOM.get(0));
            } else if (FileSignatures.isNIFTI1(isd.stream())) {
                isd.setType(MIME_TYPE_NII);
                isd.setExtension("nii");
                isd.setLogicalType(LOGICAL_TYPES_NIFTI1.get(0));
            } else if (FileSignatures.isMINC(isd.stream())) {
                isd.setType(MIME_TYPE_MNC);
                isd.setExtension("mnc");
                isd.setLogicalType(LOGICAL_TYPES_MINC.get(0));
            }
        }
        return isd;
    }

    private static InputStreamDetails gunzip(InputStreamDetails isd) throws Throwable {
        File tf = PluginTask.createTemporaryFile();
        InputStream in = new BufferedInputStream(new GZIPInputStream(isd.stream()));
        try {
            StreamCopy.copy(in, tf);
        } finally {
            in.close();
        }
        in = new BufferedInputStream(PluginTask.deleteOnCloseInputStream(tf));
        isd.setStream(in);
        isd.setLength(tf.length());
        if (FileSignatures.isTAR(in)) {
            isd.setType(MIME_TYPE_TAR);
            isd.setExtension("tar");
        } else if (FileSignatures.isNIFTI1(in)) {
            isd.setType(MIME_TYPE_NII);
            isd.setExtension("nii");
            isd.setLogicalType(LOGICAL_TYPES_NIFTI1.get(0));
        } else if (FileSignatures.isMINC(in)) {
            isd.setType(MIME_TYPE_MNC);
            isd.setExtension("tar");
            isd.setLogicalType(LOGICAL_TYPES_MINC.get(0));
        } else if (FileSignatures.isDICOM(in)) {
            isd.setType(MIME_TYPE_DCM);
            isd.setExtension("dcm");
            isd.setLogicalType(LOGICAL_TYPES_DICOM.get(0));
        } else {
            isd.setType(MIME_TYPE_CONTENT_UNKNOWN);
            isd.setExtension(null);
            isd.setLogicalType(null);
        }
        return isd;
    }

    static void registerImageReaders() {
        /*
         * Don't include analyze. This is because the MIME type image/hdr is
         * handled by both the analyze and nifti plugins as nifti files are
         * backwards compatible with analyze (I think). Even if they are not, we
         * get in trouble having multiple readers for one MIME type. I don't
         * think there is a need to handle old antiquated analyze format.
         */
        // @formatter:off
        // registerImageReader(LOGICAL_TYPES_ANALYZE, new AnalyzeImageReaderSpi());
        // @formatter:on
        registerImageReader(LOGICAL_TYPES_NIFTI1, new NiftiImageReaderSpi());
        registerImageReader(LOGICAL_TYPES_MINC, new MincImageReaderSpi());
        registerImageReader(LOGICAL_TYPES_DICOM, new DicomImageReaderSpi());
    }

    private static void registerImageReader(List<String> logicalTypes, ImageReaderSpi readerSpi) {
        String[] mimeTypes = readerSpi.getMIMETypes();
        if (mimeTypes != null) {
            for (String mimeType : mimeTypes) {
                if (logicalTypes != null && logicalTypes.contains(mimeType)) {
                    continue;
                }
                ImageRegistry.declareImageReader(mimeType, readerSpi);
            }
        }
        if (logicalTypes != null) {
            for (String ltype : logicalTypes) {
                ImageRegistry.declareImageReader(ltype, readerSpi);
            }
        }
    }
}
