package daris.client.model.collection.archive;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.collection.CollectionArchiveFormatDataSource;
import daris.client.model.collection.messages.CollectionContentSizeSum;
import daris.client.model.object.DObjectRef;

public enum ArchiveFormat {
    aar, zip, tgz;
    @Override
    public String toString() {
        return this.name();
    }

    public long maxSize() {
        if (this == zip) {
            return Long.MAX_VALUE; // 4GB - 1
        } else if (this == tgz) {
            return 8589934592L; // 8GB - 1;
        } else {
            return Long.MAX_VALUE;
        }
    }

    public static List<ArchiveFormat> availableFormatsForSize(long size) {
        List<ArchiveFormat> formats = new ArrayList<ArchiveFormat>();
        formats.add(ArchiveFormat.aar);
        if (size < tgz.maxSize()) {
            formats.add(0, ArchiveFormat.tgz);
        }
        if (size < zip.maxSize()) {
            formats.add(0, ArchiveFormat.zip);
        }
        return formats;
    }

    public static void availableFormatsForCollection(String cid,
            boolean includeAttachments,
            final ObjectResolveHandler<List<ArchiveFormat>> handler) {
        new CollectionContentSizeSum(cid, includeAttachments)
                .send(new ObjectMessageResponse<Long>() {

                    @Override
                    public void responded(Long size) {
                        if (size == null) {
                            handler.resolved(null);
                            return;
                        }
                        List<ArchiveFormat> formats = availableFormatsForSize(
                                size);
                        handler.resolved(formats);
                    }
                });
    }

    public static ArchiveFormat fromString(String value) {
        if (value != null) {
            if (zip.toString().equalsIgnoreCase(value)) {
                return zip;
            }
            if (tgz.toString().equalsIgnoreCase(value)) {
                return tgz;
            }
            if (aar.toString().equalsIgnoreCase(value)) {
                return aar;
            }
        }
        return null;
    }

    public static DynamicEnumerationDataSource<ArchiveFormat> dataSourceFor(
            DObjectRef obj, boolean includeAttachments) {
        return new CollectionArchiveFormatDataSource(obj, includeAttachments);
    }

    public static EnumerationType<ArchiveFormat> enumTypeFor(DObjectRef obj,
            boolean includeAttachments) {
        return new EnumerationType<ArchiveFormat>(
                dataSourceFor(obj, includeAttachments));
    }
}
