package daris.client.model.archive;

public class ArchiveUtils {

    public static boolean checkIfArchiveContentBrowsableByExtention(
            String ext) {
        return "aar".equalsIgnoreCase(ext) || "zip".equalsIgnoreCase(ext)
                || "tar".equalsIgnoreCase(ext) || "jar".equalsIgnoreCase(ext);
    }

    public static boolean checkIfArchiveContentBrowsableByMimeType(
            String mimeType) {
        return "application/arc-archive".equalsIgnoreCase(mimeType)
                || "application/java-archive".equalsIgnoreCase(mimeType)
                || "application/jar".equalsIgnoreCase(mimeType)
                || "application/x-jar".equalsIgnoreCase(mimeType)
                || "application/x-tar".equalsIgnoreCase(mimeType)
                || "application/zip".equalsIgnoreCase(mimeType)
                || "application/x-zip-compressed".equalsIgnoreCase(mimeType)
                || "application/x-zip".equalsIgnoreCase(mimeType);
    }

}
