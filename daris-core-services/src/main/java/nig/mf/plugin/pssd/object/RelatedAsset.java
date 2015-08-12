package nig.mf.plugin.pssd.object;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nig.mf.MimeTypes;
import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Inputs;
import arc.mf.plugin.PluginService.Outputs;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class RelatedAsset {

	public static final int BUFFER_SIZE = 2048;

	public static enum IfExists {

		error, rename;
		public static IfExists fromString(String s) {
			IfExists[] vs = values();
			for (int i = 0; i < vs.length; i++) {
				if (vs[i].toString().equalsIgnoreCase(s)) {
					return vs[i];
				}
			}
			return null;
		}

		public static EnumType enumType() {
			IfExists[] vs = values();
			String[] svs = new String[vs.length];
			for (int i = 0; i < vs.length; i++) {
				svs[i] = vs[i].toString();
			}
			return new EnumType(svs);
		}

	}

	protected static void update(ServiceExecutor executor, String assetId, String name, String description,
			PluginService.Input input) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		dm.add("name", name);
		dm.add("description", description == null ? "" : description);
		executor.execute("asset.set", dm.root(), new Inputs(input), null);
	}

	protected static void create(ServiceExecutor executor, String cid, String relationshipType, String namespace,
			String name, String description, PluginService.Input input, IfExists ifExists, XmlWriter w)
			throws Throwable {

		boolean assetExists = Asset.exists(executor,
				namespace != null ? namespace : Asset.getNamespaceByCid(executor, null, cid), name);
		boolean rename = false;
		if (assetExists) {
			switch (ifExists) {
			case error:
				throw new Exception("Asset(name='" + name + "') already exists.");
			case rename:
				rename = true;
				break;
			default:
				break;
			}
		}
		/*
		 * create the asset without name
		 */
		XmlDocMaker dm = new XmlDocMaker("args");
		if (namespace != null) {
			dm.add("namespace", namespace);
		} else {
			dm.add("namespace", Asset.getNamespaceByCid(executor, null, cid));
		}
		if (description != null) {
			dm.add("description", description);
		}
		dm.push("related");
		dm.add("from", new String[] { "relationship", relationshipType }, Asset.getIdByCid(executor, cid));
		dm.pop();
		XmlDoc.Element r = executor.execute("asset.create", dm.root(), new Inputs(input), null);
		String assetId = r.value("id");
		
		/*
		 * set the asset name
		 */
		dm = new XmlDocMaker("args");
		if (rename) {
			name = assetId + "_" + name;
		}
		dm.add("name", name);
		dm.add("id", assetId);
		executor.execute("asset.set", dm.root());
		w.add(relationshipType, new String[] { "id", assetId, "name", name });
	}

	protected static Collection<String> findAll(ServiceExecutor executor, ServerRoute sroute, String cid,
			String relationshipType) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root());
		return r.values("asset/related[@type='" + relationshipType + "']/to");
	}

	protected static String find(ServiceExecutor executor, ServerRoute sroute, String cid, String relationshipType,
			String namespace, String name) throws Throwable {
		String inverseRelationshipType = RelationshipType.getInverseType(executor, relationshipType);
		StringBuilder sb = new StringBuilder();
		sb.append("(name='" + name + "')");
		if (namespace != null) {
			sb.append(" and (namespace='" + namespace + "')");
		}
		sb.append(" and (");
		if (inverseRelationshipType == null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("cid", cid);
			XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root());
			Collection<String> ids = r.values("asset/related[@type='" + relationshipType + "']/to");
			if (ids == null || ids.isEmpty()) {
				return null;
			}
			for (Iterator<String> it = ids.iterator(); it.hasNext();) {
				String id = it.next();
				sb.append("id=" + id);
				if (it.hasNext()) {
					sb.append(" or ");
				}
			}
		} else {
			sb.append("related to{" + inverseRelationshipType + "}" + " (cid='" + cid + "')");
		}
		sb.append(")");
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", sb.toString());
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.value("id");
	}

	protected static void remove(ServiceExecutor executor, String cid, String relationshipType, Collection<String> aids)
			throws Throwable {
		Collection<String> ids = findAll(executor, null, cid, relationshipType);
		if (ids == null || ids.isEmpty()) {
			return;
		}
		if (aids == null || aids.isEmpty()) {
			return;
		}
		for (String aid : aids) {
			if (!ids.contains(aid)) {
				throw new Exception("Asset(id=" + aid + ") is not " + relationshipType + " of object(id=" + cid + ").");
			}
		}
		for (String aid : aids) {
			Asset.destroyById(executor, aid);
		}
	}

	protected static boolean exists(ServiceExecutor executor, ServerRoute sroute, String cid, String relationshipType,
			String namespace, String name) throws Throwable {
		return find(executor, sroute, cid, relationshipType, namespace, name) != null;
	}

	protected static void clear(ServiceExecutor executor, String cid, String relationshipType) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		Collection<String> ids = r.values("asset/related[@type='" + relationshipType + "']/to");
		if (ids == null || ids.isEmpty()) {
			return;
		}
		for (String id : ids) {
			Asset.destroyById(executor, id);
		}
	}

	public static void get(ServiceExecutor executor, ServerRoute sroute, Collection<String> aids, Outputs outputs)
			throws Throwable {

		if (aids.size() == 1) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", aids.iterator().next());
			dm.add("pdist", 0); // Force local on whatever server it's executed
			executor.execute(sroute, "asset.get", dm.root(), null, outputs);
		} else {
			File of = PluginService.createTemporaryFile();
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(of)));
			byte[] buffer = new byte[BUFFER_SIZE];
			for (String aid : aids) {
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", aid);
				PluginService.Outputs os = new PluginService.Outputs(1);
				XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root(), null, os);
				String name = r.value("asset/name");
				ZipEntry entry = new ZipEntry(name);
				zos.putNextEntry(entry);
				BufferedInputStream is = new BufferedInputStream(os.output(0).stream());

				int count;
				while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
					zos.write(buffer, 0, count);
				}
				is.close();
			}
			zos.close();
			outputs.output(0).setData(PluginService.deleteOnCloseInputStream(of), of.length(), MimeTypes.ZIP);
		}
	}
}
