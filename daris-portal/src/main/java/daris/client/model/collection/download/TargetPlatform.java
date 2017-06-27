package daris.client.model.collection.download;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.EnumerationType;

public enum TargetPlatform {
    JAVA("Java", "Cross-platform Java executable JAR file", "daris-downloader.jar"), WINDOWS("Windows",
            "Windows executable EXE file",
            "daris-downloader-win.zip"), MAC("Mac", "Mac application bundle", "daris-downloader-mac.zip");

    private String _name;
    private String _description;
    private String _filename;

    TargetPlatform(String name, String description, String filename) {
        _name = name;
        _description = description;
        _filename = filename;
    }

    @Override
    public String toString() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public String filename() {
        return _filename;
    }

    public static EnumerationType<TargetPlatform> asEnumerationType() {
        TargetPlatform[] vs = values();
        List<EnumerationType.Value<TargetPlatform>> values = new ArrayList<EnumerationType.Value<TargetPlatform>>(
                vs.length);
        for (TargetPlatform v : vs) {
            EnumerationType.Value<TargetPlatform> value = new EnumerationType.Value<TargetPlatform>(v.toString(),
                    v.description(), v);
            values.add(value);
        }
        return new EnumerationType<TargetPlatform>(values);
    }
}
