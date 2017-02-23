package daris.plugin.asset.path;

import arc.mf.plugin.PluginLog;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;

public class RelatedXpathToken extends Token {

    // TODO This is not done because there is not yet use case requires this.
    RelatedXpathToken(String s) {
        super(s);
    }

    @Override
    public String compile(ServiceExecutor executor, Element assetMeta) {
        try {
            String relationship = extractRelationship(source());
            String relatedAssetId = assetMeta.value("related[@type='" + relationship + "']/to");
            String relatedXpath = extractRelatedXpath(source());
            XmlDoc.Element relatedAssetMeta = executor
                    .execute("asset.get", "<args><id>" + relatedAssetId + "</id></args>", null, null).element("asset");
            return relatedAssetMeta.value(relatedXpath);
        } catch (Throwable e) {
            PluginLog.log().add(PluginLog.ERROR, e.getMessage(), e);
            return null;
        }
    }

    static String extractRelationship(String token) throws Throwable {
        // TODO
        return null;
    }

    static String extractRelatedXpath(String token) throws Throwable {
        // TODO
        return null;
    }

}
