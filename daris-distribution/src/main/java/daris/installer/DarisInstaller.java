package daris.installer;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import arc.mf.client.ServerClient;
import arc.streams.SizedInputStream;
import arc.utils.ObjectUtil;

public class DarisInstaller {

	public static class PackageEntry implements Comparable<PackageEntry> {

		public static enum Type implements Comparable<Type> {
			ESSENTIALS("essentials"), CORE_SERVICES("core-services"), PORTAL(
					"portal"), SINKS("sinks"), TRANSCODERS("transcoders"), ANALYZERS(
					"analyzers");
			private String _typeName;

			Type(String typeName) {
				_typeName = typeName;
			}

			public String typeName() {
				return _typeName;
			}

			public String fullName() {
				return "daris-" + _typeName;
			}

			@Override
			public String toString() {
				return fullName();
			}

			public static Type fromString(String typeName) {
				if (typeName != null) {
					Type[] vs = values();
					for (Type v : vs) {
						if (typeName.equalsIgnoreCase(v.typeName())
								|| typeName.equalsIgnoreCase(v.fullName())) {
							return v;
						}
					}
				}
				return null;
			}
		}

		private String _jarFilePath;
		private String _path;
		private long _size;
		private Type _type;

		public PackageEntry(String jarFilePath, ZipEntry entry) {
			this(jarFilePath, entry.getName(), entry.getSize());
		}

		public PackageEntry(String jarFilePath, String path, long size) {
			_jarFilePath = jarFilePath;
			_path = path;
			_size = size;
			_type = Type.fromString(shortName());
			if (_type == null) {
				throw new IllegalArgumentException("Unknown daris package: "
						+ name());
			}
		}

		public Type type() {
			return _type;
		}

		public long size() {
			return _size;
		}

		public String path() {
			return _path;
		}

		public String jarFilePath() {
			return _jarFilePath;
		}

		public String name() {
			return nameFor(_path);
		}

		public String version() {
			return versionFor(_path);
		}

		private static String nameFor(String pkgPath) {

			int beginIndex = pkgPath.lastIndexOf("mfpkg-") + "mfpkg-".length();
			int endIndex = pkgPath.lastIndexOf('-');
			return pkgPath.substring(beginIndex, endIndex);
		}

		private static String versionFor(String pkgPath) {
			int beginIndex = pkgPath.lastIndexOf('-') + 1;
			return pkgPath.substring(beginIndex,
					pkgPath.length() - ".zip".length());
		}

		public String shortName() {
			return name().substring("daris-".length());
		}

		public static boolean isPackageEntry(ZipEntry entry) {
			if (entry.isDirectory()) {
				return false;
			}
			return isPackageEntry(entry.getName());
		}

		public static boolean isPackageEntry(String path) {
			if (path == null) {
				return false;
			}
			if (path.matches(".*mfpkg-daris-.*-\\d+\\.\\d+\\.\\d+\\.zip$")) {
				String name = nameFor(path);
				Type[] vs = Type.values();
				for (Type v : vs) {
					if (v.typeName().equalsIgnoreCase(name)
							|| v.fullName().equalsIgnoreCase(name)) {
						return true;
					}
				}
			}
			return false;
		}

		public int ordinal() {
			return _type.ordinal();
		}

		@Override
		public int compareTo(PackageEntry o) {
			return ordinal() - o.ordinal();
		}

		@Override
		public boolean equals(Object o) {
			if (o != null) {
				if (o instanceof PackageEntry) {
					PackageEntry pe = (PackageEntry) o;
					return ObjectUtil.equals(_jarFilePath, pe.jarFilePath())
							&& ObjectUtil.equals(_path, pe.path())
							&& _size == pe.size();
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (_path + _jarFilePath).hashCode();
		}

		public static boolean isValidPackageName(String name) {
			return Type.fromString(name) != null;
		}

	}

	public static String getJarFilePath() {
		String classFileName = DarisInstaller.class.getSimpleName() + ".class";
		String classFileUrl = DarisInstaller.class.getResource(classFileName)
				.toString();
		int beginIndex = "jar:file:".length();
		int endIndex = classFileUrl.indexOf("!/");
		String jarFilePath = classFileUrl.substring(beginIndex, endIndex);
		return jarFilePath;
	}

	public static Map<PackageEntry.Type, PackageEntry> getPackageEntries()
			throws Throwable {
		return getPackageEntries(getJarFilePath());
	}

	public static Map<PackageEntry.Type, PackageEntry> getPackageEntries(
			String jarFilePath) throws Throwable {
		ZipFile jarFile = null;
		Map<PackageEntry.Type, PackageEntry> pes = new TreeMap<PackageEntry.Type, PackageEntry>();
		try {
			jarFile = new ZipFile(jarFilePath);
			Enumeration<? extends ZipEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (PackageEntry.isPackageEntry(entry)) {
					PackageEntry pe = new PackageEntry(jarFilePath, entry);
					pes.put(pe.type(), pe);
				}
			}
			if (pes.isEmpty()) {
				throw new Exception(
						"Could not find any daris package in file: "
								+ jarFilePath);
			}
			return pes;
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
	}

	public static Map<PackageEntry.Type, PackageEntry> getPackageEntries(
			Set<PackageEntry.Type> pkgTypes) throws Throwable {
		return getPackageEntries(getJarFilePath(), pkgTypes);
	}

	public static Map<PackageEntry.Type, PackageEntry> getPackageEntries(
			String jarFilePath, Set<PackageEntry.Type> pkgTypes)
			throws Throwable {
		Map<PackageEntry.Type, PackageEntry> entries = getPackageEntries(jarFilePath);
		if (pkgTypes == null || pkgTypes.isEmpty()) {
			return entries;
		}
		Map<PackageEntry.Type, PackageEntry> matches = new TreeMap<PackageEntry.Type, PackageEntry>();
		for (PackageEntry.Type pkgType : pkgTypes) {
			boolean found = false;
			Collection<PackageEntry> values = entries.values();
			for (PackageEntry entry : values) {
				if (pkgType == entry.type()) {
					found = true;
					matches.put(entry.type(), entry);
				}
			}
			if (!found) {
				throw new Exception("Package: " + pkgType.fullName()
						+ " is not found in " + jarFilePath);
			}
		}
		return matches;
	}

	public static void installPackage(ServerClient.Connection cxn,
			PackageEntry pkgEntry) throws Throwable {
		ZipFile jarFile = null;
		try {
			jarFile = new ZipFile(new File(pkgEntry.jarFilePath()));
			ZipEntry zipEntry = jarFile.getEntry(pkgEntry.path());
			if (zipEntry.isDirectory()) {
				throw new Exception(
						"Invalid zip entry. Expecting a file found a directory.");
			}
			InputStream is = jarFile.getInputStream(zipEntry);
			ServerClient.Input in = new ServerClient.Input("application/zip",
					new SizedInputStream(is, zipEntry.getSize()),
					zipEntry.getName());
			cxn.execute("package.install", null, in);
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
	}

}
