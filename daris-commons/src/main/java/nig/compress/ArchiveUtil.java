package nig.compress;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.streams.SizedInputStream;

/**
 * Class that creates archives using the Arcitecta ArchiveOutput class
 * Can create archives of multiple output types
 * 
 * @author nebk
 *
 */
public class ArchiveUtil {

	private ArchiveUtil() {

	}

	/**
	 * Compresses a  list of files into an archive of the given mime type.
	 * Does not add a parent directory
	 * Does not add empty directories
	 * 
	 * @param files  The files to pack into the archive
	 * @param archive  The file which will hold the archive.  You must supply a valid File handle
	 *        (e.g. a temporary file) but with no content. 
	 * @param containerMimeType e.g. MimeTypes.AAR to get an Arcitecta archive
	 * @param clevel Compression level 0-9
	 * @param fileMimeType can be null if unknown
	 * @throws Throwable
	 */
	public static void compress (Collection<File> files, File archive, String containerMimeType, int clevel, String fileMimeType) throws Throwable {
		ArchiveOutput ao = ArchiveRegistry.createOutput(archive, containerMimeType, clevel, null);
		try {
			for (File file : files) {
				FileInputStream fis = new FileInputStream(file);
				SizedInputStream fiss = new SizedInputStream(fis, file.length());
				String name = file.getName();
				ao.add(fileMimeType, name, fiss);
				fiss.close();
				fis.close();
			}
		} finally {
			ao.end();
			ao.close();
		}
	}

	/**
	 * Compress a directory into an archive of the given mime type. The files go directly into
	 * the archive with no additional parent directory.
	 * 
	 * @param inputDirectory  The directory to compress
	 * @param archive  The file which will hold the archive.  You must supply a valid File handle
	 *        (e.g. a temporary file) but with no content. 
	 * @param containerMimeType  e.g. aar to get an Arcitecta archive
	 * @param clevel Compression level 0-9
	 * @param fileMimeType can be null if unknown
	 * @throws Throwable
	 */
	public static void compressDirectory (File inputDirectory, File archive, String containerMimeType, 
			int clevel, String fileMimeType) throws Throwable {
		if (!inputDirectory.isDirectory()) {
			throw new Exception ("Supplied file is not a directory");
		}
		ArchiveOutput ao = ArchiveRegistry.createOutput(archive, containerMimeType, clevel, null);
		try {
			compressDirectory (inputDirectory, ao, clevel, fileMimeType, -1);
		} finally {
			ao.end();
			ao.close();
		}
	}


	/**
	 * Compress a set of files into an archive of the given mime type (matches the ZipUtil and TarUtil API)
	 * DOes not include empty directories
	 * 
	 * @param files
	 * @param baseDir Specifies the top-level directory (which the files are children of) to add to the archive
	 * @param archiveFile
	 * @param containerMimeType  E.g. MimeTypes.AAR
	 * @param clevel Compression level 0-9
	 * @param fileMimeType
	 * @throws Throwable
	 */
	public static void compress (Collection<File> files, File baseDir, File  archiveFile, String containerMimeType, int clevel, String fileMimeType) throws Throwable {

		

			File[] files2 = new File[files.size()];
			int i = 0;
			for (File f : files) {
				files2[i++] = f;
			}
			compress(files2, baseDir, archiveFile, containerMimeType, clevel, fileMimeType);
	}

	
	
	/**
	 * Compress a set of files into an archive of the given mime type (matches the ZipUtil and TarUtil API)
	 * DOes not include empty directories
	 * 
	 * @param files
	 * @param baseDir Specifies the top-level directory (which the files are children of) to add to the archive
	 * @param archiveFile
	 * @param containerMimeType  E.g. MimeTypes.AAR
	 * @param clevel Compression level 0-9
	 * @param fileMimeType
	 * @throws Throwable
	 */
	public static void compress (File[] files, File baseDir, File  archiveFile, String containerMimeType, int clevel, String fileMimeType) throws Throwable {

		
		ArchiveOutput ao = ArchiveRegistry.createOutput(archiveFile, containerMimeType, clevel, null);

		try {
			compress(files, baseDir, ao, fileMimeType);
		} finally {
			ao.end();
			ao.close();
		}
	}




	private static void compressDirectory (File inputDirectory, ArchiveOutput archive,  
			int clevel, String fileMimeType, int parentLength) throws Throwable {

		String fullName = inputDirectory.getAbsolutePath();
		int l = parentLength;
		if (parentLength<=0) l = fullName.length();
		File[] files = inputDirectory.listFiles();

		for (File file : files) {
			// Pull off parent part of input directory
			String t = file.getAbsolutePath();
			String name = t.substring(l+1);

			if (file.isDirectory()) {

				// Drop empty directories implicitly
				compressDirectory (file, archive, clevel, fileMimeType, l);
			} else {
				FileInputStream fis = new FileInputStream(file);
				SizedInputStream fiss = new SizedInputStream(fis, file.length());
				archive.add(fileMimeType, name, fiss);
				fiss.close();
				fis.close();
			}
		}
	}



	private static void compress (File[] files, File baseDir, ArchiveOutput archive,  String fileMimeType) throws Throwable {

		String base = baseDir.getAbsolutePath();
		for (File file : files) {
			String name = file.getAbsolutePath();
			if (name.startsWith(base)) {
				name = name.substring(base.length());
			}
			if (name.startsWith(System.getProperty("file.separator"))) {
				name = name.substring(1);
			}
			if (file.isDirectory()) {
				// Drops empty directories implicitly 
				compress(file.listFiles(), baseDir, archive, fileMimeType);
			} else {
				FileInputStream fis = new FileInputStream(file);
				SizedInputStream fiss = new SizedInputStream(fis, file.length());
				archive.add(fileMimeType, name, fiss);
				fiss.close();
				fis.close();
			}
		}
	}
}
