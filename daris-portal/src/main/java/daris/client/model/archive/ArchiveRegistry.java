package daris.client.model.archive;

public class ArchiveRegistry {

	// @formatter:off
	public static final String[] SUPPORTED_MIME_TYPES = new String[] { 
		    "application/arc-archive",
			"application/java-archive",
			"application/jar", 
			"application/x-jar", 
			"application/x-gtar",
			"application/x-tar", 
			"application/zip", 
			"application/x-zip-compressed", 
			"application/x-zip",
			"application/x-iso9660-image" };
	// @formatter:on

	public static boolean isSupportedArchive(String mimeType) {
		if (mimeType != null) {
			for (int i = 0; i < ArchiveRegistry.SUPPORTED_MIME_TYPES.length; i++) {
				if (SUPPORTED_MIME_TYPES[i].equals(mimeType)) {
					return true;
				}
			}
		}
		return false;
	}
}
