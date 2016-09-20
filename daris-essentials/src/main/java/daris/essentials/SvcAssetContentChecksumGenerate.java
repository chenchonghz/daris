package daris.essentials;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetContentChecksumGenerate extends PluginService {

    public static final String SERVICE_NAME = "nig.asset.content.checksum.generate";

    private Interface _defn;

    public SvcAssetContentChecksumGenerate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT,
                "The path to the asset. If not specified, then a citeable identifier or an alterate identifier must be specified.",
                0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "he citeable identifier of the asset. If not specified, then a non-citeable 'id' or an alterate identifier must be specified.",
                0, 1));
        _defn.add(
                new Interface.Element("type",
                        new EnumType(new String[] { "crc32", "md5", "sha1",
                                "sha256" }),
                        "The checksum type/algorithm.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Generate content checksum for the specified asset.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {

        String id = args.value("id");
        String cid = args.value("cid");
        if (id == null && cid == null) {
            throw new IllegalArgumentException(
                    "Either id or cid must be specified.");
        }
        String type = args.value("type");
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        PluginService.Outputs o = new PluginService.Outputs(1);
        executor().execute("asset.get", dm.root(), null, o);
        InputStream is = o.output(0).stream();
        String checksum = null;
        if ("md5".equalsIgnoreCase(type)) {
            checksum = getMD5(is);
        } else if ("sha1".equalsIgnoreCase(type)) {
            checksum = getSHA1(is);
        } else if ("sha256".equalsIgnoreCase(type)) {
            checksum = getSHA256(is);
        } else if ("crc32".equalsIgnoreCase(type)) {
            checksum = getCRC32(is);
        } else {
            throw new Exception("Unknown checksum type: " + type);
        }
        w.add("csum", new String[] { "type", type }, checksum);
    }

    static String getCRC32(InputStream in) throws Throwable {
        CheckedInputStream cin = new CheckedInputStream(
                new BufferedInputStream(in), new CRC32());
        byte[] buffer = new byte[1024];
        try {
            while (cin.read(buffer) != -1) {
                // Read file in completely
            }
        } finally {
            cin.close();
            in.close();
        }
        long value = cin.getChecksum().getValue();
        return Long.toHexString(value);
    }

    static String getSHA1(InputStream in) throws Throwable {
        return getDigest("SHA-1", in);
    }

    static String getSHA256(InputStream in) throws Throwable {
        return getDigest("SHA-256", in);
    }

    static String getMD5(InputStream in) throws Throwable {
        return getDigest("MD5", in);
    }

    static byte[] getDigest(InputStream in, String algorithm) throws Throwable {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        DigestInputStream dis = new DigestInputStream(in, md);
        try {
            byte[] buffer = new byte[1024];
            while (dis.read(buffer) != -1) {
                // Read the stream fully
            }
        } finally {
            dis.close();
            in.close();
        }
        return md.digest();
    }

    static String getDigest(String algorithm, InputStream in) throws Throwable {
        return toHexString(getDigest(in, algorithm));
    }

    private static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
