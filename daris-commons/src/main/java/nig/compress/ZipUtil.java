package nig.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipUtil {

	private ZipUtil() {

	}

	public static final int BUFFER_SIZE = 2048;
	public static final int CLEVEL = ZipArchiveOutputStream.DEFAULT_COMPRESSION;

	public static void zip(File dir, File zipFile) throws Throwable {
		zip(dir, true, zipFile, CLEVEL);

	}

	public static void zip(File dir, boolean self, File zipFile) throws Throwable {

		zip(dir, self, zipFile, CLEVEL);

	}

	public static void zip(File dir, File zipFile, int clevel) throws Throwable {

		zip(dir, true, zipFile, clevel);

	}

	public static void zip(File dir, boolean self, File zipFile, int clevel) throws Throwable {

		if (self) {
			// Find the parent directory. If 'dir' was supplied as a relative path originally
			// it will fail to find the parent directory. Work around this by
			// remaking from the absolute path
			File t = new File(dir.getAbsolutePath());
			zip(new File[] { dir }, t.getParentFile(), zipFile, clevel);
		} else {
			zip(dir.listFiles(), dir, zipFile, clevel);
		}

	}

	public static void zip(File[] filesToZip, File baseDir, File zipFile) throws Throwable {

		zip(filesToZip, baseDir, zipFile, ZipArchiveOutputStream.DEFAULT_COMPRESSION);

	}

	public static void zip(File[] filesToZip, File baseDir, File zipFile, int clevel) throws Throwable {

		ZipArchiveOutputStream os = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile),
				BUFFER_SIZE));
		try {
			os.setLevel(clevel);
			os.setMethod(ZipArchiveOutputStream.DEFLATED);
			zip(filesToZip, baseDir, os);
		} finally {
			os.close();
		}

	}

	public static void zip(Collection<File> filesToZip, File baseDir, File zipFile) throws Throwable {

		zip(filesToZip, baseDir, zipFile, ZipArchiveOutputStream.DEFAULT_COMPRESSION);

	}

	public static void zip(Collection<File> filesToZip, File baseDir, File zipFile, int clevel) throws Throwable {

		File[] files = new File[filesToZip.size()];
		int i = 0;
		for (File f : filesToZip) {
			files[i++] = f;
		}
		zip(files, baseDir, zipFile, clevel);

	}

	/**
	 * Add one file to the open zip archive stream. Drops the file if it is a directory
	 * 
	 * @param fileToZip
	 * @param baseDir
	 * @param os
	 * @throws Throwable
	 */
	public static void zip(File fileToZip, ZipArchiveOutputStream os) throws Throwable {
		if (fileToZip.isDirectory()) return;

		byte buffer[] = new byte[BUFFER_SIZE];
		String name = fileToZip.getName();
		ZipArchiveEntry entry = new ZipArchiveEntry(fileToZip, name);
		entry.setSize(fileToZip.length());
		os.putArchiveEntry(entry);
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileToZip), BUFFER_SIZE);
		int count;
		try {
			while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				os.write(buffer, 0, count);
			}
		} finally {
			is.close();
		}
		os.closeArchiveEntry();
	}

	private static void zip(File[] filesToZip, File baseDir, ZipArchiveOutputStream os) throws Throwable {

		System.out.println("basedir="+baseDir);
		String base = baseDir.getAbsolutePath();
		byte buffer[] = new byte[BUFFER_SIZE];
		for (File f : filesToZip) {
			String name = f.getAbsolutePath();
			if (name.startsWith(base)) {
				name = name.substring(base.length());
			}
			if (name.startsWith(System.getProperty("file.separator"))) {
				name = name.substring(1);
			}
			if (f.isDirectory()) {
				ZipArchiveEntry entry = new ZipArchiveEntry(f, name + "/");
				os.putArchiveEntry(entry);
				os.closeArchiveEntry();
				zip(f.listFiles(), baseDir, os);
			} else {
				ZipArchiveEntry entry = new ZipArchiveEntry(f, name);
				entry.setSize(f.length());
				os.putArchiveEntry(entry);
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);
				int count;
				try {
					while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
						os.write(buffer, 0, count);
					}
				} finally {
					is.close();
				}
				os.closeArchiveEntry();
			}
		}
	}




	/**
	 * Extract zip file to the specified directory. (Using Apache Commons
	 * Compress Library)
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir) throws Throwable {

		unzip2(file, toDir, null);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files
	 * into the specified Collection. (Using Apache Commons Compress Library)
	 * 
	 * @param file
	 *            the zip file to extract
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the Collection of the extracted files
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir, Collection<File> files) throws Throwable {

		unzip2(file, toDir, files, false);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files
	 * into the specified Collection. (Using Apache Commons Compress Library).
	 * The order of unzipping does not preserve the order in which files were
	 * packed in to the zip archive.
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the collection of the extracted files. Set to null if no
	 *            references to the extracted files need to be kept.
	 * @param stream
	 *            set to true to use ZipArchiveInputStream instead of ZipFile.
	 *            See <a
	 *            href="http://commons.apache.org/compress/zip.html">Detail</a>
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir, Collection<File> files, boolean stream) throws Throwable {

		byte[] buffer = new byte[BUFFER_SIZE];
		if (stream) {
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
			ZipArchiveEntry entry;
			while ((entry = zis.getNextZipEntry()) != null) {
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
			zis.close();
		} else {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<?> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE);
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
						is.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
		}

	}

	public static void unzip(File file, File toDir) throws Throwable {

		unzip(file, toDir, null);

	}

	public static void unzip(File file, File toDir, Collection<File> files) throws Throwable {

		unzip(file, toDir, files, false);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files
	 * into the specified Collection. (Using java.util.zip). The order of
	 * unzipping does preserve the order in which files were packed in to the
	 * zip archive.
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the collection of the extracted files. Set to null if no
	 *            references to the extracted files need to be kept.
	 * @param stream
	 *            set to true to use ZipInputStream instead of ZipFile. See <a
	 *            href=
	 *            "http://java.sun.com/developer/technicalArticles/Programming/compression/"
	 *            >Detail</a>
	 * @throws Throwable
	 */
	public static void unzip(File file, File toDir, Collection<File> files, boolean stream) throws Throwable {

		byte[] buffer = new byte[BUFFER_SIZE];
		if (stream) {
			java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			java.util.zip.ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
			zis.close();
		} else {
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file);
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				java.util.zip.ZipEntry entry = (java.util.zip.ZipEntry) entries.nextElement();
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE);
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
						is.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
		}

	}

	public static byte[] MAGIC = { 'P', 'K', 0x3, 0x4 };

	/**
	 * The method to test if a input stream is a zip archive.
	 * 
	 * @param in
	 *            the input stream to test.
	 * @return
	 */
	public static boolean isZipStream(InputStream in) throws Throwable {

		if (!in.markSupported()) {
			throw new IOException("The stream does not support mark.");
		}
		boolean isZip = true;
		try {
			in.mark(MAGIC.length);
			for (int i = 0; i < MAGIC.length; i++) {
				if (MAGIC[i] != (byte) in.read()) {
					isZip = false;
					break;
				}
			}
			in.reset();
		} catch (IOException e) {
			isZip = false;
		}
		return isZip;
	}

	/**
	 * Test if a file is a zip file.
	 * 
	 * @param f
	 *            the file to test.
	 * @return
	 */
	public static boolean isZipFile(File f) {

		boolean isZip = true;
		byte[] buffer = new byte[MAGIC.length];
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			raf.readFully(buffer);
			for (int i = 0; i < MAGIC.length; i++) {
				if (buffer[i] != MAGIC[i]) {
					isZip = false;
					break;
				}
			}
			raf.close();
		} catch (Throwable e) {
			isZip = false;
		}
		return isZip;
	}


	/**
	 * Find the Zip CRC32 checksum in the desired base.
	 * Mediaflux uses this algorithm with base 16  for setting its content checksum
	 * This algorithm copes with large files
	 * 
	 * @param f
	 * @param radix
	 * @return
	 * @throws Throwable
	 */
	public static String getCRC32 (File f, int radix) throws Throwable {


		FileInputStream file = new FileInputStream(f);
		CheckedInputStream check = 
				new CheckedInputStream(file, new CRC32());
		BufferedInputStream in = 
				new BufferedInputStream(check);
		while (in.read() != -1) {
			// Read file in completely
		}
		in.close();
		long n = check.getChecksum().getValue();
		check.close();
		return Long.toString(n, radix);

	}



}
