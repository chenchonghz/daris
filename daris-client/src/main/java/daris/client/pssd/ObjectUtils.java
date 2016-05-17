package daris.client.pssd;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;

public class ObjectUtils {

    public static XmlDoc.Element getAssetMeta(ServerClient.Connection cxn,
            String cid) throws Throwable {
        return cxn.execute("asset.get", "<cid>" + cid + "</cid>", null, null)
                .element("asset");
    }

}
