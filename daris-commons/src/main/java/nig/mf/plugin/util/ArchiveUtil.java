package nig.mf.plugin.util;

import java.io.File;


import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.ServiceExecutor;
import arc.mime.NamedMimeType;

/**
 * SOme archive related helper functions that use plugin services
 * See also nig.compress.ArchiveUtil
 * 
 * @author nebk
 *
 */
public class ArchiveUtil {

	/**
	 * Unpack the content of an asset into a temporary directory
	 * 
	 * @param executor
	 * @param id
	 * @param tempDir
	 * @throws Throwable
	 */
	public static void unpackContent (ServiceExecutor executor, String id, File tempDir) throws Throwable {
		
		// Get the content into a file
		File f = arc.mf.plugin.PluginTask.createTemporaryFile();
		AssetUtil.getContentInFile(executor, id, f);
		NamedMimeType contentMimeType = new NamedMimeType(AssetUtil.getContentMimeType(executor, id));
		
		// Unpack the files into temporary directory
		ArchiveInput ai = ArchiveRegistry.createInput(f, contentMimeType);
		ArchiveExtractor.extract(ai, tempDir, false, true, false);	
		ai.close();
		f.delete();
	}
}
