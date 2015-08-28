package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

/**
 * Class to specify some details about this local PSSD repository. For example,
 * the custodian, the rights and a generic description of the held data.
 * 
 * 
 */
public class RepositoryDescription {

    public static final String DOC_TYPE = "daris:pssd-repository-description";
    public static final String ASSET_NAME = "daris repository description";

    public static String getAssetId(ServiceExecutor executor) throws Throwable {
        return executor.execute(
                "asset.query",
                "<args><pdist>0</pdist><size>1</size><where>" + DOC_TYPE
                        + " has value</where></args>", null, null).value("id");
    }

    public static XmlDoc.Element getAssetMeta(ServiceExecutor executor)
            throws Throwable {
        String assetId = getAssetId(executor);
        if (assetId == null) {
            return null;
        }
        return executor.execute("asset.get",
                "<args><id>" + assetId + "</id></args>", null, null).element(
                "asset");
    }

}
