package nig.mf.plugin.pssd.object;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlWriter;

public class Attachment extends RelatedAsset {

	public static final String RELATIONSHIP_TYPE = "attachment";
	public static final String INVERSE_RELATIONSHIP_TYPE = "attached-to";

	public static void create(ServiceExecutor executor, String cid, String name, String description,
			PluginService.Input input, IfExists ifExists, XmlWriter w) throws Throwable {
		RelatedAsset.create(executor, cid, RELATIONSHIP_TYPE, null, name, description, input, ifExists, w);
	}

	// TODO: remove it as it is deprecated.
	private static void destroy(ServiceExecutor executor, String cid, Collection<String> aids) throws Throwable {
		RelatedAsset.remove(executor, cid, RELATIONSHIP_TYPE, aids);
	}

	public static String find(ServiceExecutor executor, ServerRoute sroute, String cid, String name) throws Throwable {
		return RelatedAsset.find(executor, sroute, cid, RELATIONSHIP_TYPE, null, name);
	}

	public static Collection<String> findAll(ServiceExecutor executor, ServerRoute sroute, String cid) throws Throwable {
		return RelatedAsset.findAll(executor, sroute, cid, RELATIONSHIP_TYPE);
	}

	public static boolean exists(ServiceExecutor executor, ServerRoute sroute, String cid, String name)
			throws Throwable {
		return RelatedAsset.exists(executor, sroute, cid, RELATIONSHIP_TYPE, null, name);
	}

	// TODO: remove it as it is deprecated.
	private static void clear(ServiceExecutor executor, String cid) throws Throwable {
		RelatedAsset.clear(executor, cid, RELATIONSHIP_TYPE);
	}

}
