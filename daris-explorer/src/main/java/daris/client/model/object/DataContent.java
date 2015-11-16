package daris.client.model.object;

import arc.xml.XmlDoc;
import daris.client.model.archive.ArchiveUtils;

public class DataContent {

    public final long id;
    public final long atime;
    public final String mimeType;
    public final String extension;
    public final String logicalMimeType;
    public final long size;
    public final String humanReadableSize;
    public final long csum;
    public final long stime;
    public final long copyId;
    public final String storeType;
    public final String storeName;
    public final long storeOid;
    public final boolean urlManaged;
    public final String url;

    public DataContent(XmlDoc.Element de) throws Throwable {
        this.id = de.longValue("@id", 1);
        this.atime = de.longValue("atime/@millisec", -1);
        this.mimeType = de.value("type");
        this.extension = de.value("type/@ext");
        this.logicalMimeType = de.value("ltype");
        this.size = de.longValue("size", -1);
        this.humanReadableSize = de.value("size/@h");
        this.csum = de.longValue("csum[@base='10']", -1);
        this.copyId = de.longValue("copy-id", 1);
        this.stime = de.longValue("copy-id/@stime", -1);
        this.storeOid = de.longValue("store/@oid", -1);
        this.storeType = de.value("store/@type");
        this.storeName = de.value("store");
        this.urlManaged = de.booleanValue("url/@managed", false);
        this.url = de.value("url");
    }

    public boolean isBrowsableArchive() {
        return ArchiveUtils
                .checkIfArchiveContentBrowsableByExtention(this.extension);
    }

}
